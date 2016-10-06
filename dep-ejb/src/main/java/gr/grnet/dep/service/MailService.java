package gr.grnet.dep.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.MailRecord;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

@Stateless
public class MailService {

    @Resource(name = "java:jboss/mail/Default")
    protected Session mailSession;

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @EJB
    MailService mailService;

    protected static Configuration conf;

    static {
        try {
            conf = DEPConfigurationFactory.getServerConfiguration();
        } catch (ConfigurationException e) {
        }
    }

    private ResourceBundle resources = ResourceBundle.getBundle("dep-mail", new Locale("el"));

    private static final Logger logger = Logger.getLogger(MailService.class.getName());

    public void pushEmail(String aToEmailAddr, String aSubject, String aBody) {
        MailRecord mail = new MailRecord();
        mail.setToEmailAddr(aToEmailAddr);
        mail.setSubject(aSubject);
        mail.setBody(aBody);
        pushEmail(mail);
    }

    public void pushEmail(MailRecord mail) {
        try {
            em.createQuery("select m.id from MailRecord m " +
                    "where m.toEmailAddr = :toEmailAddr " +
                    "and m.subject = :subject " +
                    "and m.body = :body", Long.class)
                    .setParameter("toEmailAddr", mail.getToEmailAddr())
                    .setParameter("subject", mail.getSubject())
                    .setParameter("body", mail.getBody())
                    .getSingleResult();
        } catch (NoResultException e) {
            // Not found, so persist
            em.persist(mail);
        } catch (EJBTransactionRolledbackException e) {
            // Email already exists, do nothing, but log a message
            logger.log(Level.FINE, "Attempted to push duplicate email: " + mail.toString() + " " + e.getMessage());
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public MailRecord popEmail() {
        try {
            MailRecord mail = (MailRecord) em.createQuery(
                    "from MailRecord")
                    .setMaxResults(1)
                    .getSingleResult();
            em.remove(mail);
            em.flush();

            return mail;
        } catch (NoResultException e) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int sendPendingEmails() {
        int i = 0;

        MailRecord mail = mailService.popEmail();
        while (mail != null) {
            mailService.sendEmail(mail);
            mail = mailService.popEmail();
            i += 1;

            if (i % 10 == 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Mail sent error: ", e.getMessage());
                }
            }
        }

        return i;
    }

    @Asynchronous
    public Future<MailRecord> postEmail(String aToEmailAddr, String aSubjectKey, String aBodyKey, Map<String, String> parameters) {
        return postEmail(aToEmailAddr, aSubjectKey, aBodyKey, parameters, false);
    }

    @Asynchronous
    public Future<MailRecord> postEmail(String aToEmailAddr, String aSubjectKey, String aBodyKey, Map<String, String> parameters, boolean sendNow) {
        // Subject
        String aSubject = resources.getString(aSubjectKey);
        // Body
        String aBody = resources.getString(aBodyKey);
        for (String key : parameters.keySet()) {
            String value = parameters.get(key);
            if (value == null || value.trim().isEmpty()) {
                value = "-";
            }
            aBody = aBody.replaceAll("\\[" + key + "\\]", value);
        }
        // Replace login with link
        aBody = aBody.replaceAll("\\[login\\]", "Είσοδο");
        // Add footer
        String aFooter = resources.getString("default.footer");
        aBody = aBody.concat("<br/><br/>").concat(aFooter);
        // end: Body

        MailRecord mail = new MailRecord();
        mail.setToEmailAddr(aToEmailAddr);
        mail.setSubject(aSubject);
        mail.setBody(aBody);
        if (sendNow) {
            mailService.sendEmail(mail);
        } else {
            mailService.pushEmail(mail);
        }
        return new AsyncResult<>(mail);
    }

    public void sendEmail(String aToEmailAddr, String aSubject, String aBody) {
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("no-reply@apella.grnet.gr"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
            message.setSubject(aSubject, "UTF-8");

            MimeMultipart multipart = new MimeMultipart("alternative");
            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(aBody, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlBodyPart);
            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Failed to send email to " + aToEmailAddr + " " + aSubject + "\n" + aBody + ": " + e.getMessage());
        }
    }

    public void sendEmail(MailRecord mail) {
        sendEmail(mail.getToEmailAddr(), mail.getSubject(), mail.getBody());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendLoginEmail(Long userId, boolean sendNow) {
        User u = em.find(User.class, userId);
        if (u == null) {
            logger.log(Level.WARNING, "Failed to send login email user with id " + userId + " not found");
            return;
        }
        try {
            // Double Check here:
            if (u.getAuthenticationType().equals(AuthenticationType.EMAIL) &&
                    u.getPermanentAuthToken() != null) {

                String aToEmailAddr = u.getContactInfo().getEmail();
                String aSubject = resources.getString("login.email.subject");
                String aBody = resources.getString("login.email.body")
                        .replaceAll("\\[firstname_el\\]", u.getFirstname("el"))
                        .replaceAll("\\[lastname_el\\]", u.getLastname("el"))
                        .replaceAll("\\[firstname_en\\]", u.getFirstname("en"))
                        .replaceAll("\\[lastname_en\\]", u.getLastname("en"))
                        .replaceAll("\\[loginLink\\]", "<a href=\"" + getloginLink(u.getPermanentAuthToken()) + "\">Είσοδος</a>");
                if (sendNow) {
                    sendEmail(aToEmailAddr, aSubject, aBody);
                } else {
                    pushEmail(aToEmailAddr, aSubject, aBody);
                }
                u.setLoginEmailSent(Boolean.TRUE);
                logger.log(Level.FINE, "Sent login email to user with id " + u.getId() + " " + getloginLink(u.getPermanentAuthToken()));
            } else {
                logger.log(Level.FINE, "Skipped login email for user with id " + u.getId());
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Failed to send login email");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendReminderLoginEmail(Long userId, boolean sendNow) {
        User u = em.find(User.class, userId);
        if (u == null) {
            logger.log(Level.WARNING, "Failed to send login email user with id " + userId + " not found");
            return;
        }
        try {
            // Double Check here:
            if (u.getAuthenticationType().equals(AuthenticationType.EMAIL) &&
                    u.getPermanentAuthToken() != null) {

                String aToEmailAddr = u.getContactInfo().getEmail();
                String aSubject = resources.getString("reminder.login.email.subject");
                String aBody = resources.getString("reminder.login.email.body")
                        .replaceAll("\\[firstname_el\\]", u.getFirstname("el"))
                        .replaceAll("\\[lastname_el\\]", u.getLastname("el"))
                        .replaceAll("\\[firstname_en\\]", u.getFirstname("en"))
                        .replaceAll("\\[lastname_en\\]", u.getLastname("en"))
                        .replaceAll("\\[loginLink\\]", "<a href=\"" + getloginLink(u.getPermanentAuthToken()) + "\">Είσοδος</a>");
                if (sendNow) {
                    sendEmail(aToEmailAddr, aSubject, aBody);
                } else {
                    pushEmail(aToEmailAddr, aSubject, aBody);
                }
                u.setLoginEmailSent(Boolean.TRUE);
                logger.log(Level.FINE, "Sent login email to user with id " + u.getId() + " " + getloginLink(u.getPermanentAuthToken()));
            } else {
                logger.log(Level.FINE, "Skipped login email for user with id " + u.getId());
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Failed to send login email");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendShibbolethConnectEmail(Long userId, boolean sendNow) {
        User u = em.find(User.class, userId);
        if (u == null) {
            logger.log(Level.WARNING, "Failed to send login email user with id " + userId + " not found");
            return;
        }
        try {
            // Double Check here:
            if (u.getAuthenticationType().equals(AuthenticationType.EMAIL) &&
                    u.getPermanentAuthToken() != null) {

                String aToEmailAddr = u.getContactInfo().getEmail();
                String aSubject = resources.getString("shibboleth.connect.email.subject");
                String aBody = resources.getString("shibboleth.connect.email.body")
                        .replaceAll("\\[firstname_el\\]", u.getFirstname("el"))
                        .replaceAll("\\[lastname_el\\]", u.getLastname("el"))
                        .replaceAll("\\[firstname_en\\]", u.getFirstname("en"))
                        .replaceAll("\\[lastname_en\\]", u.getLastname("en"))
                        .replaceAll("\\[loginLink\\]", "<a href=\"" + getloginLink(u.getPermanentAuthToken()) + "\">Είσοδος</a>");
                if (sendNow) {
                    sendEmail(aToEmailAddr, aSubject, aBody);
                } else {
                    pushEmail(aToEmailAddr, aSubject, aBody);
                }
                u.setLoginEmailSent(Boolean.TRUE);
                logger.log(Level.FINE, "Sent connect to shibboleth email to user with id " + u.getId() + " " + getloginLink(u.getPermanentAuthToken()));
            } else {
                logger.log(Level.FINE, "Skipped Sent connect to shibboleth email for user with id " + u.getId());
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Failed to send login email");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendReminderFinalizeRegistrationEmail(Long userId, boolean sendNow) {
        User u = em.find(User.class, userId);
        if (u == null) {
            logger.log(Level.WARNING, "Failed to sendReminderFinalizeRegistrationEmail, user with id " + userId + " not found");
            return;
        }
        try {
            // Double Check here:
            if (u.getAuthenticationType().equals(AuthenticationType.EMAIL) &&
                    u.getPermanentAuthToken() != null) {

                String aToEmailAddr = u.getContactInfo().getEmail();
                String aSubject = resources.getString("reminder.complete.registration.email.subject");
                String aBody = resources.getString("reminder.complete.registration.email.body")
                        .replaceAll("\\[firstname_el\\]", u.getFirstname("el"))
                        .replaceAll("\\[lastname_el\\]", u.getLastname("el"))
                        .replaceAll("\\[firstname_en\\]", u.getFirstname("en"))
                        .replaceAll("\\[lastname_en\\]", u.getLastname("en"))
                        .replaceAll("\\[loginLink\\]", "<a href=\"" + getloginLink(u.getPermanentAuthToken()) + "\">Είσοδος</a>");
                if (sendNow) {
                    sendEmail(aToEmailAddr, aSubject, aBody);
                } else {
                    pushEmail(aToEmailAddr, aSubject, aBody);
                }
                u.setLoginEmailSent(Boolean.TRUE);
                logger.log(Level.FINE, "Sent ReminderFinalizeRegistrationEmail email to user with id " + u.getId() + " " + getloginLink(u.getPermanentAuthToken()));
            } else {
                logger.log(Level.FINE, "Skipped ReminderFinalizeRegistrationEmail for user with id " + u.getId());
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Failed to send ReminderFinalizeRegistrationEmail");
        }
    }

    private String getloginLink(String token) throws UnsupportedEncodingException {
        return conf.getString("rest.url") +
                "/email/login?nextURL=" + conf.getString("home.url") + "/apella.html" +
                "&user=" + URLEncoder.encode(token, "UTF-8");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendEvaluationEmail(Long userId, boolean sendNow) {
        User u = em.find(User.class, userId);
        if (u == null) {
            logger.log(Level.WARNING, "Failed to sendEvaluationEmail, user with id " + userId + " not found");
            return;
        }
        try {
            // Double Check here:
            if (u.getStatus().equals(User.UserStatus.ACTIVE) &&
                    u.getRole(u.getPrimaryRole()).getStatus().equals(Role.RoleStatus.ACTIVE)) {

                String aToEmailAddr = u.getContactInfo().getEmail();
                String aSubject = resources.getString("evaluation.email.subject");
                String aBody = resources.getString("evaluation.email.body")
                        .replaceAll("\\[firstname_el\\]", u.getFirstname("el"))
                        .replaceAll("\\[lastname_el\\]", u.getLastname("el"))
                        .replaceAll("\\[firstname_en\\]", u.getFirstname("en"))
                        .replaceAll("\\[lastname_en\\]", u.getLastname("en"))
                        .replaceAll("\\[evaluationLink\\]", "<a href=\"" + getEvaluationLink(u.getId()) + "\">Συμπλήρωση Ερωτηματολογίου</a>");
                if (sendNow) {
                    sendEmail(aToEmailAddr, aSubject, aBody);
                } else {
                    pushEmail(aToEmailAddr, aSubject, aBody);
                }
                u.setLoginEmailSent(Boolean.TRUE);
                logger.log(Level.FINE, "Sent sendEvaluationEmail email to user with id " + u.getId() + " " + getEvaluationLink(u.getId()));
            } else {
                logger.log(Level.FINE, "Skipped sendEvaluationEmail for user with id " + u.getId());
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Failed to send sendEvaluationEmail");
        }
    }

    private String getEvaluationLink(Long userId) throws UnsupportedEncodingException {
        return conf.getString("rest.url") + "/evaluation/" + EvaluationService.encryptID(userId);
    }
}

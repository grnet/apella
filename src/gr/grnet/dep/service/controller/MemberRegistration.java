package gr.grnet.dep.service.controller;

import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

@Startup
// The @Stateful annotation eliminates the need for manual transaction demarcation
@Singleton
// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation

public class MemberRegistration {

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<User> memberEventSrc;

	private User newUser;

	@Produces
	@Named
	public User getNewMember() {
		return newUser;
	}

	public void register() throws Exception {
		log.info("Registering " + newUser.getUsername());
		em.persist(newUser);
		memberEventSrc.fire(newUser);
		initNewUser();
	}

	@PostConstruct
	public void initNewUser() {
		/*Department department = em.find(Department.class, 1L);
		Subject subj = em.find(Subject.class, 1L);
		User newUser = new User();
		newUser.getBasicInfo().setFirstname("Aggelos");
		newUser.getBasicInfo().setLastname("Lenis");
		newUser.setUsername("anglen");
		newUser.setRegistrationDate(new Date());
		newUser.getContactInfo().setEmail("anglen@ebs.gr");
		newUser.getContactInfo().setPhoneNumber("2106747631");
		newUser.getContactInfo().getAddress().setStreet("Eftalioti");
		newUser.getContactInfo().getAddress().setNumber("3");
		newUser.getContactInfo().getAddress().setZip("11525");
		ProfessorDomestic pd = new ProfessorDomestic();
		pd.setDepartment(department);
		Rank rank = em.find(Rank.class, 1L);
		pd.setRank(rank);
		pd.setSubject(subj);
		newUser.addRole(pd);
		Candidate can = new Candidate();
		newUser.addRole(can);
		em.persist(newUser);*/
		
		/*User user = em.find(User.class, 1L);
		Candidate can = new Candidate();
		user.addRole(can);
		Position pos = new Position();
		pos.setName("Front Desk Security");
		pos.setDescription("Kickboxing services");
		pos.setDepartment(department);
		pos.setSubject(subj);
		em.persist(pos);
		Candidacy ccy = new Candidacy();
		ccy.setDate(new Date());
		ccy.setPosition(pos);
		can.addCandidacy(ccy);
		em.persist(user);*/
	}

}

package gr.grnet.dep.service.model;


import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Entity
@XmlRootElement
public class CandidacyStatus {

    public enum ACTIONS {SUBMIT, WITHDRAW}

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Enumerated(EnumType.STRING)
    private ACTIONS action;

    @ManyToOne
    private Candidacy candidacy;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ACTIONS getAction() {
        return action;
    }

    public void setAction(ACTIONS action) {
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidacy getCandidacy() {
        return candidacy;
    }

    public void setCandidacy(Candidacy candidacy) {
        this.candidacy = candidacy;
    }

}

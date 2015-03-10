package gr.grnet.dep.service.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

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

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Enumerated(EnumType.STRING)
    private ACTIONS action;

    @JsonIgnore
    @ManyToOne
    private Candidacy candidacy;

	@JsonSerialize(using = SimpleDateSerializer.class)
    public Date getDate() {
        return date;
    }

	@JsonDeserialize(using = SimpleDateDeserializer.class)
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

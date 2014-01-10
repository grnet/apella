package gr.grnet.dep.service.model;

import java.util.Date;

public class JiraComment {

	private Date date;

	private String comment;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}

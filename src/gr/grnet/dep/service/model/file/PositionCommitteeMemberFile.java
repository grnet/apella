package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.PositionCommitteeMember;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("PositionCommitteeMemberFile")
public class PositionCommitteeMemberFile extends FileHeader {

	private static final long serialVersionUID = -3461035817248854894L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			// TODO:
			put(FileType.TEKMIRIOSI_AKSIOLOGITI, 1);
		}
	});

	@ManyToOne
	private PositionCommitteeMember positionCommitteeMember;

	public PositionCommitteeMember getPositionCommitteeMember() {
		return positionCommitteeMember;
	}

	public void setPositionCommitteeMember(PositionCommitteeMember positionCommitteeMember) {
		this.positionCommitteeMember = positionCommitteeMember;
	}

}

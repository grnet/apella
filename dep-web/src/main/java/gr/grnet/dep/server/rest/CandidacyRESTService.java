package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.*;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidacy.EvaluatorCandidacyView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/candidacy")
public class CandidacyRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	private CandidacyService candidacyService;

	@EJB
	private CandidateService candidateService;

	@EJB
	private PositionService positionService;

	@EJB
	private RegisterService registerService;

	@EJB
	private PositionCandidaciesService positionCandidaciesService;


	/**
	 * Get Candidacy by it's ID
	 *
	 * @param authToken The Authentication Token
	 * @param id        The Id of the Candidacy
	 * @returnWrapped gr.grnet.dep.service.model.Candidacy
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9]+}")
	public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// find candidacy
			Candidacy candidacy = candidacyService.getCandidacy(id, false);
			Candidate candidate = candidacy.getCandidate();

			// Full Access ADMINISTRATOR, MINISTRY, ISNSTITUTION, OWNER
			if (loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) ||
					loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
					loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) ||
					loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) ||
					candidate.getUser().getId().equals(loggedOn.getId())) {
				// Full Access
				candidacy.getCandidacyEvalutionsDueDate(); // Load this to avoid lazy exception
				candidacy.setCanAddEvaluators(candidacyService.canAddEvaluators(candidacy));
				candidacy.setNominationCommitteeConverged(candidacyService.hasNominationCommitteeConverged(candidacy));
				return toJSON(candidacy, DetailedCandidacyView.class);
			}
			// Medium Access COMMITTEE MEMBER, EVALUATOR
			if ((candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) ||
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn))) {
				// Medium (without ContactInformation)
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			// Medium Access OTHER CANDIDATE if isOpenToOtherCandidates
			if (candidacy.isOpenToOtherCandidates() && candidacy.getCandidacies().containsCandidate(loggedOn)) {
				// Medium (without ContactInformation)
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			// Medium Access CANDIDACY EVALUATOR
			if (candidacy.containsEvaluator(loggedOn)) {
				// Medium (without ContactInformation)
				return toJSON(candidacy, EvaluatorCandidacyView.class);
			}
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Creates a new Candidacy, not finalized
	 *
	 * @param authToken The Authentication Token
	 * @param candidacy
	 * @return The new candidacy, with ID
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 403 X-Error-Code: wrong.position.candidacies.openingDate
	 * @HTTP 403 X-Error-Code: wrong.position.candidacies.closingDate
	 * @HTTP 404 X-Error-Code: wrong.candidate.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@POST
	@JsonView({DetailedCandidacyView.class})
	public Candidacy create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		Date now = new Date();
		try {
			// get candidate
			Candidate candidate = candidateService.getCandidate(candidacy.getCandidate().getId());

			// Validate
			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			// find position
			Position position = positionService.getPosition(candidacy.getCandidacies().getPosition().getId());

			if (DateUtil.compareDates(position.getPhase().getCandidacies().getOpeningDate(), now) > 0) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.openingDate");
			}
			if (DateUtil.compareDates(position.getPhase().getCandidacies().getClosingDate(), now) < 0) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.closingDate");
			}
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.status");
			}
			// create or update candidacy
			candidacy = candidacyService.createCandidacy(candidacy, position, candidate);

			return candidacy;

		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	/**
	 * Saves and finalizes candidacy
	 *
	 * @param authToken The Authentication Token
	 * @param id
	 * @param candidacy
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: max.evaluators.exceeded
	 */
	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// get existing candidacy
			Candidacy existingCandidacy = candidacyService.getCandidacy(id, false);

			if (!existingCandidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			existingCandidacy = candidacyService.updateCandidacy(existingCandidacy.getId(), candidacy);

			return existingCandidacy;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	/**
	 * Deletes the submitted candidacy
	 *
	 * @param authToken The Authentication Token
	 * @param id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: wrong.position.status.committee.converged
	 */
	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			final Candidacy existingCandidacy = candidacyService.getCandidacy(id,false);

			Candidate candidate = existingCandidacy.getCandidate();

			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			candidacyService.deleteCandidacy(existingCandidacy.getId());

		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	/*********************
	 * Register Members **
	 *********************/

	/**
	 * Returns the list of Register Members selectable
	 * as Evaluators for this Candidacy
	 *
	 * @param authToken   The Authentication Token
	 * @param positionId
	 * @param candidacyId
	 * @returnWrapped gr.grnet.dep.service.model.RegisterMember Array
	 */
	@GET
	@Path("/{id:[0-9]+}/register")
	@JsonView({CandidacyEvaluator.CandidacyEvaluatorView.class})
	public Collection<RegisterMember> getCandidacyRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("id") Long candidacyId) {
		try {
			User loggedOn = getLoggedOn(authToken);

			final Candidacy existingCandidacy = candidacyService.getCandidacy(candidacyId, false);

			Candidate candidate = existingCandidacy.getCandidate();
			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return empty response while committee is not defined
			if (!candidacyService.canAddEvaluators(existingCandidacy)) {
				return new ArrayList<>();
			}
			// Prepare Data for Query
			Institution institution = existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution();
			Set<Long> committeeMemberIds = new HashSet<Long>();
			if (existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee() != null) {
				PositionCommittee committee = existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee();
				for (PositionCommitteeMember member : committee.getMembers()) {
					committeeMemberIds.add(member.getRegisterMember().getId());
				}
			}
			if (committeeMemberIds.isEmpty()) {
				// This should not happen, but just to avoid exceptions in case it does
				return new ArrayList<>();
			}
			// get register members
			List<RegisterMember> registerMembers = registerService.getRegisterMembers(institution.getId(), committeeMemberIds, false);

			// Return result
			return registerMembers;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}


    @POST
    @Path("/{id:[0-9]+}/register/search")
    @JsonView({CandidacyEvaluator.CandidacyEvaluatorView.class})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public SearchData<RegisterMember> search(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) {
        User loggedOn = getLoggedOn(authToken);

		try {
			final Candidacy existingCandidacy = candidacyService.getCandidacy(candidacyId, false);

			Candidate candidate = existingCandidacy.getCandidate();
			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// get the fetched data
			SearchData<RegisterMember> result = candidacyService.search(candidacyId, request);
			// Return result
			return result;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
    }


	/*******************************
	 * Snapshot File Functions *****
	 *******************************/

	/**
	 * Returns the list of snapshot files of this candidacy
	 * (copied from profile)
	 *
	 * @param authToken   The Authentication Token
	 * @param candidacyId
	 * @return Array of gr.grnet.dep.service.model.file.CandidateFile Array
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}/snapshot/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<CandidateFile> getSnapshotFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get candidacy
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, true);

			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			return candidacy.getSnapshotFiles();
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns the file description with the given id
	 *
	 * @param authToken   The authentication Token
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public CandidateFile getSnapshotFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get candidacy with the snapshot files
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, true);

			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			for (CandidateFile file : candidacy.getSnapshotFiles()) {
				if (file.getId().equals(fileId)) {
					return file;
				}
			}
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns the actual file body (binary)
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param bodyId
	 * @return file body
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getSnapshotFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, true);
			// Validate:
			if (candidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			for (CandidateFile file : candidacy.getSnapshotFiles()) {
				if (file.getId().equals(fileId) && file.getCurrentBody().getId().equals(bodyId)) {
					return sendFileBody(file.getCurrentBody());
				}
			}
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/*******************************
	 * Candidacy File Functions ****
	 *******************************/

	/**
	 * Returns files specific to this candidacy
	 *
	 * @param authToken   The authentication Token
	 * @param candidacyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<CandidacyFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);

			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			return FileHeader.filterDeleted(candidacy.getFiles());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns file description of a file in candidacy
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public CandidacyFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);

			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			for (CandidacyFile file : candidacy.getFiles()) {
				if (file.getId().equals(fileId) && !file.isDeleted()) {
					return file;
				}
			}
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns the actual file body (binary)
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);

			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			for (CandidacyFile file : candidacy.getFiles()) {
				if (file.getId().equals(fileId) && !file.isDeleted()) {
					for (FileBody fb : file.getBodies()) {
						if (fb.getId().equals(bodyId)) {
							return sendFileBody(fb);
						}
					}
				}
			}
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Uploads a new file
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{id:[0-9][0-9]*}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);
			// Validate:
			if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Parse Request
			List<FileItem> fileItems = readMultipartFormData(request);
			// Find required type:
			FileType type = null;
			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
					type = FileType.valueOf(fileItem.getString("UTF-8"));
					break;
				}
			}
			if (type == null) {
				throw new RestException(Status.BAD_REQUEST, "missing.file.type");
			}
			// get candidacy file
			CandidacyFile candidacyFile = candidacyService.createFile(type, candidacyId, fileItems, loggedOn);

			return toJSON(candidacyFile, SimpleFileHeaderView.class);

		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Uploads and updates an existing file
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);
			// Validate:
			if (candidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Parse Request
			List<FileItem> fileItems = readMultipartFormData(request);
			// Find required type:
			FileType type = null;
			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
					type = FileType.valueOf(fileItem.getString("UTF-8"));
					break;
				}
			}
			if (type == null) {
				throw new RestException(Status.BAD_REQUEST, "missing.file.type");
			}
			// Update
			CandidacyFile candidacyFile = candidacyService.updateFile(type, candidacyId, fileItems, loggedOn, fileId);

			// TODO fix return
			return candidacyFile.toString();
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Removes the file body
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long candidacyId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);

			if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			CandidacyFile candidacyFile = null;
			for (CandidacyFile file : candidacy.getFiles()) {
				if (file.getId().equals(fileId) && !file.isDeleted()) {
					candidacyFile = file;
					break;
				}
			}
			if (candidacyFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			FileType type = candidacyFile.getType();

			//delete file
			candidacyService.deleteFile(type, candidacyId, candidacyFile);

			return Response.noContent().build();

		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	/*******************************
	 * Evaluation File Functions ***
	 *******************************/

	/**
	 * Returns the list of evaluation files of this candidacy
	 *
	 * @param authToken   The Authentication Token
	 * @param candidacyId
	 * @return Array of gr.grnet.dep.service.model.file.PositionCandidaciesFile
	 * Array
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}/evaluation/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCandidaciesFile> getEvaluationFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);

			PositionCandidacies existingCandidacies = candidacy.getCandidacies();
			if (existingCandidacies == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
			}
			// Validate:
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			// Search
			List<PositionCandidaciesFile> result = positionCandidaciesService.getPositionCandidaciesFiles(existingCandidacies.getId(), candidacy.getId());

			// Return Result
			return result;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns the file description with the given id
	 *
	 * @param authToken   The authentication Token
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{id:[0-9]+}/evaluation/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCandidaciesFile getEvaluationFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);
			// Validate:
			if (candidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			PositionCandidacies existingCandidacies = candidacy.getCandidacies();
			if (existingCandidacies == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
			}
			// Validate:
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Find File
			Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
			for (PositionCandidaciesFile file : files) {
				if (file.getId().equals(fileId)) {
					return file;
				}
			}
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns the actual file body (binary)
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param bodyId
	 * @return file body
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/evaluation/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getEvaluationFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);
			// Validate:
			if (candidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			PositionCandidacies existingCandidacies = candidacy.getCandidacies();
			if (existingCandidacies == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
			}
			// Validate:
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return Result
			Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
			for (PositionCandidaciesFile file : files) {
				if (file.getId().equals(fileId) && file.getCurrentBody().getId().equals(bodyId)) {
					return sendFileBody(file.getCurrentBody());
				}
			}
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	@GET
	@Path("/incompletecandidacies")
	@JsonView({MediumCandidacyView.class})
	public Collection<Candidacy> getIncompleteCandidacies(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Administrator admin = (Administrator) loggedOn.getActiveRole(RoleDiscriminator.ADMINISTRATOR);
		if (!admin.isSuperAdministrator()) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		List<Candidacy> retv = candidacyService.getIncompleteCandidacies();

		return retv;
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@JsonView({Candidacy.DetailedCandidacyView.class})
	@Path("/submitcandidacy")
	public Candidacy createCandidacy(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Candidacy candidacy) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// Authenticate
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// submit candidacy
			candidacy = candidacyService.submitCandidacy(candidacy.getId());

			return candidacy;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id:[0-9]+}/statushistory")
	public Response getCandidacyStatusHistoryList(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			Candidacy candidacy = candidacyService.getCandidacy(candidacyId, false);
			if (candidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			// Validate:
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
					(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.containsEvaluator(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
					!candidacy.isOpenToOtherCandidates() &&
					!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			List<CandidacyStatus> statusList = candidacyService.getCandidacyStatus(candidacyId);

			return Response.ok(statusList).build();
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

}

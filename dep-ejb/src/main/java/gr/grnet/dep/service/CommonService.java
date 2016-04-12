package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;


    /******************************
     * Candidacy Snapshot *********
     ******************************/

    /**
     * Check that a (possible) Candidacy passes some basic checks before
     * creation / update. Takes into account all roles
     *
     * @param candidacy
     * @param candidate
     */
    protected void validateCandidacy(Candidacy candidacy, Candidate candidate, boolean isNew) throws ValidationException {
        if (!candidate.getStatus().equals(Role.RoleStatus.ACTIVE)) {
           // throw new RestException(Response.Status.CONFLICT, "validation.candidacy.inactive.role");
            throw new ValidationException("validation.candidacy.inactive.role");
        }
        if (isNew) {
            if (FileHeader.filter(candidate.getFiles(), FileType.DIMOSIEYSI).size() == 0) {
               // throw new RestException(Response.Status.CONFLICT, "validation.candidacy.no.dimosieysi");
                throw new ValidationException("validation.candidacy.no.dimosieysi");
            }
            if (FileHeader.filter(candidate.getFiles(), FileType.BIOGRAFIKO).size() == 0) {
                //throw new RestException(Response.Status.CONFLICT, "validation.candidacy.no.cv");
                throw new ValidationException("validation.candidacy.no.cv");
            }
            if (FileHeader.filter(candidate.getFiles(), FileType.PTYXIO).size() == 0) {
               // throw new RestException(Response.Status.CONFLICT, "validation.candidacy.no.ptyxio");
                throw new ValidationException("validation.candidacy.no.ptyxio");
            }
        } else {
            for (FileHeader fh : candidacy.getSnapshotFiles()) {
                log.info(fh.getId() + " " + fh.getType() + " " + fh.isDeleted());
            }

            if (FileHeader.filterIncludingDeleted(candidacy.getSnapshotFiles(), FileType.DIMOSIEYSI).size() == 0) {
               // throw new RestException(Response.Status.CONFLICT, "validation.candidacy.no.dimosieysi");
                throw new ValidationException("validation.candidacy.no.dimosieysi");
            }
            if (FileHeader.filterIncludingDeleted(candidacy.getSnapshotFiles(), FileType.BIOGRAFIKO).size() == 0) {
               // throw new RestException(Response.Status.CONFLICT, "validation.candidacy.no.cv");
                throw new ValidationException("validation.candidacy.no.cv");
            }
            if (FileHeader.filterIncludingDeleted(candidacy.getSnapshotFiles(), FileType.PTYXIO).size() == 0) {
               // throw new RestException(Response.Status.CONFLICT, "validation.candidacy.no.ptyxio");
                throw new ValidationException("validation.candidacy.no.ptyxio");
            }
        }
    }

    /**
     * Hold on to a snapshot of candidate's details in given candidacy. Takes
     * into account all roles
     *
     * @param candidacy
     * @param candidate
     */
    protected void updateSnapshot(Candidacy candidacy, Candidate candidate) {
        candidacy.clearSnapshot();
        candidacy.updateSnapshot(candidate);
        User user = candidate.getUser();
        ProfessorDomestic professorDomestic = (ProfessorDomestic) user.getActiveRole(Role.RoleDiscriminator.PROFESSOR_DOMESTIC);
        ProfessorForeign professorForeign = (ProfessorForeign) user.getActiveRole(Role.RoleDiscriminator.PROFESSOR_FOREIGN);
        if (professorDomestic != null) {
            candidacy.updateSnapshot(professorDomestic);
        } else if (professorForeign != null) {
            candidacy.updateSnapshot(professorForeign);
        }
    }

    protected void updateOpenCandidacies(Candidate candidate) throws ValidationException {
        // Get Open Candidacies
        List<Candidacy> openCandidacies = em.createQuery(
                "from Candidacy c where c.candidate = :candidate " +
                        "and c.candidacies.closingDate >= :now", Candidacy.class)
                .setParameter("candidate", candidate)
                .setParameter("now", new Date())
                .getResultList();

        // Validate all candidacies
        for (Candidacy candidacy : openCandidacies) {
            validateCandidacy(candidacy, candidate, true);
        }
        // If all valid, update
        for (Candidacy candidacy : openCandidacies) {
            updateSnapshot(candidacy, candidate);
        }
    }

    public List<FileItem> readMultipartFormData(HttpServletRequest request) throws ValidationException {
        try {
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
            ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
            servletFileUpload.setFileSizeMax(50 * 1024 * 1024); // 54.428.800
            servletFileUpload.setSizeMax(50 * 1024 * 1024);
            servletFileUpload.setHeaderEncoding("UTF-8");
            @SuppressWarnings("unchecked")
            List<FileItem> fileItems = servletFileUpload.parseRequest(request);
            return fileItems;
        } catch (FileUploadBase.SizeLimitExceededException e) {
           // throw new RestException(Response.Status.BAD_REQUEST, "file.size.exceeded");
            throw new ValidationException("file.size.exceeded");
        } catch (FileUploadException e) {
            log.log(Level.SEVERE, "Error encountered while parsing the request", e);
            //throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "generic");
            throw new ValidationException("generic");
        }
    }


    /**
     * ***************************
     * File Functions *************
     * ****************************
     */

    public File saveFile(User loggedOn, List<FileItem> fileItems, FileHeader header) throws Exception {
        try {
            File file = null;
            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {
                    log.info("Incoming text data: '" + fileItem.getFieldName() + "'=" + fileItem.getString("UTF-8") + "\n");
                    if (fileItem.getFieldName().equals("type")) {
                        header.setType(FileType.valueOf(fileItem.getString("UTF-8")));
                    } else if (fileItem.getFieldName().equals("name")) {
                        header.setName(fileItem.getString("UTF-8"));
                    } else if (fileItem.getFieldName().equals("description")) {
                        header.setDescription(fileItem.getString("UTF-8"));
                    }
                } else {
                    FileBody body = new FileBody();
                    em.persist(body);
                    em.flush(); // Get an id

                    String filename = fileItem.getName();
                    String newFilename = suggestFilename(body.getId(), "upl", filename);
                    file = new File(WebConstants.FILES_PATH + newFilename);
                    fileItem.write(file);

                    // Now add metadata in database,
                    // up to now only an empty body is stored, no reference to header
                    String mimeType = fileItem.getContentType();
                    if (StringUtils.isEmpty(mimeType)
                            || "application/octet-stream".equals(mimeType)
                            || "application/download".equals(mimeType)
                            || "application/force-download".equals(mimeType)
                            || "octet/stream".equals(mimeType)
                            || "application/unknown".equals(mimeType)) {
                        body.setMimeType(identifyMimeType(filename));
                    } else {
                        body.setMimeType(mimeType);
                    }
                    body.setOriginalFilename(fileItem.getName());
                    body.setStoredFilePath(newFilename);
                    body.setFileSize(fileItem.getSize());
                    body.setDate(new Date());

                    header.addBody(body);
                    em.persist(header);
                    em.flush();
                }
            }

            return file;
        } catch (FileUploadException ex) {
            log.log(Level.SEVERE, "Error encountered while parsing the request", ex);
           // throw new RestException(Status.INTERNAL_SERVER_ERROR, "generic");
            throw new Exception("generic");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error encountered while uploading file", e);
            //throw new RestException(Status.INTERNAL_SERVER_ERROR, "generic");
            throw new Exception("generic");
        }
    }


    /**
     * Check FileType to upload agrees with max number and direct caller.
     *
     * @param fileTypes Map<FileType, Integer>
     * @param type      type to check
     * @param files     existing files
     * @return null to continue and create file; existing file to switch to
     * update
     * @throws ValidationException if wrong file type
     */
    protected <T extends FileHeader> T checkNumberOfFileTypes(Map<FileType, Integer> fileTypes, FileType type, Set<T> files) throws ValidationException {
        if (!fileTypes.containsKey(type)) {
            // throw new RestException(Response.Status.CONFLICT, "wrong.file.type");
            throw new ValidationException("wrong.file.type");
        }
        Set<T> existingFiles = null;
        if (fileTypes.get(type) == 1) {
            existingFiles = FileHeader.filterIncludingDeleted(files, type);
            if (existingFiles.size() >= 1) {
                T existingFile = existingFiles.iterator().next();
                if (existingFile.isDeleted()) {
                    // Reuse!
                    existingFile.undelete();
                    return existingFile;
                } else {
                    // throw new RestException(Response.Status.CONFLICT, "wrong.file.type");
                    throw new ValidationException("wrong.file.type");
                }
            }
        } else {
            existingFiles = FileHeader.filter(files, type);
            if (existingFiles.size() >= fileTypes.get(type))
                //throw new RestException(Response.Status.CONFLICT, "wrong.file.type");
                throw new ValidationException("wrong.file.type");
        }
        return null;
    }

    <T extends FileHeader> T _updateFile(User loggedOn, List<FileItem> fileItems, T file) throws Exception {
        try {
            saveFile(loggedOn, fileItems, file);
            em.flush();
            file.getBodies().size();

            //TODO remove null
            return null;

            //return toJSON(file, FileHeader.SimpleFileHeaderView.class);
        } catch (PersistenceException e) {
            log.log(Level.WARNING, e.getMessage(), e);
           // sc.setRollbackOnly();
           // throw new RestException(Status.BAD_REQUEST, "persistence.exception");
            throw new ValidationException("");
        }
    }

    /**
     * Helper method for identifying mime type by examining the filename
     * extension
     *
     * @param filename
     * @return the mime type
     */
    private String identifyMimeType(String filename) {
        if (filename.indexOf('.') != -1) {
            String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ENGLISH);
            if (".doc".equals(extension))
                return "application/msword";
            else if (".xls".equals(extension))
                return "application/vnd.ms-excel";
            else if (".ppt".equals(extension))
                return "application/vnd.ms-powerpoint";
            else if (".xlsx".equals(extension))
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            else if (".pdf".equals(extension))
                return "application/pdf";
            else if (".gif".equals(extension))
                return "image/gif";
            else if (".jpg".equals(extension) || ".jpeg".equals(extension) || ".jpe".equals(extension))
                return "image/jpeg";
            else if (".tiff".equals(extension) || ".tif".equals(extension))
                return "image/tiff";
            else if (".png".equals(extension))
                return "image/png";
            else if (".bmp".equals(extension))
                return "image/bmp";
        }
        // when all else fails assign the default mime type
        return WebConstants.DEFAULT_MIME_TYPE;
    }

    protected String suggestFilename(Long id, String prefix, String originalName) throws IOException {
        int dotPoint = originalName.lastIndexOf('.');
        String extension = "";
        if (dotPoint > -1) {
            extension = originalName.substring(dotPoint);
        }
        String subPath = getSubdirForId(id);
        // Create if it does not exist
        new File(WebConstants.FILES_PATH + File.separator + subPath).mkdirs();

        return subPath + File.separator + prefix + "-" + id + extension;
    }

    private String getSubdirForId(Long id) {
        long subdir = id % 100;
        return (subdir < 10) ? "0" + subdir : String.valueOf(subdir);
    }

    /**
     * Delete as many bodies and physical files of given FileHeader as possible.
     * If no bodies are left, deletes header too.
     * If a body that is in use is found, deletions stop there:
     * The body and previous ones are left untouched, and
     * FileHeader is just marked as deleted.
     * No Exception is thrown.
     *
     * @param fh FileHeader
     */
    protected <T extends FileHeader> T deleteAsMuchAsPossible(T fh) {
        List<FileBody> fileBodies = fh.getBodies();
        for (int i = fileBodies.size() - 1; i >= 0; i--) {
            try {
                File file = deleteFileBody(fh);
                file.delete();
            } catch (ValidationException e) {
                fh.delete();
                return fh;
            }
        }
        return null;
    }

    /**
     * Delete given FileHeader and all bodies and physical files.
     *
     * @param fh FileHeader
     * @throws ValidationException (Status.CONFLICT) if FileHeader cannot be deleted
     *                       because some body is in use
     */
    protected void deleteCompletely(FileHeader fh) throws ValidationException {
        List<FileBody> fileBodies = fh.getBodies();
        for (int i = fileBodies.size() - 1; i >= 0; i--) {
            File file = deleteFileBody(fh);
            file.delete();
        }
    }

    /**
     * Deletes the last body of given file, if possible.
     * If no bodies are left, deletes header too.
     *
     * @param fh
     * @return
     * @throws ValidationException (Status.CONFLICT) if body cannot be deleted because
     *                       it is in use
     */
    protected <T extends FileHeader> File deleteFileBody(T fh) throws ValidationException {
        int size = fh.getBodies().size();
        // Delete the file itself, if possible.
        FileBody fb = fh.getCurrentBody();

        // Validate:
        try {
            em.createQuery("select c.id from Candidacy c " +
                    "left join c.snapshot.files fb " +
                    "where fb.id = :bodyId", Long.class)
                    .setParameter("bodyId", fb.getId())
                    .setMaxResults(1)
                    .getSingleResult();
            log.log(Level.INFO, "Could not delete FileBody id=" + fb.getId() + ". Constraint violation. ");
            //throw new RestException(Response.Status.CONFLICT, "file.in.use");
            throw new ValidationException("file.in.use");
        } catch (NoResultException e) {
        }
        // Reference physical file
        String fullPath = WebConstants.FILES_PATH + File.separator + fb.getStoredFilePath();
        File file = new File(fullPath);
        // Delete
        fh.getBodies().remove(size - 1);
        fh.setCurrentBody(null);
        em.remove(fb);
        if (size > 1) {
            fh.setCurrentBody(fh.getBodies().get(size - 2));
        } else {
            em.remove(fh);
        }
        //Return physical file, should be deleted by caller of function
        return file;
    }







}

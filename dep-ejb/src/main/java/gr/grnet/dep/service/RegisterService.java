package gr.grnet.dep.service;

import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Stateless
public class RegisterService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    private EntityManager em;

    @Inject
    private Logger log;


    public List<RegisterMember> getRegisterMembers(Long institutionId, Set<Long> newRegisterMemberIds, boolean registerMembersIncluded) {

        String registerMemberIdsClause;
        if (registerMembersIncluded) {
            registerMemberIdsClause = "and m.id in (:registerMembersIds)";
        } else {
            registerMemberIdsClause = "and m.id not in (:registerMembersIds)";
        }

        List<RegisterMember> registerMembers = em.createQuery(
                "select distinct m from Register r " +
                        "join r.members m " +
                        "where r.permanent = true " +
                        "and r.institution.id = :institutionId " +
                        "and m.deleted = false " +
                        "and m.professor.status = :status " +
                        registerMemberIdsClause, RegisterMember.class)
                .setParameter("institutionId", institutionId)
                .setParameter("status", Role.RoleStatus.ACTIVE)
                .setParameter("registerMembersIds", newRegisterMemberIds)
                .getResultList();

        return registerMembers;
    }


}

package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Position;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@Stateless
public class PositionService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public Position getPosition(Long id) throws NotFoundException {

        Position position = em.find(Position.class, id);

        if (position == null) {
            throw new NotFoundException("wrong.position.id");
        }

        return position;
    }


}

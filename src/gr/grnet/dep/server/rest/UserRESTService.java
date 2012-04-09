package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.data.DEPService;
import gr.grnet.dep.service.model.User;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * JAX-RS Example
 * 
 * This class produces a RESTful service to read the contents of the users table.
 */
@Path("/users")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserRESTService {
   @PersistenceContext(unitName="depdb", type = PersistenceContextType.EXTENDED)
	//@PersistenceContext(unitName="depdb")
   private EntityManager em;
   
   @EJB
   private DEPService service;
   
   @Inject
   private Logger log;


   @GET
   public List<User> listAllUsers() {
	   Query qry = em.createQuery("from User where active=true " +
					"order by lastname, firstname");
			return qry.getResultList();
      //return service.getUsers();
   }
   
   @GET
   @Path("/hello")
   @Produces(MediaType.TEXT_XML)
   public String hello() {
	  log.info("HelloService!");
      return "<hello>"+service.sayHello()+"</hello>";
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   public User getUserById(@PathParam("id") long id) {
	   User member = em.find(User.class, id);
      return member;
   }
}

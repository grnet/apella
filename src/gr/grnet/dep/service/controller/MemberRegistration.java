package gr.grnet.dep.service.controller;

import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.ProfessorDomestic;
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
      newUser = new User();
      newUser.setFirstname("Aggelos");
      newUser.setLastname("Lenis");
      newUser.setUsername("anglen");
      newUser.setEmail("anglen@ebs.gr");
      newUser.setPhoneNumber("2106747631");
      ProfessorDomestic pd = new ProfessorDomestic();
      Department department = em.find(Department.class, 1L);
      pd.setDepartment(department);
      newUser.addRole(pd);
      em.persist(newUser);
   }
   
}

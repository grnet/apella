package gr.grnet.dep.service.data;

import javax.ejb.Stateless;

@Stateless
public class HelloService {

	public String sayHello() {
        String message = "Hello";
        return message;
    }
	
}

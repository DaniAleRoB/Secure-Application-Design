package co.escuelaing.edu.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private SecureServiceCaller serviceCaller;

    @GetMapping("/")
    public String index() {
        return "Greetings from Login Service (HTTPS)!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Login Service! (requires authentication)";
    }

    @GetMapping("/call")
    public String callOther() {
        try {
            return "Response from Other Service: " + serviceCaller.callOtherService();
        } catch (Exception e) {
            return "Error calling Other Service: " + e.getMessage();
        }
    }
}

package co.escuelaing.edu.other;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OtherController {

    @GetMapping("/data")
    public String data() {
        return "Secure data from Other Service!";
    }
}

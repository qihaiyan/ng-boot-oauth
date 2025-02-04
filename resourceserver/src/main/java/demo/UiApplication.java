package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class UiApplication {

    @GetMapping("/messages")
    public String[] getMessages() {
        return new String[]{"Message 1", "Message 2", "Message 3"};
    }

    public static void main(String[] args) {
        SpringApplication.run(UiApplication.class, args);
    }
}

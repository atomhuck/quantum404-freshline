package ru.quantum404.freshline.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FreshLineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreshLineApplication.class, args);
    }
}

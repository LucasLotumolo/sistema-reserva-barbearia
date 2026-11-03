package br.ifsp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoAuthAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoAuthAppApplication.class, args);
    }

}

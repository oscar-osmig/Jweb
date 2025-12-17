package com.osmig.Jweb.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * JWeb Application Entry Point
 *
 * This is your main application class.
 * Routes are configured in Routes.java.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.osmig.Jweb")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

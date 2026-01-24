package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWebApplication;
import org.springframework.boot.SpringApplication;

/**
 * JWeb Application Entry Point
 */
@JWebApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

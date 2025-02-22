package com.openjfx.controllers;
import org.h2.tools.Server;

public class H2Server {
    private static Server webServer;

    public static void main(String[] args) {
        try {
            // Only start the web console, connect to existing TCP server
            Server webServer = Server.createWebServer(
                "-webPort", "8082",
                "-webAllowOthers",
                "-webDaemon"
            ).start();

            System.out.println("H2 Console available at: http://localhost:8082");
            System.out.println("Use these settings:");
            System.out.println("JDBC URL: jdbc:h2:tcp://localhost:9092/./data/database");
            System.out.println("User Name: thanos");

            // Keep the process running
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("H2 Admin error: " + e.getMessage());
        }
    }
}
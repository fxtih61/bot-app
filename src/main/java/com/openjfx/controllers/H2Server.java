package com.openjfx.controllers;
import org.h2.tools.Server;

public class H2Server {
    public static void main(String[] args) {
        try {
            // Starten des H2-Servers auf localhost:8082
            Server server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Web Console started at: " + server.getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


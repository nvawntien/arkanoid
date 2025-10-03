package com.game.arkanoid.services;

public class HelloService {

    public String buildWelcomeMessage(String name) {
        if (name == null || name.isBlank()) {
            return "Welcome, Guest!";
        }
        return "Welcome, " + name + "!";
    }
}

package io.sphynx.server.util;

public class Validator {
    public static boolean isValidAgentName(String agentName) {
        if (agentName == null || agentName.isEmpty()) {
            return true;
        }

        return !agentName.matches("^[a-zA-Z0-9_-]+$");
    }
}

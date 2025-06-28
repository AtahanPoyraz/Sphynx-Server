package io.sphynx.server.config.security;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Endpoints {
    private final Environment environment;

    @Autowired
    public Endpoints(Environment environment) {
        this.environment = environment;
    }

    private final List<String> common = List.of(
            "/api/v1/auth/**",
            "/api/v1/agent/activate"
    );

    private final List<String> development = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private final List<String> authenticated = List.of(
            "/api/v1/agent/**",
            "/api/v1/user/me"
    );

    private final List<String> admin = List.of(
            "/api/v1/user/**"
    );

    public String[] getCommonEndpoints() {
        List<String> endpoints = new ArrayList<>(common);
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            endpoints.addAll(development);
        }

        return endpoints.toArray(new String[0]);
    }

    public String[] getAuthenticatedEndpoints() {
        return authenticated.toArray(new String[0]);
    }

    public String[] getAdminEndpoints() {
        return admin.toArray(new String[0]);
    }
}

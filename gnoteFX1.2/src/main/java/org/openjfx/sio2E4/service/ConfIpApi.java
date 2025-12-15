package org.openjfx.sio2E4.service;

import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.*;
import java.lang.module.Configuration;

public class ConfIpApi {
    private ApiConfig api;

    // 2. Le getter doit être public
    public ApiConfig getApi() { return api; }
    public void setApi(ApiConfig api) { this.api = api; }

    // 3. La classe interne doit être publique ET statique
    public static class ApiConfig {
        private String base_url;

        // 4. Les getters et setters internes doivent être publics
        public String getBase_url() { return base_url; }
        public void setBase_url(String base_url) { this.base_url = base_url; }
    }
}


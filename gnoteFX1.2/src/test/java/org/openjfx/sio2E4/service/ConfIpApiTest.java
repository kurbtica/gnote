package org.openjfx.sio2E4.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfIpApiTest {

    @Test
    void testGettersAndSetters() {
        ConfIpApi conf = new ConfIpApi();
        ConfIpApi.ApiConfig apiConfig = new ConfIpApi.ApiConfig();
        
        apiConfig.setBase_url("http://localhost:9090");
        conf.setApi(apiConfig);

        assertNotNull(conf.getApi());
        assertEquals("http://localhost:9090", conf.getApi().getBase_url());
    }
}

package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.test.util.ReflectionTestUtils;

class HttpToHttpsRedirectConfigTest {

    private HttpToHttpsRedirectConfig config;

    @BeforeEach
    void setUp() {
        config = new HttpToHttpsRedirectConfig();
        ReflectionTestUtils.setField(config, "httpsPort", 8443);
    }

    @Test
    void webServerFactoryCustomizer_conSslHabilitadoYPuertoHttpValido_anadeElConector() {
        ReflectionTestUtils.setField(config, "sslEnabled", true);
        ReflectionTestUtils.setField(config, "httpPort", 8080);
        TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

        WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer = config.webServerFactoryCustomizer();
        customizer.customize(factory);

        ArgumentCaptor<Connector> captor = ArgumentCaptor.forClass(Connector.class);
        verify(factory).addAdditionalTomcatConnectors(captor.capture());
        assertEquals(8080, captor.getValue().getPort());
        assertEquals(8443, captor.getValue().getRedirectPort());
    }

    @Test
    void webServerFactoryCustomizer_conSslDeshabilitado_noAnadeElConector() {
        ReflectionTestUtils.setField(config, "sslEnabled", false);
        ReflectionTestUtils.setField(config, "httpPort", 8080);
        TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

        config.webServerFactoryCustomizer().customize(factory);

        verify(factory, never()).addAdditionalTomcatConnectors(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void webServerFactoryCustomizer_sinPuertoHttpConfigurado_noAnadeElConector() {
        ReflectionTestUtils.setField(config, "sslEnabled", true);
        ReflectionTestUtils.setField(config, "httpPort", 0);
        TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

        config.webServerFactoryCustomizer().customize(factory);

        verify(factory, never()).addAdditionalTomcatConnectors(org.mockito.ArgumentMatchers.any());
    }
}

package com.abhishek.ecommerce.config.tomcat;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardManager;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Disables Tomcat session persistence to prevent session deserialization errors.
 * Sessions are stored in-memory only and are not persisted to disk.
 */
@Configuration
@Profile({"dev", "test"})
public class TomcatSessionConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            // Disable session persistence
            Manager manager = context.getManager();
            if (manager instanceof StandardManager) {
                StandardManager standardManager = (StandardManager) manager;
                // Don't serialize sessions
                standardManager.setPathname(null);
            }
        });
    }
}

package com.abhishek.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "spring.application.name=ecommerce-backend-test",
    "server.port=8080",
    "app.jwt.access-secret=test-access-secret-key-for-testing-purposes-only",
    "app.jwt.refresh-secret=test-refresh-secret-key-for-testing-purposes-only",
    "app.jwt.access-token-expiration-ms=3600000",
    "app.jwt.refresh-token-expiration-ms=86400000",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.com.abhishek.ecommerce=INFO",
    "spring.mail.host=localhost",
    "spring.mail.port=1025",
    "spring.mail.username=test",
    "spring.mail.password=test",
    "spring.mail.debug=false"
})
class EcommerceBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}

package com.abhishek.ecommerce.config.openapi;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
// @OpenAPIDefinition(
//         info = @Info(
//                 title = "AbhiOnlineDukaan Backend API",
//                 version = "v1",
//                 description = """
//                 Modular monolithic AbhiOnlineDukaan backend.
//
//                 Security implemented:
//                 • JWT access token
//                 • Refresh token
//                 • OAuth2 login (Google)
//                 • Role-based authorization (ADMIN / USER)
//
//                 Notes:
//                 • Admin is a ROLE, not a separate entity
//                 • Admin users are bootstrapped internally
//                 • APIs are stateless (no sessions)
//                 """
//         ),
//         security = {
//                 @SecurityRequirement(name = "bearerAuth")
//         }
// )
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT Bearer token authentication"
)
// @SecurityScheme(
//         name = "oauth2",
//         type = SecuritySchemeType.OAUTH2,
//         flows = @OAuthFlows(
//                 authorizationCode = @OAuthFlow(
//                         authorizationUrl = "/oauth2/authorization/google",
//                         tokenUrl = "/login/oauth2/code/google",
//                         scopes = {
//                                 @OAuthScope(name = "openid", description = "OpenID"),
//                                 @OAuthScope(name = "profile", description = "Profile"),
//                                 @OAuthScope(name = "email", description = "Email")
//                         }
//                 )
//         )
// )
public class OpenApiConfig {
}
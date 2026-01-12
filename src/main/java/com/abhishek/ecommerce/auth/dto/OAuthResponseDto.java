package com.abhishek.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class OAuthResponseDto {
    private String email;
    private String token;
    private String message;

    public OAuthResponseDto(String email, String token, String message) {
        this.email = email;
        this.token = token;
        this.message = message;
    }

    public static OAuthResponseDtoBuilder builder() {
        return new OAuthResponseDtoBuilder();
    }

    public static class OAuthResponseDtoBuilder {
        private String email;
        private String token;
        private String message;

        public OAuthResponseDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public OAuthResponseDtoBuilder token(String token) {
            this.token = token;
            return this;
        }

        public OAuthResponseDtoBuilder message(String message) {
            this.message = message;
            return this;
        }

        public OAuthResponseDto build() {
            return new OAuthResponseDto(email, token, message);
        }
    }
}


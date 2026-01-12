package com.abhishek.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@Schema(description = "User registration response")
public class SignupResponseDto {

    @Schema(description = "User ID", example = "123")
    private Long id;

    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User role", example = "USER")
    private String role;

    public SignupResponseDto(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public static SignupResponseDtoBuilder builder() {
        return new SignupResponseDtoBuilder();
    }

    public static class SignupResponseDtoBuilder {
        private Long id;
        private String email;
        private String role;

        public SignupResponseDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SignupResponseDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public SignupResponseDtoBuilder role(String role) {
            this.role = role;
            return this;
        }

        public SignupResponseDto build() {
            SignupResponseDto dto = new SignupResponseDto(id, email, role);
            return dto;
        }
    }
}


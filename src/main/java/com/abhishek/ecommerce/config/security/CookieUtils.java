package com.abhishek.ecommerce.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final ObjectMapper objectMapper;

    public <T> String serialize(T object) {
        try {
            return Base64.getUrlEncoder()
                    .encodeToString(objectMapper.writeValueAsBytes(object));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize object to Base64", e);
        }
    }

    public <T> T deserialize(String cookieValue, Class<T> cls) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookieValue);
            return objectMapper.readValue(bytes, cls);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize Base64 cookie", e);
        }
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    public void addCookie(HttpServletResponse response,
                          String name,
                          String value,
                          int maxAge) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletRequest request,
                             HttpServletResponse response,
                             String name) {

        if (request.getCookies() == null) {
            return;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}

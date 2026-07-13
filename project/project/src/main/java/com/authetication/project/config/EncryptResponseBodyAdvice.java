package com.authetication.project.config;

import com.authetication.project.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@ControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Value("${app.security.aes-encryption-key}")
    private String secretKey;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return "AuthController".equals(returnType.getContainingClass().getSimpleName());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body == null || body instanceof String) {
            return body;
        }

        // Bypass CORS OPTIONS preflights
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            if ("OPTIONS".equalsIgnoreCase(servletRequest.getMethod())) {
                return body;
            }
        }

        try {
            // 1. Serialize target response objects to plain JSON text
            String jsonString = this.mapper.writeValueAsString(body);
            
            // 2. Encrypt text string
            String encryptedData = CryptoUtils.encrypt(jsonString, secretKey);
            
            // 🌟 3. Wrap inside a clean JSON dictionary response mapping object
            Map<String, String> responseWrapper = Map.of("data", encryptedData);
            
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return responseWrapper;
            
        } catch (Exception e) {
            System.err.println("❌ Outbound Encryption Advice Error: " + e.getMessage());
            return body;
        }
    }
}

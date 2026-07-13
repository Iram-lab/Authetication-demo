package com.authetication.project.config;

import com.authetication.project.utils.CryptoUtils;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class DecryptRequestFilter implements Filter {

    private final String secretKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public DecryptRequestFilter(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getServletPath();

        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && path.startsWith("/api/auth/")) {
            String rawJsonBody = httpRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            if (rawJsonBody != null && rawJsonBody.trim().startsWith("{")) {
                try {
                    // 1. Unpack outer production JSON object
                    Map<?, ?> jsonMap = mapper.readValue(rawJsonBody, Map.class);
                    String encryptedData = (String) jsonMap.get("data");

                    if (encryptedData != null) {
                        // 2. Perform AES Decryption
                        String decryptedJson = CryptoUtils.decrypt(encryptedData, secretKey);
                        byte[] decryptedBytes = decryptedJson.getBytes(StandardCharsets.UTF_8);

                        // 3. Rewrite wrapper details to normalize payload to plain JSON
                        httpRequest = new HttpServletRequestWrapper(httpRequest) {
                            @Override
                            public ServletInputStream getInputStream() {
                                return new CustomServletInputStream(decryptedBytes);
                            }
                            @Override
                            public BufferedReader getReader() {
                                return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(decryptedBytes), StandardCharsets.UTF_8));
                            }
                            @Override
                            public int getContentLength() { return decryptedBytes.length; }
                            @Override
                            public long getContentLengthLong() { return decryptedBytes.length; }
                            @Override
                            public String getContentType() { return "application/json"; }
                        };
                    }
                } catch (Exception e) {
                    System.err.println("❌ Filter Production Decryption Error: " + e.getMessage());
                }
            }
        }
        chain.doFilter(httpRequest, response);
    }

    private static class CustomServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;
        public CustomServletInputStream(byte[] bytes) { this.buffer = new ByteArrayInputStream(bytes); }
        @Override public int read() { return buffer.read(); }
        @Override public boolean isFinished() { return buffer.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener readListener) {}
    }
}

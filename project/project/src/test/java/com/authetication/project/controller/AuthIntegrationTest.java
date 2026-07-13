package com.authetication.project.controller;



import com.authetication.project.model.User;
import com.authetication.project.repository.UserRepository;
import com.authetication.project.utils.CryptoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 🌟 INJECT PROPERTIES INLINE HERE: No separate file required!
@SpringBootTest(properties = "app.security.aes-encryption-key=k9X#mP2vF!8zA5qW@7bN4cC9xZ1vB3nL")
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.security.aes-encryption-key}")
    private String secretKey;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    private String createEncryptedRequestBody(Object rawPayload) throws Exception {
        String rawJsonString = objectMapper.writeValueAsString(rawPayload);
        String encryptedData = CryptoUtils.encrypt(rawJsonString, secretKey);
        
        Map<String, String> angularJsonContainer = new HashMap<>();
        angularJsonContainer.put("data", encryptedData);
        
        return objectMapper.writeValueAsString(angularJsonContainer);
    }

    @Test
    public void testUserRegistrationAndResponseEncryption_Success() throws Exception {
        User mockSignupUser = new User();
        mockSignupUser.setName("John Doe");
        mockSignupUser.setEmail("john.doe@example.com");
        mockSignupUser.setPassword("SecurePass123!");

        String requestBody = createEncryptedRequestBody(mockSignupUser);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        assertNotNull(jsonResponse);
        
        Map<?, ?> wrappedResponse = objectMapper.readValue(jsonResponse, Map.class);
        String encryptedResponseData = (String) wrappedResponse.get("data");
        assertNotNull(encryptedResponseData);
        
        String plainResponseJson = CryptoUtils.decrypt(encryptedResponseData, secretKey);
        Map<?, ?> plainResponseMap = objectMapper.readValue(plainResponseJson, Map.class);
        
        assertNotNull(plainResponseMap.get("token"));
        assertEquals("Registration successful!", plainResponseMap.get("message"));
        assertTrue(userRepository.findByEmail("john.doe@example.com").isPresent());
    }
}

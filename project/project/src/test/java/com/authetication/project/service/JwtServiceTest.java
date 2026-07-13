package com.authetication.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    // Secret key parameter pool (Must hit bit length limits required for HMAC SHA-256 signatures)
    private final String mockJwtSecret = "dGhpc2lzeW91cmNhbm5vdGxlYWt0ZXN0c2VjcmV0a2V5MTIzNDU2Nzg=";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Injecting your custom properties configuration key dynamically using reflection utils
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", mockJwtSecret);
    }

    @Test
    public void testGenerateAndValidateToken_Success() {
        // Arrange: Setup standard spring security model block mapping profile rules
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());

        // Act: Generate token signatures
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        // Act: Extract metadata from verification profile 
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extractedUsername);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    public void testTokenValidation_Failure_UserMismatch() {
        // Arrange
        UserDetails user1 = new User("user1@example.com", "password", Collections.emptyList());
        UserDetails user2 = new User("user2@example.com", "password", Collections.emptyList());

        // Act
        String tokenUser1 = jwtService.generateToken(user1);

        // Assert: A token generated for user1 context profile must be invalid for user2 checking parameters
        assertFalse(jwtService.isTokenValid(tokenUser1, user2));
    }
}

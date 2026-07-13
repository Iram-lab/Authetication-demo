package com.authetication.project.utility;



import org.junit.jupiter.api.Test;

import com.authetication.project.utils.CryptoUtils;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {

    private final String testSecretKey = "k9X#mP2vF!8zA5qW@7bN4cC9xZ1vB3nL";

    @Test
    public void testEncryptionAndDecryptionMatch() throws Exception {
        String originalJsonPayload = "{\"email\":\"test@example.com\",\"password\":\"Secret123!\"}";

        String encryptedBase64 = CryptoUtils.encrypt(originalJsonPayload, testSecretKey);
        assertNotNull(encryptedBase64);
        assertFalse(encryptedBase64.contains("test@example.com"));

        String decryptedJsonResult = CryptoUtils.decrypt(encryptedBase64, testSecretKey);
        assertEquals(originalJsonPayload, decryptedJsonResult);
    }
}


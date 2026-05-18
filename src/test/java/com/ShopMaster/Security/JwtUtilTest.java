package com.ShopMaster.Security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class JwtUtilTest {

    @Test
    void generateAndExtractAndValidateToken() throws InterruptedException {
        JwtUtil jwtUtil = new JwtUtil();

        String username = "user1";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);

        String extracted = jwtUtil.extractUsername(token);
        assertEquals(username, extracted);

        boolean valid = jwtUtil.validateToken(token, username);
        assertTrue(valid);
    }

}

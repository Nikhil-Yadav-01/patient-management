package com.rudraksha.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class EncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void encode() {
        String hashed = passwordEncoder.encode("MySecret123!");
        System.out.println(hashed);
    }
}


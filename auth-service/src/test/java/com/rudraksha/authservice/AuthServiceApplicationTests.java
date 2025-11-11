package com.rudraksha.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void encode() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "MySecret123!";
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println(hashedPassword);
    }

}

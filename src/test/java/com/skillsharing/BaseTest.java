package com.skillsharing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsharing.config.TestConfig;
import com.skillsharing.model.User;
import com.skillsharing.security.TokenProvider;
import com.skillsharing.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TokenProvider tokenProvider;

    protected String authToken;
    protected User testUser;
    protected UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = createTestUser();
        userPrincipal = UserPrincipal.create(testUser);

        // Set up authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate auth token
        authToken = tokenProvider.createToken(authentication);
    }

    protected User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setProvider(User.AuthProvider.LOCAL);
        user.setProviderId("local");
        return user;
    }

    protected String getAuthHeader() {
        return "Bearer " + authToken;
    }

    protected String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

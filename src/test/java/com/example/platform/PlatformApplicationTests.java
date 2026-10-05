package com.example.platform;

import com.example.platform.dto.AuthRequest;
import com.example.platform.dto.PostRequest;
import com.example.platform.security.AesEncryptionService;
import com.example.platform.security.JwtService;
import com.example.platform.service.AuthService;
import com.example.platform.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlatformApplicationTests {

    @Autowired
    private AesEncryptionService aesEncryptionService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PostService postService;

    @Test
    void contextLoads() {
        assertNotNull(aesEncryptionService);
        assertNotNull(jwtService);
        assertNotNull(authService);
        assertNotNull(postService);
    }

    @Test
    void testAesEncryptionAndDecryption() {
        String secretData = "secret_oauth_api_key_2026_xyz";
        String encrypted = aesEncryptionService.encrypt(secretData);
        assertNotNull(encrypted);
        assertNotEquals(secretData, encrypted);

        String decrypted = aesEncryptionService.decrypt(encrypted);
        assertEquals(secretData, decrypted);
    }

    @Test
    void testAuthAndJwtGeneration() {
        var response = authService.login(new AuthRequest("admin", "admin123"));
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertTrue(jwtService.validateToken(response.getAccessToken()));
        assertEquals("admin", jwtService.extractUsername(response.getAccessToken()));
    }

    @Test
    void testPostCreationAndCaching() {
        PostRequest request = new PostRequest(
                "Automated Integration Test Post",
                "This is content verifying that post creation and validation succeed properly.",
                "TESTING",
                "test,integration"
        );
        var created = postService.createPost(request, "admin");
        assertNotNull(created.getId());
        assertEquals("Automated Integration Test Post", created.getTitle());

        var fetched = postService.getPostById(created.getId());
        assertEquals(created.getId(), fetched.getId());
    }
}

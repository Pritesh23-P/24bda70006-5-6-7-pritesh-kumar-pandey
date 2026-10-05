package com.example.platform.config;

import com.example.platform.dto.OAuthCredentialRequest;
import com.example.platform.model.Comment;
import com.example.platform.model.Post;
import com.example.platform.model.Role;
import com.example.platform.model.Schedule;
import com.example.platform.model.User;
import com.example.platform.repository.CommentRepository;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.RoleRepository;
import com.example.platform.repository.ScheduleRepository;
import com.example.platform.repository.UserRepository;
import com.example.platform.service.OAuthCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScheduleRepository scheduleRepository;
    private final OAuthCredentialService oauthCredentialService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PostRepository postRepository,
                           CommentRepository commentRepository,
                           ScheduleRepository scheduleRepository,
                           OAuthCredentialService oauthCredentialService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.scheduleRepository = scheduleRepository;
        this.oauthCredentialService = oauthCredentialService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Bootstrapping sample roles, users, posts, comments, schedules, and encrypted credentials...");

        // 1. Roles
        Role roleUser = roleRepository.save(new Role("ROLE_USER"));
        Role roleAdmin = roleRepository.save(new Role("ROLE_ADMIN"));

        // 2. Users
        User adminUser = new User("admin", "admin@platform.local", passwordEncoder.encode("admin123"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roleUser);
        adminRoles.add(roleAdmin);
        adminUser.setRoles(adminRoles);
        adminUser = userRepository.save(adminUser);

        User standardUser = new User("dev_user", "developer@platform.local", passwordEncoder.encode("user123"));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roleUser);
        standardUser.setRoles(userRoles);
        standardUser = userRepository.save(standardUser);

        // 3. Sample Posts
        String[][] samplePosts = {
                {"Designing Scalable REST APIs in Spring Boot", "Learn how to build maintainable, stateless RESTful APIs following standard HTTP verb semantics and layered architecture patterns.", "ENGINEERING", "springboot,rest,java,api"},
                {"Eliminating the N+1 Query Problem with JOIN FETCH", "In-depth breakdown of JPA query optimization, Hibernate batch fetching, and leveraging JOIN FETCH to avoid redundant database round-trips.", "DATABASE", "jpa,hibernate,performance,sql"},
                {"Stateless Security with JWT and Refresh Token Rotation", "Best practices for implementing JWT authentication, short-lived access tokens, secure refresh token rotation, and Spring Security 6.", "SECURITY", "security,jwt,auth,rbac"},
                {"In-Memory Caching Strategies with Caffeine and Redis", "How caching frequently queried read endpoints reduces database CPU load and brings response latencies down to sub-10 milliseconds.", "PERFORMANCE", "caching,caffeine,performance,optimization"},
                {"Securing Sensitive OAuth Tokens Using AES-256 GCM", "Practical guide to encrypting third-party API credentials, client secrets, and access tokens at rest using Java Cryptography Architecture.", "SECURITY", "crypto,aes,encryption,vault"},
                {"Global Exception Handling and Correlation ID Tracing", "Centralizing error responses with ControllerAdvice and distributing unique correlation IDs via SLF4J MDC for production observability.", "OBSERVABILITY", "logging,mdc,exceptions,monitoring"},
                {"Mastering Spring Data Pagination and Multi-Column Sorting", "Step-by-step tutorial on leveraging Spring Data Pageable, Slice, and Sort objects for responsive dashboard UI pagination.", "ENGINEERING", "springdata,pagination,sorting,crud"},
                {"Architecting Microservices with Spring Cloud and Docker", "A comprehensive guide on decomposing monoliths into containerized microservices communicating via asynchronous event streams.", "CLOUD", "microservices,cloud,docker,architecture"}
        };

        for (int i = 0; i < samplePosts.length; i++) {
            String[] p = samplePosts[i];
            User author = (i % 2 == 0) ? adminUser : standardUser;
            Post post = new Post(p[0], p[1], p[2], p[3], author);
            post.setViewCount(25L * (i + 1));
            Post savedPost = postRepository.save(post);

            // Add sample comments
            commentRepository.save(new Comment("Excellent guide with clear practical code snippets!", "alex_dev", savedPost));
            commentRepository.save(new Comment("Very helpful for my production system performance tuning.", "sarah_tech", savedPost));

            // Add sample schedule
            if (i < 3) {
                String[] platforms = {"TWITTER", "LINKEDIN", "FACEBOOK"};
                Schedule schedule = new Schedule(
                        savedPost,
                        platforms[i % platforms.length],
                        LocalDateTime.now().plusDays(i + 1).plusHours(2),
                        "Automated post scheduled for marketing campaign"
                );
                scheduleRepository.save(schedule);
            }
        }

        // 4. Encrypted OAuth Credentials (AES-256-GCM)
        OAuthCredentialRequest twitterCreds = new OAuthCredentialRequest(
                "TWITTER",
                "tw_app_client_882910",
                "sec_live_99837192847291827401",
                "oauth2_acc_tok_991823719827391827",
                "oauth2_ref_tok_881726354819203817"
        );
        oauthCredentialService.storeCredentials(twitterCreds, adminUser.getUsername());

        OAuthCredentialRequest linkedinCreds = new OAuthCredentialRequest(
                "LINKEDIN",
                "li_app_client_447219",
                "sec_live_55610293847561829304",
                "oauth2_acc_tok_447192837461928374",
                "oauth2_ref_tok_338192048571928374"
        );
        oauthCredentialService.storeCredentials(linkedinCreds, standardUser.getUsername());

        log.info("Initialization completed successfully. Admin account: admin / admin123 | User account: dev_user / user123");
    }
}

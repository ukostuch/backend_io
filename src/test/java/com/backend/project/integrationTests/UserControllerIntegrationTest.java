package com.backend.project.integrationTests;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.UserRepository;
import com.backend.project.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.backend.project.security.JWTGenerator;
import org.springframework.security.core.Authentication;
import java.util.Collections;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JWTGenerator jwtGenerator;

    @Test
    void registerUser_ValidData_ShouldReturnSuccess() throws Exception {
        userRepository.deleteByUsername("johndoe");
        String registerJson = """
            {
                "name": "John",
                "surname": "Doe",
                "username": "johndoe",
                "mail": "john.doe@example.com",
                "password": "Password123!",
                "salutation": "Mr",
                "country": "US"
            }
        """;
        mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Successfully registered user"));

        userRepository.deleteByUsername("johndoe");
        }

    @Test
    void registerUser_UsernameTaken_ShouldReturnConflict() throws Exception {
            UserEntity existingUser = new UserEntity("John", "Doe", "john.doe@example.com",
                    "johndoe", "Password123!", "Mr", "US");
            userRepository.save(existingUser);
            String registerJson = """
            {
                "name": "Jane",
                "surname": "Smith",
                "username": "johndoe",
                "mail": "jane.smith@example.com",
                "password": "Password123!",
                "salutation": "Ms",
                "country": "GB"
            }
        """;
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson))
                    .andExpect(status().isConflict())
                    .andExpect(content().string("Username johndoe is already taken"));
        userRepository.deleteByUsername("johndoe");
        }


    @Test
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
            userRepository.deleteByUsername("jane7");
            UserEntity existingUser = new UserEntity("Jane", "Smith", "jane.smith@example.com",
                    "jane7", "Password123!", "Ms", "UK");
            existingUser.setRoles(Collections.singletonList(roleRepository.findByName("USER").orElse(null)));
            userRepository.save(existingUser);
            Mockito.when(jwtGenerator.generateToken(Mockito.any(Authentication.class))).thenReturn("mock-jwt-token");
            Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            String loginJson = """
    {
        "username": "jane7",
        "password": "Password123!"
    }
""";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"));

            userRepository.deleteByUsername("jane7");
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
            String loginJson = """
            {
                "username": "invaliduser",
                "password": "wrongPassword"
            }
        """;
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid username or password"));
        }


    @Test
    void updateUser_ValidToken_ShouldUpdateUser() throws Exception {
            userRepository.deleteByUsername("admin_jane9");
            UserEntity user = new UserEntity("Jane", "Smith", "jane.smith@example.com",
                    "admin_jane9", "Password123!", "Ms", "GB");
            user.getRoles().add((new Roles("ADMIN")));
            userRepository.save(user);
            String updateJson = """
            {
                "name": "UpdatedName",
                "surname": "UpdatedSurname",
                "mail": "updated@example.com"
            }
        """;
            Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
            Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("admin_jane9");
            Mockito.when(jwtGenerator.getRolesFromJWT("mock-jwt-token")).thenReturn(Collections.singletonList("ADMIN"));
            mockMvc.perform(patch("/api/users/admin_jane9")
                            .header("Authorization", "Bearer " + "mock-jwt-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("UpdatedName"))
                    .andExpect(jsonPath("$.mail").value("updated@example.com"));
        userRepository.deleteByUsername("admin_jane9");
    }
}
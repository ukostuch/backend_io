package com.backend.project.integrationTests;


import com.backend.project.model.Faq;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.FaqRepository;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FaqControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private JWTGenerator jwtGenerator;

    @AfterEach
    void cleanUpTestFaqs() {
        faqRepository.deleteAll(faqRepository.findByQuestionContaining("Test-"));

        if (userRepository.existsByUsername("johndoe")) {
            userRepository.deleteByUsername("johndoe");
        }
        if (userRepository.existsByUsername("admin-johndoe")) {
            userRepository.deleteByUsername("admin-johndoe");
        }
    }

    @Test
    void getAllFaqs_ShouldReturnListOfFaqs_WithMostRecentIncluded() throws Exception {
        Faq faq1 = new Faq("Test-What is the return policy?", null, false);
        String id = faqRepository.save(faq1).getId();

        mockMvc.perform(get("/api/faqs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$[-1].question").value("Test-What is the return policy?"))
                .andExpect(jsonPath("$[-1].approved").value(false));

        faqRepository.deleteById(id);
    }

    @Test
    void askQuestion_ValidData_ShouldReturnSuccess_AsAuthenticatedUser() throws Exception {
        UserEntity user = new UserEntity("John", "Doe", "john.doe@example.com",
                "johndoe", "Password123!", "Mr", "US");
        userRepository.save(user);

        String questionJson = """
    {
        "question": "Test-What are the shipping options?"
    }
    """;

        Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
        Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("johndoe");

        mockMvc.perform(post("/api/faqs/ask")
                        .header("Authorization", "Bearer " + "mock-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Question Submitted successfully!\n Wait for our staff to provide an answer\n"));

        Optional<Faq> savedFaq = faqRepository.findByQuestion("Test-What are the shipping options?");
        assert savedFaq.isPresent();
        assertEquals("Test-What are the shipping options?", savedFaq.get().getQuestion());

        userRepository.deleteByUsername("johndoe");
        faqRepository.deleteById(savedFaq.get().getId());
    }


    @Test
    void askQuestion_NonAuthenticatedUser_ShouldReturnFailed() throws Exception {
        String questionJson = """
        {
            "question": "Test-What are the shipping options?"
        }
        """;

        mockMvc.perform(post("/api/faqs/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isUnauthorized());

        Optional<Faq> savedFaq = faqRepository.findByQuestion("Test-What are the shipping options?");
        assert savedFaq.isEmpty();
    }


    @Test
    void approveQuestion_ValidId_ShouldReturnSuccess() throws Exception {
        UserEntity user = new UserEntity("John", "Doe", "john.doe@example.com",
                "admin-johndoe", "Password123!", "Mr", "US");
        user.getRoles().add((new Roles("ADMIN")));
        userRepository.save(user);

        Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
        Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("admin-johndoe");


        Faq faq = new Faq("Test-Can I change my shipping address?", null, false);
        faq = faqRepository.save(faq);

        mockMvc.perform(put("/api/faqs/" + faq.getId() + "/approve")
                        .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk());

        Optional<Faq> updatedFaq = faqRepository.findById(faq.getId());
        assert updatedFaq.isPresent() && updatedFaq.get().isApproved();
        userRepository.deleteByUsername(user.getUsername());
        faqRepository.deleteById(faq.getId());
    }

    @Test
    void deleteFaq_ValidId_ShouldReturnSuccess() throws Exception {
        UserEntity user = new UserEntity("John", "Doe", "john.doe@example.com",
                "admin-johndoe", "Password123!", "Mr", "US");
        user.getRoles().add((new Roles("ADMIN")));
        userRepository.save(user);

        Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
        Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("admin-johndoe");


        Faq faq = new Faq("Test-Can I change my shipping address?", null, false);
        faq = faqRepository.save(faq);

        mockMvc.perform(delete("/api/faqs/" + faq.getId() + "/delete")
                        .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk());

        Optional<Faq> updatedFaq = faqRepository.findById(faq.getId());
        assert updatedFaq.isEmpty();

        userRepository.deleteByUsername(user.getUsername());
        faqRepository.deleteById(faq.getId());
    }

    @Test
    void updateFaq_ValidId_ShouldReturnSuccess() throws Exception {
        UserEntity user = new UserEntity("John", "Doe", "john.doe@example.com",
                "admin-johndoe", "Password123!", "Mr", "US");
        user.getRoles().add((new Roles("ADMIN")));
        userRepository.save(user);

        Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
        Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("admin-johndoe");

        Faq faq = new Faq("Test-What is the return policy?", null, false);
        faq = faqRepository.save(faq);

        String updateJson = """
        {
            "question": "Test-Return policy question? Updated",
            "answer": "Test-Return policy answer Updated"
        }
        """;

        mockMvc.perform(put("/api/faqs/" + faq.getId() + "/update")
                        .header("Authorization", "Bearer mock-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Faq Updated Successfully!"));

        Optional<Faq> updatedFaq = faqRepository.findById(faq.getId());
        assert updatedFaq.isPresent();
        assert "Test-Return policy question? Updated".equals(updatedFaq.get().getQuestion());
        assert "Test-Return policy answer Updated".equals(updatedFaq.get().getAnswer());

        userRepository.deleteByUsername(user.getUsername());
        faqRepository.deleteById(faq.getId());
    }

    @Test
    void deleteFaq_NonValidId_ShouldReturnFail() throws Exception {
        UserEntity user = new UserEntity("John", "Doe", "john.doe@example.com",
                "johndoe", "Password123!", "Mr", "US");
        userRepository.save(user);

        Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
        Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("johndoe");


        Faq faq = new Faq("Test-Can I change my shipping address?", null, false);
        faq = faqRepository.save(faq);

        mockMvc.perform(delete("/api/faqs/" + faq.getId() + "/delete")
                        .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isForbidden());

        Optional<Faq> updatedFaq = faqRepository.findById(faq.getId());
        assert updatedFaq.isPresent();

        userRepository.deleteByUsername(user.getUsername());
        faqRepository.deleteById(faq.getId());
    }

    @Test
    void updateFaq_NonValidId_ShouldReturnFail() throws Exception {
        UserEntity user = new UserEntity("John", "Doe", "john.doe@example.com",
                "johndoe", "Password123!", "Mr", "US");
        userRepository.save(user);

        Mockito.when(jwtGenerator.validateToken("mock-jwt-token")).thenReturn(true);
        Mockito.when(jwtGenerator.getUsernameFromJWT("mock-jwt-token")).thenReturn("johndoe");

        Faq faq = new Faq("Test-What is the return policy?", null, false);
        faq = faqRepository.save(faq);

        String updateJson = """
        {
            "question": "Test-Return policy question? Updated",
            "answer": "Test-Return policy answer Updated"
        }
        """;

        mockMvc.perform(put("/api/faqs/" + faq.getId() + "/update")
                        .header("Authorization", "Bearer mock-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());

        Optional<Faq> updatedFaq = faqRepository.findById(faq.getId());
        assert updatedFaq.isPresent();
        assert "Test-What is the return policy?".equals(updatedFaq.get().getQuestion());
        assert null == updatedFaq.get().getAnswer();

        userRepository.deleteByUsername(user.getUsername());
        faqRepository.deleteById(faq.getId());
    }
}
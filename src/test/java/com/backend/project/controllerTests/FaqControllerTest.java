package com.backend.project.controllerTests;

import com.backend.project.controller.FaqController;
import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.service.FaqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FaqControllerTest {

    @InjectMocks
    private FaqController faqController;

    @Mock
    private FaqService faqService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllFaqs_WhenCalled_ReturnsAllFaqs() {
        Faq faq = new Faq("What is Spring?", "Spring is a framework", false);
        when(faqService.getAllFaqs()).thenReturn(Arrays.asList(faq));

        ResponseEntity<?> response = faqController.getAllFaqs();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("What is Spring?", ((Faq) ((Iterable<?>) response.getBody()).iterator().next()).getQuestion());
    }

}



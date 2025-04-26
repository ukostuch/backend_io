package com.backend.project.controllerTests;

import com.backend.project.controller.OfficeController;
import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.service.OfficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OfficeControllerTest {

    private OfficeController officeController;
    private OfficeService officeService;

    @BeforeEach
    public void setUp() {
        officeService = mock(OfficeService.class);
        officeController = new OfficeController(officeService);
    }

    @Test
    public void getOffices_OfficesExist_ReturnsListOfOffices() {
        OfficeRetDto officeRetDto = mock(OfficeRetDto.class);
        when(officeService.getAllOffices()).thenReturn(Optional.of(List.of(officeRetDto)));

        ResponseEntity<Optional<List<OfficeRetDto>>> response = officeController.getOffices(null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().orElseThrow().size());
    }

    @Test
    public void getOfficeById_OfficeExists_ReturnsOfficeDetails() {
        String officeId = UUID.randomUUID().toString();

        OfficeRetDto mockOffice = new OfficeRetDto(
                UUID.fromString(officeId),
                new DistrictDto(1, "District Name"),
                "+48123456789",
                "Office Address, 1234 City",
                "photo-id-12345",
                "Office description"
        );

        when(officeService.getOfficeById(officeId)).thenReturn(mockOffice);

        ResponseEntity<OfficeRetDto> response = officeController.getOfficeById(officeId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(UUID.fromString(officeId), response.getBody().id());
        assertEquals("District Name", response.getBody().district().name());
        assertEquals("+48123456789", response.getBody().phoneNumber());
        assertEquals("Office Address, 1234 City", response.getBody().address());
        assertEquals("photo-id-12345", response.getBody().photo());
        assertEquals("Office description", response.getBody().description());

        verify(officeService, times(1)).getOfficeById(officeId);
    }

    @Test
    public void getOfficeById_OfficeNotFound_ReturnsNotFoundStatus() {
        String officeId = UUID.randomUUID().toString();

        when(officeService.getOfficeById(officeId)).thenReturn(null);

        ResponseEntity<OfficeRetDto> response = officeController.getOfficeById(officeId);

        assertEquals(404, response.getStatusCodeValue());
        verify(officeService, times(1)).getOfficeById(officeId);
    }
}

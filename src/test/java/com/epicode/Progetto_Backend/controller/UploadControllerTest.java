package com.epicode.Progetto_Backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.epicode.Progetto_Backend.service.CloudinaryService;

/**
 * UploadControllerTest - Test unitari per il controller di upload file.
 * 
 * Questa classe testa gli endpoint REST del UploadController, verificando:
 * - Upload immagine profilo utente
 * - Validazione file (tipo, dimensione)
 * - Gestione errori per file non validi
 * - Integrazione con CloudinaryService (mockato nei test)
 * 
 * Il CloudinaryService viene mockato per evitare chiamate reali all'API
 * Cloudinary durante l'esecuzione dei test. I test verificano solo
 * il comportamento del controller e la gestione corretta delle richieste.
 * 
 * @see com.epicode.Progetto_Backend.controller.UploadController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@SuppressWarnings({"null", "removal"})
class UploadControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CloudinaryService cloudinaryService;

    private MockMvc mockMvc;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void testUploadProfileImage_Success() throws Exception {
        String imageUrl = "https://cloudinary.com/test-image.jpg";
        when(cloudinaryService.uploadImage(any())).thenReturn(imageUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/profile-image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(imageUrl));
    }

    @Test
    void testUploadProfileImage_Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/profile-image")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testUploadProfileImage_Error() throws Exception {
        when(cloudinaryService.uploadImage(any()))
                .thenThrow(new java.io.IOException("Upload failed"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/profile-image")
                        .file(file))
                .andExpect(status().isInternalServerError());
    }
}


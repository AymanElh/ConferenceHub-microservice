package org.example.keynoteservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.exception.KeynoteNotFoundException;
import org.example.keynoteservice.service.IKeynoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = KeynoteController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class KeynoteControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    IKeynoteService keynoteService;

    @Test
    void create_Returns201_WhenValidRequest() throws Exception {
        KeynoteDTO dto = buildDTO("john@test.com");

        when(keynoteService.create(any())).thenReturn(dto);

        mockMvc.perform(
                        post("/api/keynotes")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("john@test.com"))
                .andDo(print());
    }

    @Test
    void create_Returns400_WhenBlankNom() throws Exception {
        KeynoteDTO dto = new KeynoteDTO();

        dto.setNom("");
        dto.setPrenom("John");
        dto.setEmail("john@test.com");
        dto.setFonction("Speaker");

        mockMvc.perform(
                        post("/api/keynotes")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());

        verify(keynoteService, never()).create(any());
    }

    @Test
    void create_Returns400_WhenInvalidEmail() throws Exception {
        KeynoteDTO dto = buildDTO("invalid-email");

        mockMvc.perform(
                        post("/api/keynotes")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))

                )
                .andExpect(status().isBadRequest());

        verify(keynoteService, never()).create(any());
    }

    // Get by id

    @Test
    void findById_Return200_WhenExists() throws Exception {
        KeynoteDTO dto = buildDTO("test@test.com");

        when(keynoteService.findById(1L)).thenReturn(dto);

        mockMvc.perform(
                get("/api/keynotes/1")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }


    @Test
    void update_Returns200_WhenValid() throws Exception {
        KeynoteDTO dto = buildDTO("updated@test.com");
        when(keynoteService.update(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/keynotes/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    // ─── DELETE ─────────────────────────────────────────────────

    @Test
    void delete_Returns204_WhenExists() throws Exception {
        doNothing().when(keynoteService).delete(1L);

        mockMvc.perform(delete("/api/keynotes/1"))
                .andExpect(status().isNoContent());

        verify(keynoteService).delete(1L);
    }

    @Test
    void delete_Returns404_WhenNotFound() throws Exception {
        doThrow(new KeynoteNotFoundException("not found"))
                .when(keynoteService).delete(99L);

        mockMvc.perform(delete("/api/keynotes/99"))
                .andExpect(status().isNotFound());
    }


    // ─── PAGINATION ─────────────────────────────────────────────

    @Test
    void findAll_ReturnsPaginatedResults() throws Exception {
        Page<KeynoteDTO> page = new PageImpl<>(
                List.of(buildDTO("a@b.com")),
                PageRequest.of(0, 10),
                1
        );
        when(keynoteService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/keynotes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("a@b.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void search_ReturnResults_WhenKeywordMatches() throws Exception {
        Page<KeynoteDTO> page = new PageImpl<>(List.of(buildDTO("dev@test.com")));
        when(keynoteService.search(eq("java"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/keynotes/search")
                        .param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("dev@test.com"));
    }

    private KeynoteDTO buildDTO(String mail) {
        KeynoteDTO dto = new KeynoteDTO();
        dto.setId(1L);
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setEmail(mail);
        dto.setFonction("Speaker");
        return dto;
    }
}

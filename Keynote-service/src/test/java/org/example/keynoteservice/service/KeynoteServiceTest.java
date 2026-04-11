package org.example.keynoteservice.service;

import org.example.keynoteservice.Mappers.KeynoteMapper;
import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.exception.KeynoteNotFoundException;
import org.example.keynoteservice.kafka.KeynoteProducer;
import org.example.keynoteservice.kafka.KeynoteWelcomeEvent;
import org.example.keynoteservice.model.Keynote;
import org.example.keynoteservice.repositroy.KeynoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeynoteServiceTest {

    @Mock
    private KeynoteRepository keynoteRepository;

    @Mock
    private KeynoteMapper keynoteMapper;

    @Mock
    private KeynoteProducer keynoteProducer;

    @InjectMocks
    private KeynoteService keynoteService;

    private Keynote keynote;
    private KeynoteDTO keynoteDTO;

    @BeforeEach
    void setUp() {
        keynote = new Keynote();
        keynote.setId(1L);
        keynote.setNom("Doe");
        keynote.setPrenom("John");
        keynote.setEmail("john.doe@example.com");

        keynoteDTO = new KeynoteDTO();
        keynoteDTO.setId(1L);
        keynoteDTO.setNom("Doe");
        keynoteDTO.setPrenom("John");
        keynoteDTO.setEmail("john.doe@example.com");
    }

    @Test
    void create_ShouldSaveKeynoteAndSendWelcomeEvent() {
        // Given
        when(keynoteRepository.existsByEmail(keynoteDTO.getEmail())).thenReturn(false);
        when(keynoteMapper.toEntity(keynoteDTO)).thenReturn(keynote);
        when(keynoteRepository.save(keynote)).thenReturn(keynote);
        when(keynoteMapper.toDto(keynote)).thenReturn(keynoteDTO);

        // When
        KeynoteDTO result = keynoteService.create(keynoteDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(keynoteDTO.getEmail());
        verify(keynoteRepository).save(any(Keynote.class));
        verify(keynoteProducer).sendWelcomeEvent(any(KeynoteWelcomeEvent.class));
    }

    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(keynoteRepository.existsByEmail(keynoteDTO.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> keynoteService.create(keynoteDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
        
        verify(keynoteRepository, never()).save(any());
        verify(keynoteProducer, never()).sendWelcomeEvent(any());
    }

    @Test
    void findById_ShouldReturnKeynote_WhenIdExists() {
        // Given
        when(keynoteRepository.findById(1L)).thenReturn(Optional.of(keynote));
        when(keynoteMapper.toDto(keynote)).thenReturn(keynoteDTO);

        // When
        KeynoteDTO result = keynoteService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Given
        when(keynoteRepository.findById(1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> keynoteService.findById(1L))
                .isInstanceOf(KeynoteNotFoundException.class)
                .hasMessageContaining("Keynote not found");
    }

    @Test
    void update_ShouldUpdateFields_WhenIdExists() {
        // Given
        when(keynoteRepository.findById(1L)).thenReturn(Optional.of(keynote));
        when(keynoteRepository.save(any(Keynote.class))).thenReturn(keynote);
        when(keynoteMapper.toDto(any(Keynote.class))).thenReturn(keynoteDTO);

        // When
        KeynoteDTO result = keynoteService.update(1L, keynoteDTO);

        // Then
        assertThat(result).isNotNull();
        verify(keynoteRepository).save(keynote);
    }

    @Test
    void delete_ShouldCallRepositoryDelete_WhenIdExists() {
        // Given
        when(keynoteRepository.existsById(1L)).thenReturn(true);

        // When
        keynoteService.delete(1L);

        // Then
        verify(keynoteRepository).deleteById(1L);
    }

    @Test
    void findAll_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Keynote> keynotePage = new PageImpl<>(List.of(keynote));
        when(keynoteRepository.findAll(pageable)).thenReturn(keynotePage);
        when(keynoteMapper.toDto(keynote)).thenReturn(keynoteDTO);

        // When
        Page<KeynoteDTO> result = keynoteService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}

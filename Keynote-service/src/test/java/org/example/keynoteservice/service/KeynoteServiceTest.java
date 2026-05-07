package org.example.keynoteservice.service;

import org.example.keynoteservice.Mappers.KeynoteMapper;
import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.exception.KeynoteNotFoundException;
import org.example.keynoteservice.kafka.KeynoteProducer;
import org.example.keynoteservice.model.Keynote;
import org.example.keynoteservice.repositroy.KeynoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeynoteServiceTest {

    @Mock
    private KeynoteRepository keynoteRepository;

    @Mock
    private KeynoteMapper keynoteMapper;

    @Mock
    private KeynoteProducer keynoteProducer;

    @InjectMocks
    private KeynoteService service;

    @Test
    void create_whenEmailExists_throws_andDoesNotPublish() {
        KeynoteDTO dto = new KeynoteDTO();
        dto.setEmail("x@example.com");

        when(keynoteRepository.existsByEmail("x@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(keynoteProducer, never()).sendWelcomeEvent(any());
    }

    @Test
    void create_saves_andPublishesWelcomeEvent() {
        KeynoteDTO dto = new KeynoteDTO();
        dto.setNom("N");
        dto.setPrenom("P");
        dto.setEmail("x@example.com");
        dto.setFonction("F");

        Keynote entity = new Keynote();
        when(keynoteRepository.existsByEmail("x@example.com")).thenReturn(false);
        when(keynoteMapper.toEntity(dto)).thenReturn(entity);

        Keynote savedEntity = new Keynote();
        savedEntity.setId(99L);
        when(keynoteRepository.save(entity)).thenReturn(savedEntity);

        KeynoteDTO savedDto = new KeynoteDTO();
        savedDto.setId(99L);
        savedDto.setNom("N");
        savedDto.setPrenom("P");
        savedDto.setEmail("x@example.com");
        savedDto.setFonction("F");
        when(keynoteMapper.toDto(savedEntity)).thenReturn(savedDto);

        KeynoteDTO out = service.create(dto);

        assertThat(out.getId()).isEqualTo(99L);
        verify(keynoteProducer).sendWelcomeEvent(any());
    }

    @Test
    void update_whenMissing_throws() {
        when(keynoteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, new KeynoteDTO()))
                .isInstanceOf(KeynoteNotFoundException.class);
    }

    @Test
    void delete_whenMissing_throws() {
        when(keynoteRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(KeynoteNotFoundException.class);
    }
}


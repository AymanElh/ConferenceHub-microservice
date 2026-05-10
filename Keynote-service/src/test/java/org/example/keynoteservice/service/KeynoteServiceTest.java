package org.example.keynoteservice.service;

import org.example.keynoteservice.mappers.KeynoteMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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
        KeynoteDTO dto = new KeynoteDTO();

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(KeynoteNotFoundException.class);
    }

    @Test
    void update_whenEmailChangedToExistingEmail_throws() {
        Keynote existing = new Keynote();
        existing.setId(1L);
        existing.setEmail("old@example.com");

        KeynoteDTO dto = new KeynoteDTO();
        dto.setEmail("new@example.com");

        when(keynoteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(keynoteRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(keynoteRepository, never()).save(any());
    }

    @Test
    void update_whenValid_updatesFieldsAndReturnsDto() {
        Keynote existing = new Keynote();
        existing.setId(1L);
        existing.setEmail("old@example.com");

        KeynoteDTO dto = new KeynoteDTO(1L, "Doe", "Jane", "new@example.com", "Architect");
        KeynoteDTO savedDto = new KeynoteDTO(1L, "Doe", "Jane", "new@example.com", "Architect");

        when(keynoteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(keynoteRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(keynoteRepository.save(existing)).thenReturn(existing);
        when(keynoteMapper.toDto(existing)).thenReturn(savedDto);

        KeynoteDTO out = service.update(1L, dto);

        assertThat(existing.getNom()).isEqualTo("Doe");
        assertThat(existing.getPrenom()).isEqualTo("Jane");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getFonction()).isEqualTo("Architect");
        assertThat(out).isSameAs(savedDto);
    }

    @Test
    void delete_whenMissing_throws() {
        when(keynoteRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(KeynoteNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesById() {
        when(keynoteRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(keynoteRepository).deleteById(1L);
    }

    @Test
    void findAll_mapsRepositoryPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Keynote keynote = new Keynote();
        keynote.setId(3L);
        KeynoteDTO dto = new KeynoteDTO(3L, "Doe", "Jane", "jane@example.com", "Architect");

        when(keynoteRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(keynote)));
        when(keynoteMapper.toDto(keynote)).thenReturn(dto);

        Page<KeynoteDTO> out = service.findAll(pageable);

        assertThat(out.getContent()).containsExactly(dto);
    }

    @Test
    void search_mapsRepositoryPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Keynote keynote = new Keynote();
        keynote.setId(4L);
        KeynoteDTO dto = new KeynoteDTO(4L, "Smith", "John", "john@example.com", "Speaker");

        when(keynoteRepository.search("java", pageable)).thenReturn(new PageImpl<>(List.of(keynote)));
        when(keynoteMapper.toDto(keynote)).thenReturn(dto);

        Page<KeynoteDTO> out = service.search("java", pageable);

        assertThat(out.getContent()).containsExactly(dto);
    }

    @Test
    void findById_whenFound_returnsMappedDto() {
        Keynote keynote = new Keynote();
        keynote.setId(5L);
        KeynoteDTO dto = new KeynoteDTO(5L, "Doe", "Jane", "jane@example.com", "Architect");

        when(keynoteRepository.findById(5L)).thenReturn(Optional.of(keynote));
        when(keynoteMapper.toDto(keynote)).thenReturn(dto);

        KeynoteDTO out = service.findById(5L);

        assertThat(out).isSameAs(dto);
    }

    @Test
    void findById_whenMissing_throws() {
        when(keynoteRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(5L))
                .isInstanceOf(KeynoteNotFoundException.class)
                .hasMessageContaining("Keynote not found with id: 5");
    }

    @Test
    void existsByEmail_delegatesToRepository() {
        when(keynoteRepository.existsByEmail("jane@example.com")).thenReturn(true);

        boolean exists = service.existsByEmail("jane@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void findByIds_mapsAllRepositoryResults() {
        Keynote keynote1 = new Keynote();
        keynote1.setId(1L);
        Keynote keynote2 = new Keynote();
        keynote2.setId(2L);
        KeynoteDTO dto1 = new KeynoteDTO(1L, "Doe", "Jane", "jane@example.com", "Architect");
        KeynoteDTO dto2 = new KeynoteDTO(2L, "Smith", "John", "john@example.com", "Speaker");

        when(keynoteRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(keynote1, keynote2));
        when(keynoteMapper.toDto(keynote1)).thenReturn(dto1);
        when(keynoteMapper.toDto(keynote2)).thenReturn(dto2);

        List<KeynoteDTO> out = service.findByIds(List.of(1L, 2L));

        assertThat(out).containsExactly(dto1, dto2);
    }
}

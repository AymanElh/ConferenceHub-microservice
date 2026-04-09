package org.example.keynoteservice.service;

import lombok.RequiredArgsConstructor;
import org.example.keynoteservice.Mappers.KeynoteMapper;
import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.kafka.KeynoteProducer;
import org.example.keynoteservice.kafka.KeynoteWelcomeEvent;
import org.example.keynoteservice.model.Keynote;
import org.example.keynoteservice.repositroy.KeynoteRepository;
import org.example.keynoteservice.exception.KeynoteNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class KeynoteService implements IKeynoteService {

    private final KeynoteRepository keynoteRepository;
    private final KeynoteMapper keynoteMapper;
    private final KeynoteProducer keynoteProducer;

    @Override
    public KeynoteDTO create(KeynoteDTO dto) {
        if (keynoteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }
        Keynote keynote = keynoteMapper.toEntity(dto);
        KeynoteDTO saved = keynoteMapper.toDto(keynoteRepository.save(keynote));

        // Publish a welcome event so the notification-service can send the welcome e-mail
        KeynoteWelcomeEvent event = KeynoteWelcomeEvent.builder()
                .keynoteId(saved.getId())
                .nom(saved.getNom())
                .prenom(saved.getPrenom())
                .email(saved.getEmail())
                .fonction(saved.getFonction())
                .welcomeMessage("Welcome " + saved.getPrenom() + " " + saved.getNom()
                        + "! You have been registered as a keynote speaker.")
                .build();
        keynoteProducer.sendWelcomeEvent(event);

        return saved;
    }

    @Override
    public KeynoteDTO update(Long id, KeynoteDTO dto) {
        Keynote existing = keynoteRepository.findById(id)
                .orElseThrow(() -> new KeynoteNotFoundException("Keynote not found with id: " + id));
        if (!existing.getEmail().equals(dto.getEmail()) && keynoteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }
        existing.setNom(dto.getNom());
        existing.setPrenom(dto.getPrenom());
        existing.setEmail(dto.getEmail());
        existing.setFonction(dto.getFonction());
        return keynoteMapper.toDto(keynoteRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        if (!keynoteRepository.existsById(id)) {
            throw new KeynoteNotFoundException("Keynote not found with id: " + id);
        }
        keynoteRepository.deleteById(id);
    }

    @Override
    public Page<KeynoteDTO> findAll(Pageable pageable) {
        return keynoteRepository.findAll(pageable)
                .map(keynoteMapper::toDto);
    }

    @Override
    public Page<KeynoteDTO> search(String keyword, Pageable pageable) {
        return keynoteRepository.search(keyword, pageable)
                .map(keynoteMapper::toDto);
    }

    @Override
    public KeynoteDTO findById(Long id) {
        Keynote keynote = keynoteRepository.findById(id)
                .orElseThrow(() -> new KeynoteNotFoundException("Keynote not found with id: " + id));
        return keynoteMapper.toDto(keynote);
    }

    @Override
    public boolean existsByEmail(String email) {
        return keynoteRepository.existsByEmail(email);
    }

    @Override
    public List<KeynoteDTO> findByIds(List<Long> ids) {
        return keynoteRepository.findAllById(ids).stream()
                .map(keynoteMapper::toDto)
                .collect(Collectors.toList());
    }
}

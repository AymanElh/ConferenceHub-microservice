package org.example.keynoteservice.service;

import lombok.RequiredArgsConstructor;
import org.example.keynoteservice.Mappers.KeynoteMapper;
import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.model.Keynote;
import org.example.keynoteservice.repositroy.keynoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KeynoteService implements IKeynoteService {

    private final keynoteRepository keynoteRepository;
    private final KeynoteMapper keynoteMapper;

    @Override
    public KeynoteDTO create(KeynoteDTO dto) {
        if (keynoteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }
        Keynote keynote = keynoteMapper.toEntity(dto);
        return keynoteMapper.toDto(keynoteRepository.save(keynote));
    }

    @Override
    public KeynoteDTO update(Long id, KeynoteDTO dto) {
        Keynote existing = keynoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Keynote not found with id: " + id));
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
            throw new RuntimeException("Keynote not found with id: " + id);
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
                .orElseThrow(() -> new RuntimeException("Keynote not found with id: " + id));
        return keynoteMapper.toDto(keynote);
    }

    @Override
    public boolean existsByEmail(String email) {
        return keynoteRepository.existsByEmail(email);
    }
}

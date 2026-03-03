package org.example.keynoteservice.service;

import org.example.keynoteservice.dto.KeynoteDTO;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;

public interface IKeynoteService {
    KeynoteDTO create(KeynoteDTO dto);

    KeynoteDTO update(Long id, KeynoteDTO dto);

    void delete(Long id);

    Page<KeynoteDTO> findAll(Pageable pageable);

    Page<KeynoteDTO> search(String keyword, Pageable pageable);

    KeynoteDTO findById(Long id);

    boolean existsByEmail(String email);
}

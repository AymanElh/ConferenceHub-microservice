package org.example.keynoteservice.service;

import org.example.keynoteservice.dto.KeynoteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IKeynoteService {
    KeynoteDTO create(KeynoteDTO dto);

    KeynoteDTO update(Long id, KeynoteDTO dto);

    void delete(Long id);

    Page<KeynoteDTO> findAll(Pageable pageable);

    Page<KeynoteDTO> search(String keyword, Pageable pageable);

    KeynoteDTO findById(Long id);

    boolean existsByEmail(String email);

    List<KeynoteDTO> findByIds(List<Long> ids);
}

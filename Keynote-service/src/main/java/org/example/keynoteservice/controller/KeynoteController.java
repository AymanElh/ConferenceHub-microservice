package org.example.keynoteservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.service.IKeynoteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/keynotes")
@RequiredArgsConstructor
public class KeynoteController {

    private final IKeynoteService keynoteService;


    @PostMapping
    public ResponseEntity<KeynoteDTO> create(@Valid @RequestBody KeynoteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(keynoteService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KeynoteDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody KeynoteDTO dto) {
        return ResponseEntity.ok(keynoteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        keynoteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeynoteDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(keynoteService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<KeynoteDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));
        return ResponseEntity.ok(keynoteService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<KeynoteDTO>> search(@RequestParam String keyword,
                                                   Pageable pageable) {
        return ResponseEntity.ok(keynoteService.search(keyword, pageable));
    }
}

package org.example.keynoteservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeynoteDTO {
    private Long id;
    @NotBlank
    private String nom;
    @NotBlank
    private String prenom;
    @Email
    private String email;
    @NotBlank
    private String fonction;

}

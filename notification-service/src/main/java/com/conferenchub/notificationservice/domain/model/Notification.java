package com.conferenchub.notificationservice.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    @NotBlank
    @Email
    private String destinataire;

    @NotBlank
    private String sujet;

    @NotBlank
    private String contenu;

    @NotNull
    private EventType typeEvenement;

    private Long referenceId;

    @Builder.Default
    private NotificationStatus statut = NotificationStatus.EN_ATTENTE;

    private LocalDateTime dateEnvoi;

    @Builder.Default
    private Integer tentatives = 0;

    private String erreurMessage;
}

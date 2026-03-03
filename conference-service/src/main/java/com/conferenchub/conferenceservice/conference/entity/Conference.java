package com.conferenchub.conferenceservice.conference.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conferences")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ConferenceStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ConferenceType type;

    @Min(1)
    private Integer duration; // in minutes

    @Min(0)
    private Integer registeredNumber;
    private Double score;

    // relationships with other entities from other services
    @ElementCollection
    @CollectionTable(name = "ConferenceKeynote", joinColumns = @JoinColumn(name = "conferenceId"))
    @Column(name = "keynoteId")
    private List<Long> keynoteIds;

    @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;

    @NotNull
    private LocalDate date;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

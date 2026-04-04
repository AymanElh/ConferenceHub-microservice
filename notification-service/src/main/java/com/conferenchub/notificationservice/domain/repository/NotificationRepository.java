package com.conferenchub.notificationservice.domain.repository;

import com.conferenchub.notificationservice.domain.model.EventType;
import com.conferenchub.notificationservice.domain.model.Notification;
import com.conferenchub.notificationservice.domain.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByDestinataire(String destinataire, Pageable pageable);
    Page<Notification> findByReferenceId(Long referenceId, Pageable pageable);
    List<Notification> findByStatut(NotificationStatus statut);
    boolean existsByReferenceIdAndTypeEvenementAndStatut(Long referenceId, EventType typeEvenement, NotificationStatus statut);
}

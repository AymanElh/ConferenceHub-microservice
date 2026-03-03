package org.example.keynoteservice.repositroy;

import org.example.keynoteservice.model.Keynote;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;

public interface keynoteRepository extends JpaRepository<Keynote,Long> {
    boolean existsByEmail(String email);
    @Query("""
        SELECT k FROM Keynote k
        WHERE LOWER(k.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(k.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(k.fonction) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Keynote> search(@Param("keyword") String keyword, Pageable pageable);
}

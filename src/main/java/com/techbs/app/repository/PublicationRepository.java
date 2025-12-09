package com.techbs.app.repository;

import com.techbs.app.model.Publication;
import com.techbs.app.model.Publication.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    
    // Rechercher par description contenant un mot-clé
    Page<Publication> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);
    
    // Rechercher par type de média
    Page<Publication> findByMediaType(MediaType mediaType, Pageable pageable);
    
    // Rechercher les publications d'une formation spécifique
    Page<Publication> findByFormationId(Long formationId, Pageable pageable);
    
    // Rechercher les publications sans média
    @Query("SELECT p FROM Publication p WHERE p.mediaType = 'NONE' OR p.mediaType IS NULL")
    Page<Publication> findPublicationsWithoutMedia(Pageable pageable);
    
    // Rechercher les publications avec média
    @Query("SELECT p FROM Publication p WHERE p.mediaType != 'NONE' AND p.mediaType IS NOT NULL")
    Page<Publication> findPublicationsWithMedia(Pageable pageable);
    
    // Compter les publications par formation
    long countByFormationId(Long formationId);
    
    // Rechercher les publications récentes (dernières 30 jours)
    @Query("SELECT p FROM Publication p WHERE p.createdAt >= :date")
    Page<Publication> findRecentPublications(@Param("date") java.time.LocalDateTime date, Pageable pageable);
}
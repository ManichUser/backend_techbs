package com.techbs.app.repository;

import com.techbs.app.model.Formation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {
   
    Optional<Formation> findByTitre(String titre);
    
    List<Formation> findByTitreContainingIgnoreCase(String keyword);
    
    List<Formation> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String titreKeyword, String descriptionKeyword
    );
                         
    Page<Formation> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String titreKeyword, String descriptionKeyword, Pageable pageable
    );
    
    // Vérifier si une formation existe par titre
    boolean existsByTitre(String titre);
}
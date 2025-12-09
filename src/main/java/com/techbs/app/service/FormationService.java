package com.techbs.app.service;

import com.techbs.app.model.Formation;
import com.techbs.app.repository.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FormationService {
    
    private final FormationRepository formationRepository;
    
    // Créer une nouvelle formation
    public Formation createFormation(Formation formation) {
        if (formationRepository.existsByTitre(formation.getTitre())) {
            throw new RuntimeException("Une formation avec ce titre existe déjà");
        }
        return formationRepository.save(formation);
    }
    
    // Récupérer toutes les formations avec pagination
    @Transactional(readOnly = true)
    public Page<Formation> getAllFormations(Pageable pageable) {
        return formationRepository.findAll(pageable);
    }
    
    // Récupérer toutes les formations sans pagination
    @Transactional(readOnly = true)
    public List<Formation> getAllFormations() {
        return formationRepository.findAll();
    }
    
    // Récupérer une formation par ID
    @Transactional(readOnly = true)
    public Optional<Formation> getFormationById(Long id) {
        return formationRepository.findById(id);
    }
    
    // Mettre à jour une formation
    public Formation updateFormation(Long id, Formation formationDetails) {
        Formation formation = formationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + id));
        
        formation.setTitre(formationDetails.getTitre());
        formation.setDescription(formationDetails.getDescription());
        formation.setUrlImage(formationDetails.getUrlImage());
        formation.setUrlPdf(formationDetails.getUrlPdf());
        
        return formationRepository.save(formation);
    }
    
    // Supprimer une formation
    public void deleteFormation(Long id) {
        if (!formationRepository.existsById(id)) {
            throw new RuntimeException("Formation non trouvée avec l'ID: " + id);
        }
        formationRepository.deleteById(id);
    }
    
    // Rechercher des formations par mot-clé avec pagination
    @Transactional(readOnly = true)
    public Page<Formation> searchFormations(String keyword, Pageable pageable) {
        return formationRepository.findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            keyword, keyword, pageable
        );
    }
}
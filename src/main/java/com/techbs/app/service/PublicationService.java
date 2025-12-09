package com.techbs.app.service;

import com.techbs.app.model.Formation;
import com.techbs.app.model.Publication;
import com.techbs.app.model.Publication.MediaType;
import com.techbs.app.repository.FormationRepository;
import com.techbs.app.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicationService {
    
    private final PublicationRepository publicationRepository;
    private final FormationRepository formationRepository;
    
    // Créer une nouvelle publication
    public Publication createPublication(Publication publication) {
        // Si une formation est liée, vérifier qu'elle existe
        if (publication.getFormation() != null && publication.getFormation().getId() != null) {
            Formation formation = formationRepository.findById(publication.getFormation().getId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + publication.getFormation().getId()));
            publication.setFormation(formation);
        }
        
        return publicationRepository.save(publication);
    }
    
    // Récupérer toutes les publications avec pagination
    @Transactional(readOnly = true)
    public Page<Publication> getAllPublications(Pageable pageable) {
        return publicationRepository.findAll(pageable);
    }
    
    // Récupérer toutes les publications sans pagination
    @Transactional(readOnly = true)
    public List<Publication> getAllPublications() {
        return publicationRepository.findAll();
    }
    
    // Récupérer une publication par ID
    @Transactional(readOnly = true)
    public Optional<Publication> getPublicationById(Long id) {
        return publicationRepository.findById(id);
    }
    
    // Mettre à jour une publication
    public Publication updatePublication(Long id, Publication publicationDetails) {
        Publication publication = publicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Publication non trouvée avec l'ID: " + id));
        
        publication.setDescription(publicationDetails.getDescription());
        publication.setMediaUrl(publicationDetails.getMediaUrl());
        publication.setMediaType(publicationDetails.getMediaType());
        
        // Mettre à jour la formation si fournie
        if (publicationDetails.getFormation() != null && publicationDetails.getFormation().getId() != null) {
            Formation formation = formationRepository.findById(publicationDetails.getFormation().getId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + publicationDetails.getFormation().getId()));
            publication.setFormation(formation);
        }
        
        return publicationRepository.save(publication);
    }
    
    // Supprimer une publication
    public void deletePublication(Long id) {
        if (!publicationRepository.existsById(id)) {
            throw new RuntimeException("Publication non trouvée avec l'ID: " + id);
        }
        publicationRepository.deleteById(id);
    }
    
    // Rechercher des publications par mot-clé
    @Transactional(readOnly = true)
    public Page<Publication> searchPublications(String keyword, Pageable pageable) {
        return publicationRepository.findByDescriptionContainingIgnoreCase(keyword, pageable);
    }
    
    // Récupérer les publications par type de média
    @Transactional(readOnly = true)
    public Page<Publication> getPublicationsByMediaType(MediaType mediaType, Pageable pageable) {
        return publicationRepository.findByMediaType(mediaType, pageable);
    }
    
    // Récupérer les publications d'une formation
    @Transactional(readOnly = true)
    public Page<Publication> getPublicationsByFormation(Long formationId, Pageable pageable) {
        return publicationRepository.findByFormationId(formationId, pageable);
    }
    
    // Récupérer les publications sans média
    @Transactional(readOnly = true)
    public Page<Publication> getPublicationsWithoutMedia(Pageable pageable) {
        return publicationRepository.findPublicationsWithoutMedia(pageable);
    }
    
    // Récupérer les publications avec média
    @Transactional(readOnly = true)
    public Page<Publication> getPublicationsWithMedia(Pageable pageable) {
        return publicationRepository.findPublicationsWithMedia(pageable);
    }
    
    // Récupérer les publications récentes
    @Transactional(readOnly = true)
    public Page<Publication> getRecentPublications(Pageable pageable) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return publicationRepository.findRecentPublications(thirtyDaysAgo, pageable);
    }
    
    // Compter les publications d'une formation
    @Transactional(readOnly = true)
    public long countPublicationsByFormation(Long formationId) {
        return publicationRepository.countByFormationId(formationId);
    }
}
package com.techbs.app.controller;

import com.techbs.app.model.Formation;
import com.techbs.app.model.Publication;
import com.techbs.app.model.Publication.MediaType;
import com.techbs.app.service.FileStorageService;
import com.techbs.app.service.PublicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Publications", description = "API de gestion des publications avec médias")
public class PublicationController {
    
    private final PublicationService publicationService;
    private final FileStorageService fileStorageService;
    
    // Créer une nouvelle publication avec média optionnel
    @Operation(
        summary = "Créer une nouvelle publication",
        description = "Crée une publication avec description, média optionnel (image/audio/vidéo) et lien vers une formation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Publication créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou type de média non supporté")
    })
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Publication> createPublication(
        @Parameter(description = "Description de la publication", required = true)
        @RequestParam("description") String description,
        
        @Parameter(description = "ID de la formation associée (optionnel)")
        @RequestParam(value = "formationId", required = false) Long formationId,
        
        @Parameter(description = "Fichier média : image (JPG/PNG/GIF), audio (MP3) ou vidéo (MP4)")
        @RequestParam(value = "media", required = false) MultipartFile media
    ) {
        try {
            Publication publication = new Publication();
            publication.setDescription(description);
            
            // Associer à une formation si l'ID est fourni
            if (formationId != null) {
                Formation formation = new Formation();
                formation.setId(formationId);
                publication.setFormation(formation);
            }
            
            // Gérer le média si présent
            if (media != null && !media.isEmpty()) {
                String contentType = media.getContentType();
                String mediaUrl;
                MediaType mediaType;
                
                if (contentType != null) {
                    if (contentType.startsWith("image/")) {
                        mediaUrl = fileStorageService.saveMedia(media, "images");
                        mediaType = MediaType.IMAGE;
                    } else if (contentType.equals("audio/mpeg") || contentType.equals("audio/mp3")) {
                        mediaUrl = fileStorageService.saveMedia(media, "audios");
                        mediaType = MediaType.MP3;
                    } else if (contentType.startsWith("video/")) {
                        mediaUrl = fileStorageService.saveMedia(media, "videos");
                        mediaType = MediaType.MP4;
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    
                    publication.setMediaUrl(mediaUrl);
                    publication.setMediaType(mediaType);
                } else {
                    publication.setMediaType(MediaType.NONE);
                }
            } else {
                publication.setMediaType(MediaType.NONE);
            }
            
            Publication createdPublication = publicationService.createPublication(publication);
            return new ResponseEntity<>(createdPublication, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Récupérer toutes les publications avec pagination
    @GetMapping
    public ResponseEntity<Page<Publication>> getAllPublications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Publication> publications = publicationService.getAllPublications(pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer toutes les publications sans pagination
    @GetMapping("/all")
    public ResponseEntity<List<Publication>> getAllPublicationsNoPagination() {
        List<Publication> publications = publicationService.getAllPublications();
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer une publication par ID
    @GetMapping("/{id}")
    public ResponseEntity<Publication> getPublicationById(@PathVariable Long id) {
        return publicationService.getPublicationById(id)
            .map(publication -> new ResponseEntity<>(publication, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // Mettre à jour une publication
    @PutMapping("/{id}")
    public ResponseEntity<Publication> updatePublication(
        @PathVariable Long id,
        @RequestParam("description") String description,
        @RequestParam(value = "formationId", required = false) Long formationId,
        @RequestParam(value = "media", required = false) MultipartFile media
    ) {
        try {
            Publication existingPublication = publicationService.getPublicationById(id)
                .orElseThrow(() -> new RuntimeException("Publication non trouvée"));
            
            Publication publicationDetails = new Publication();
            publicationDetails.setDescription(description);
            
            // Associer à une formation si l'ID est fourni
            if (formationId != null) {
                Formation formation = new Formation();
                formation.setId(formationId);
                publicationDetails.setFormation(formation);
            }
            
            // Gérer le nouveau média si présent
            if (media != null && !media.isEmpty()) {
                // Supprimer l'ancien média
                if (existingPublication.getMediaUrl() != null) {
                    fileStorageService.deleteFile(existingPublication.getMediaUrl());
                }
                
                String contentType = media.getContentType();
                String mediaUrl;
                MediaType mediaType;
                
                if (contentType != null) {
                    if (contentType.startsWith("image/")) {
                        mediaUrl = fileStorageService.saveMedia(media, "images");
                        mediaType = MediaType.IMAGE;
                    } else if (contentType.equals("audio/mpeg") || contentType.equals("audio/mp3")) {
                        mediaUrl = fileStorageService.saveMedia(media, "audios");
                        mediaType = MediaType.MP3;
                    } else if (contentType.startsWith("video/")) {
                        mediaUrl = fileStorageService.saveMedia(media, "videos");
                        mediaType = MediaType.MP4;
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    
                    publicationDetails.setMediaUrl(mediaUrl);
                    publicationDetails.setMediaType(mediaType);
                }
            } else {
                publicationDetails.setMediaUrl(existingPublication.getMediaUrl());
                publicationDetails.setMediaType(existingPublication.getMediaType());
            }
            
            Publication updatedPublication = publicationService.updatePublication(id, publicationDetails);
            return new ResponseEntity<>(updatedPublication, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Supprimer une publication
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable Long id) {
        try {
            Publication publication = publicationService.getPublicationById(id)
                .orElseThrow(() -> new RuntimeException("Publication non trouvée"));
            
            // Supprimer le média si présent
            if (publication.getMediaUrl() != null) {
                fileStorageService.deleteFile(publication.getMediaUrl());
            }
            
            publicationService.deletePublication(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Rechercher des publications par mot-clé
    @GetMapping("/search")
    public ResponseEntity<Page<Publication>> searchPublications(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Publication> publications = publicationService.searchPublications(keyword, pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer les publications par type de média
    @GetMapping("/type/{mediaType}")
    public ResponseEntity<Page<Publication>> getPublicationsByMediaType(
        @PathVariable MediaType mediaType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Publication> publications = publicationService.getPublicationsByMediaType(mediaType, pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer les publications d'une formation
    @GetMapping("/formation/{formationId}")
    public ResponseEntity<Page<Publication>> getPublicationsByFormation(
        @PathVariable Long formationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Publication> publications = publicationService.getPublicationsByFormation(formationId, pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer les publications sans média
    @GetMapping("/no-media")
    public ResponseEntity<Page<Publication>> getPublicationsWithoutMedia(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Publication> publications = publicationService.getPublicationsWithoutMedia(pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer les publications avec média
    @GetMapping("/with-media")
    public ResponseEntity<Page<Publication>> getPublicationsWithMedia(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Publication> publications = publicationService.getPublicationsWithMedia(pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Récupérer les publications récentes (30 derniers jours)
    @GetMapping("/recent")
    public ResponseEntity<Page<Publication>> getRecentPublications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Publication> publications = publicationService.getRecentPublications(pageable);
        return new ResponseEntity<>(publications, HttpStatus.OK);
    }
    
    // Compter les publications d'une formation
    @GetMapping("/formation/{formationId}/count")
    public ResponseEntity<Long> countPublicationsByFormation(@PathVariable Long formationId) {
        long count = publicationService.countPublicationsByFormation(formationId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
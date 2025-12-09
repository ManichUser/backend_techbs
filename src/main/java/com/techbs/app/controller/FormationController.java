package com.techbs.app.controller;

import com.techbs.app.model.Formation;
import com.techbs.app.service.FileStorageService;
import com.techbs.app.service.FormationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Formations", description = "API de gestion des formations")
public class FormationController {
    
    private final FormationService formationService;
    private final FileStorageService fileStorageService;
    
    // Créer une nouvelle formation avec fichiers
    @Operation(
        summary = "Créer une nouvelle formation",
        description = "Crée une formation avec titre, description, image et PDF optionnels"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Formation créée avec succès"),
        @ApiResponse(responseCode = "409", description = "Une formation avec ce titre existe déjà")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Formation> createFormation(
        @Parameter(description = "Titre de la formation", required = true)
        @RequestParam("titre") String titre,
        
        @Parameter(description = "Description détaillée de la formation", required = true)
        @RequestParam("description") String description,
        
        @Parameter(description = "Image de la formation (JPG, PNG, GIF)")
        @RequestParam(value = "image", required = false) MultipartFile image,
        
        @Parameter(description = "Document PDF de la formation")
        @RequestParam(value = "pdf", required = false) MultipartFile pdf
    ) {
        try {
            Formation formation = new Formation();
            formation.setTitre(titre);
            formation.setDescription(description);
            
            // Sauvegarder l'image si présente
            if (image != null && !image.isEmpty()) {
                String imageUrl = fileStorageService.saveImage(image);
                formation.setUrlImage(imageUrl);
            }
            
            // Sauvegarder le PDF si présent
            if (pdf != null && !pdf.isEmpty()) {
                String pdfUrl = fileStorageService.savePdf(pdf);
                formation.setUrlPdf(pdfUrl);
            }
            
            Formation createdFormation = formationService.createFormation(formation);
            return new ResponseEntity<>(createdFormation, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    // Récupérer toutes les formations avec pagination
    @Operation(
        summary = "Récupérer toutes les formations",
        description = "Liste paginée de toutes les formations avec options de tri"
    )
    @GetMapping
    public ResponseEntity<Page<Formation>> getAllFormations(
        @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "id") String sortBy,
        @Parameter(description = "Direction du tri (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Formation> formations = formationService.getAllFormations(pageable);
        return new ResponseEntity<>(formations, HttpStatus.OK);
    }
    
    // Récupérer toutes les formations sans pagination
    @GetMapping("/all")
    public ResponseEntity<List<Formation>> getAllFormationsNoPagination() {
        List<Formation> formations = formationService.getAllFormations();
        return new ResponseEntity<>(formations, HttpStatus.OK);
    }
    
    // Récupérer une formation par ID
    @GetMapping("/{id}")
    public ResponseEntity<Formation> getFormationById(@PathVariable Long id) {
        return formationService.getFormationById(id)
            .map(formation -> new ResponseEntity<>(formation, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // Mettre à jour une formation avec fichiers
    @PutMapping("/{id}")
    public ResponseEntity<Formation> updateFormation(
        @PathVariable Long id,
        @RequestParam("titre") String titre,
        @RequestParam("description") String description,
        @RequestParam(value = "image", required = false) MultipartFile image,
        @RequestParam(value = "pdf", required = false) MultipartFile pdf
    ) {
        try {
            Formation formationDetails = new Formation();
            formationDetails.setTitre(titre);
            formationDetails.setDescription(description);
            
            // Récupérer la formation existante pour supprimer les anciens fichiers si nécessaire
            Formation existingFormation = formationService.getFormationById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
            
            // Sauvegarder la nouvelle image si présente
            if (image != null && !image.isEmpty()) {
                // Supprimer l'ancienne image
                if (existingFormation.getUrlImage() != null) {
                    fileStorageService.deleteFile(existingFormation.getUrlImage());
                }
                String imageUrl = fileStorageService.saveImage(image);
                formationDetails.setUrlImage(imageUrl);
            } else {
                formationDetails.setUrlImage(existingFormation.getUrlImage());
            }
            
            // Sauvegarder le nouveau PDF si présent
            if (pdf != null && !pdf.isEmpty()) {
                // Supprimer l'ancien PDF
                if (existingFormation.getUrlPdf() != null) {
                    fileStorageService.deleteFile(existingFormation.getUrlPdf());
                }
                String pdfUrl = fileStorageService.savePdf(pdf);
                formationDetails.setUrlPdf(pdfUrl);
            } else {
                formationDetails.setUrlPdf(existingFormation.getUrlPdf());
            }
            
            Formation updatedFormation = formationService.updateFormation(id, formationDetails);
            return new ResponseEntity<>(updatedFormation, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Supprimer une formation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormation(@PathVariable Long id) {
        try {
            // Récupérer la formation pour supprimer les fichiers associés
            Formation formation = formationService.getFormationById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
            
            // Supprimer les fichiers
            if (formation.getUrlImage() != null) {
                fileStorageService.deleteFile(formation.getUrlImage());
            }
            if (formation.getUrlPdf() != null) {
                fileStorageService.deleteFile(formation.getUrlPdf());
            }
            
            formationService.deleteFormation(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Rechercher des formations par mot-clé avec pagination
    @GetMapping("/search")
    public ResponseEntity<Page<Formation>> searchFormations(
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
        Page<Formation> formations = formationService.searchFormations(keyword, pageable);
        return new ResponseEntity<>(formations, HttpStatus.OK);
    }
}
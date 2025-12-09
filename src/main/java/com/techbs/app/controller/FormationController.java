package com.techbs.app.controller;

import com.techbs.app.model.Formation;
import com.techbs.app.service.FormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FormationController {
    
    private final FormationService formationService;
    
    // Créer une nouvelle formation
       @PostMapping
       public ResponseEntity<Formation> createFormation(@RequestBody Formation formation) {
           try {
               Formation createdFormation = formationService.createFormation(formation);
               return new ResponseEntity<>(createdFormation, HttpStatus.CREATED);
           } catch (RuntimeException e) {
               return ResponseEntity.status(HttpStatus.CONFLICT).build();
           }
       }
    // Récupérer toutes les formations avec pagination
    @GetMapping
    public ResponseEntity<Page<Formation>> getAllFormations(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
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
    
    // Mettre à jour une formation
    @PutMapping("/{id}")
    public ResponseEntity<Formation> updateFormation(
        @PathVariable Long id, 
        @RequestBody Formation formationDetails
    ) {
        try {
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
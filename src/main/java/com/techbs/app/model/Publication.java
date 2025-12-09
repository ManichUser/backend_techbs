package com.techbs.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "publications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "media_url", length = 500)
    private String mediaUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20)
    private MediaType mediaType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id")
    private Formation formation;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum pour les types de média
    public enum MediaType {
        IMAGE,
        MP3,
        MP4,
        NONE
    }
}
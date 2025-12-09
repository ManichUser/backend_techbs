package com.techbs.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // Initialiser le répertoire de stockage
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                Files.createDirectories(uploadPath.resolve("images"));
                Files.createDirectories(uploadPath.resolve("pdfs"));
                Files.createDirectories(uploadPath.resolve("audios"));
                Files.createDirectories(uploadPath.resolve("videos"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage", e);
        }
    }

    // Sauvegarder une image
    public String saveImage(MultipartFile file) {
        return saveFile(file, "images");
    }

    // Sauvegarder un PDF
    public String savePdf(MultipartFile file) {
        return saveFile(file, "pdfs");
    }
    
    // Sauvegarder un média (image, audio, vidéo)
    public String saveMedia(MultipartFile file, String subDir) {
        return saveFile(file, subDir);
    }

    // Méthode générique pour sauvegarder un fichier
    private String saveFile(MultipartFile file, String subDir) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Le fichier est vide");
            }

            // Vérifier le type de fichier
            String contentType = file.getContentType();
            if (subDir.equals("images") && !isValidImageType(contentType)) {
                throw new RuntimeException("Type d'image non valide. Accepté: JPG, PNG, GIF");
            }
            if (subDir.equals("pdfs") && !contentType.equals("application/pdf")) {
                throw new RuntimeException("Seuls les fichiers PDF sont acceptés");
            }
            if (subDir.equals("audios") && !isValidAudioType(contentType)) {
                throw new RuntimeException("Type audio non valide. Accepté: MP3");
            }
            if (subDir.equals("videos") && !isValidVideoType(contentType)) {
                throw new RuntimeException("Type vidéo non valide. Accepté: MP4");
            }

            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Créer le chemin complet
            Path uploadPath = Paths.get(uploadDir, subDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFilename);

            // Copier le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif
            return "/" + subDir + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Échec de la sauvegarde du fichier: " + e.getMessage(), e);
        }
    }

    // Supprimer un fichier
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                Path filePath = Paths.get(uploadDir).resolve(fileUrl.substring(1)); // Enlever le "/" initial
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Échec de la suppression du fichier", e);
        }
    }

    // Vérifier si le type d'image est valide
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }
    
    // Vérifier si le type audio est valide
    private boolean isValidAudioType(String contentType) {
        return contentType != null && (
            contentType.equals("audio/mpeg") ||
            contentType.equals("audio/mp3")
        );
    }
    
    // Vérifier si le type vidéo est valide
    private boolean isValidVideoType(String contentType) {
        return contentType != null && (
            contentType.equals("video/mp4") ||
            contentType.equals("video/mpeg")
        );
    }
}
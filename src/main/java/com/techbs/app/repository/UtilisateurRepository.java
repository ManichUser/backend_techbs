package com.techbs.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techbs.app.model.Utilisateur;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
}

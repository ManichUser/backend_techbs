package com.techbs.app.service;

import com.techbs.app.model.Utilisateur;
import com.techbs.app.repository.UtilisateurRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Utilisateur save(Utilisateur utilisateur) {
        // Hachage du mot de passe uniquement si création ou modification
        if (utilisateur.getMdp() != null && !utilisateur.getMdp().startsWith("$2a$")) {
            utilisateur.setMdp(passwordEncoder.encode(utilisateur.getMdp()));
        }
        return utilisateurRepository.save(utilisateur);
    }

    public void delete(Long id) {
        utilisateurRepository.deleteById(id);
    }

    public Optional<Utilisateur> login(String email, String mdp) {
        Optional<Utilisateur> user = utilisateurRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(mdp, user.get().getMdp())) {
            return user;
        }
        return Optional.empty();
    }
}

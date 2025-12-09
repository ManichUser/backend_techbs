package com.techbs.app.controller;

import com.techbs.app.model.Utilisateur;
import com.techbs.app.service.UtilisateurService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin("*")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ---------- CRUD ----------------

    @GetMapping
    public List<Utilisateur> getAll() {
        return utilisateurService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Utilisateur> getById(@PathVariable Long id) {
        return utilisateurService.findById(id);
    }

    @PostMapping
    public Utilisateur create(@RequestBody Utilisateur utilisateur) {
        return utilisateurService.save(utilisateur);
    }

    @PutMapping("/{id}")
    public Utilisateur update(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        utilisateur.setId(id);
        return utilisateurService.save(utilisateur);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        utilisateurService.delete(id);
    }

    // ---------- LOGIN (email + mdp haché) -------------

    @PostMapping("/login")
    public Object login(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        String mdp = data.get("mdp");

        Optional<Utilisateur> user = utilisateurService.login(email, mdp);

        if (user.isPresent()) {
            Utilisateur u = user.get();
            u.setMdp(null); // 🔥 On retire le mot de passe avant de renvoyer
            return Map.of(
                "success", true,
                "message", "Connexion réussie",
                "user", u
            );
        }

        return Map.of(
                "success", false,
                "message", "Email ou mot de passe incorrect"
        );
    }
}

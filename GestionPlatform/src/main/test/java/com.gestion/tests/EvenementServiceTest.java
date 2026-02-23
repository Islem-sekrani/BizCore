package com.gestion.tests;

import com.gestion.entities.Evenement;
import com.gestion.services.EvenementService;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EvenementServiceTest {

    private static EvenementService service;
    private static int createdEventId; // pour stocker l'id créé

    @BeforeAll
    public static void setup() {
        service = new EvenementService();
    }

    // ===============================
    // TEST AJOUT
    // ===============================
    @Test
    @Order(1)
    public void testAjouterEvenement() {

        Evenement e = new Evenement();
        e.setTitre("Test Event");
        e.setDescription("Description test");
        e.setLieu("Tunisie");
        e.setCapacite(50);
        e.setPrix(25.0);
        e.setStatut("ACTIF");
        e.setDateDebut(LocalDateTime.now());
        e.setDateFin(LocalDateTime.now().plusDays(1));
        e.setImageUrl("");
        e.setIdOrganisateur(1);
        e.setIdCategorie(1);

        service.ajouter(e);

        List<Evenement> liste = service.afficher();

        Evenement found = liste.stream()
                .filter(ev -> "Test Event".equals(ev.getTitre()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "L'événement ajouté devrait exister");

        createdEventId = found.getIdEvenement(); // sauvegarder pour les autres tests
    }

    // ===============================
    // TEST MODIFICATION
    // ===============================
    @Test
    @Order(2)
    public void testModifierEvenement() {

        List<Evenement> liste = service.afficher();

        Evenement e = liste.stream()
                .filter(ev -> ev.getIdEvenement() == createdEventId)
                .findFirst()
                .orElse(null);

        assertNotNull(e, "L'événement doit exister avant modification");

        e.setCapacite(100);
        service.modifier(e);

        Evenement updated = service.afficher().stream()
                .filter(ev -> ev.getIdEvenement() == createdEventId)
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertEquals(100, updated.getCapacite(),
                "La capacité doit être mise à jour à 100");
    }

    // ===============================
    // TEST SUPPRESSION
    // ===============================
    @Test
    @Order(3)
    public void testSupprimerEvenement() {

        service.supprimer(createdEventId);

        boolean existeEncore = service.afficher().stream()
                .anyMatch(ev -> ev.getIdEvenement() == createdEventId);

        assertFalse(existeEncore,
                "L'événement supprimé ne doit plus exister");
    }
}

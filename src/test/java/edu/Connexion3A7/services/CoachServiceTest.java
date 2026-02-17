package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.tools.MyConnection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for CoachService CRUD operations.
 * Order: Create → Read → Update → Delete.
 * Cleanup via @AfterEach to guarantee DB independence.
 */
@TestMethodOrder(OrderAnnotation.class)
public class CoachServiceTest {

    private static CoachService coachService;
    private static int testUserId;

    /** Coach created during test — tracked for @AfterEach cleanup */
    private coach createdCoach;

    @BeforeAll
    static void setUp() {
        assertTrue(MyConnection.getInstance().isConnected(),
                "La connexion a la base de donnees doit etre active");
        coachService = new CoachService();

        // Find a valid user id for FK (id_user in coach table)
        try {
            ResultSet rs = MyConnection.getInstance().getCnx()
                    .createStatement()
                    .executeQuery("SELECT id_user FROM users ORDER BY id_user DESC LIMIT 1");
            if (rs.next()) {
                testUserId = rs.getInt("id_user");
            } else {
                fail("Aucun utilisateur dans la table users — requis pour FK coach.id_user");
            }
        } catch (SQLException e) {
            fail("Erreur recherche utilisateur test: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (createdCoach != null && createdCoach.getId_coach() > 0) {
            try {
                coachService.deleteCoach(createdCoach);
            } catch (SQLException ignored) {
                // Already deleted by the test
            }
            createdCoach = null;
        }
    }

    // =====================================================================
    // Helper: build a coach that fits the DB schema (nom/prenom varchar 20)
    // =====================================================================
    private coach buildTestCoach() {
        coach c = new coach();
        c.setId_user(testUserId);
        c.setNom("JUnit"); // 5 chars — fits varchar(20)
        c.setPrenom("Test"); // 4 chars
        c.setDomaine("BRANDING");
        c.setBiographie("Bio test");
        c.setExperience(3);
        c.setTarif(50.0f);
        c.setDispo("Disponible");
        c.setNumTel("12345678");
        c.setNote(0f);
        return c;
    }

    /** Retrieve the auto-generated id_coach after INSERT */
    private int findCoachId(String nom, String prenom) throws SQLException {
        String sql = "SELECT id_coach FROM coach WHERE nom = ? AND prenom = ? ORDER BY id_coach DESC LIMIT 1";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, nom);
        pst.setString(2, prenom);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt("id_coach") : -1;
    }

    // =====================================================================
    // 1. CREATE
    // =====================================================================
    @Test
    @Order(1)
    @DisplayName("addCoach — insere un coach et verifie sa presence en base")
    void testAddCoach() throws SQLException {
        // Clean up any leftover from a previous failed run
        cleanLeftover("JUnit", "Test");

        coach c = buildTestCoach();
        coachService.addCoach(c);

        int id = findCoachId(c.getNom(), c.getPrenom());
        assertTrue(id > 0, "Le coach doit avoir un id_coach auto-genere > 0");

        c.setId_coach(id);
        createdCoach = c; // mark for cleanup

        // Verify via getData()
        List<coach> all = coachService.getData();
        boolean found = all.stream().anyMatch(co -> co.getId_coach() == id);
        assertTrue(found, "Le coach cree doit apparaitre dans getData()");
    }

    // =====================================================================
    // 2. READ
    // =====================================================================
    @Test
    @Order(2)
    @DisplayName("getData — retourne une liste non-null")
    void testGetData() throws SQLException {
        List<coach> data = coachService.getData();
        assertNotNull(data, "getData() ne doit jamais retourner null");
    }

    @Test
    @Order(3)
    @DisplayName("isCoachDuplicate — detecte un doublon existant")
    void testIsCoachDuplicate() throws SQLException {
        cleanLeftover("DupTest", "DupPre");

        coach c = buildTestCoach();
        c.setNom("DupTest");
        c.setPrenom("DupPre");
        coachService.addCoach(c);
        int id = findCoachId("DupTest", "DupPre");
        c.setId_coach(id);
        createdCoach = c;

        assertTrue(coachService.isCoachDuplicate("DupTest", "DupPre"),
                "Doit retourner true pour un coach existant");
        assertFalse(coachService.isCoachDuplicate("Inexistant", "Xyz"),
                "Doit retourner false pour un coach inexistant");
    }

    // =====================================================================
    // 3. UPDATE
    // =====================================================================
    @Test
    @Order(4)
    @DisplayName("updateCoach — modifie le domaine et verifie la persistance")
    void testUpdateCoach() throws SQLException {
        cleanLeftover("UpdTest", "UpdPre");

        coach c = buildTestCoach();
        c.setNom("UpdTest");
        c.setPrenom("UpdPre");
        coachService.addCoach(c);
        int id = findCoachId("UpdTest", "UpdPre");
        c.setId_coach(id);
        createdCoach = c;

        // Update fields
        c.setDomaine("E_COMMERCE");
        c.setExperience(10);
        coachService.updateCoach(c);

        // Verify
        List<coach> all = coachService.getData();
        coach updated = all.stream().filter(co -> co.getId_coach() == id).findFirst().orElse(null);
        assertNotNull(updated, "Le coach mis a jour doit exister");
        assertEquals("E_COMMERCE", updated.getDomaine());
        assertEquals(10, updated.getExperience());
    }

    // =====================================================================
    // 4. DELETE
    // =====================================================================
    @Test
    @Order(5)
    @DisplayName("deleteCoach — supprime le coach et verifie l'absence")
    void testDeleteCoach() throws SQLException {
        cleanLeftover("DelTest", "DelPre");

        coach c = buildTestCoach();
        c.setNom("DelTest");
        c.setPrenom("DelPre");
        coachService.addCoach(c);
        int id = findCoachId("DelTest", "DelPre");
        c.setId_coach(id);

        // Delete
        coachService.deleteCoach(c);

        // Verify
        boolean found = coachService.getData().stream().anyMatch(co -> co.getId_coach() == id);
        assertFalse(found, "Le coach supprime ne doit plus apparaitre");

        createdCoach = null; // already deleted
    }

    // =====================================================================
    // Util: remove leftover test data from a previous failed run
    // =====================================================================
    private void cleanLeftover(String nom, String prenom) throws SQLException {
        String sql = "DELETE FROM coach WHERE nom = ? AND prenom = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, nom);
        pst.setString(2, prenom);
        pst.executeUpdate();
    }
}

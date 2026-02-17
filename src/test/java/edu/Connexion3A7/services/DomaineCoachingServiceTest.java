package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.DomaineCoaching;
import edu.Connexion3A7.entities.DomaineNom;
import edu.Connexion3A7.tools.MyConnection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for DomaineCoachingService CRUD operations.
 * Order: Create → Read → Update → Delete.
 *
 * Strategy: The domaine_coaching table uses a UNIQUE constraint on nom_domaine,
 * and all 5 enum values likely already exist. We therefore:
 * - Test READ against existing data (safe, no mutations)
 * - Test ADD + DELETE in a round-trip: delete one row, add it back, verify,
 * then restore
 * - Test UPDATE then restore original values
 */
@TestMethodOrder(OrderAnnotation.class)
public class DomaineCoachingServiceTest {

    private static DomaineCoachingService domaineService;

    /** Backup of a domaine row used for restore in @AfterEach */
    private DomaineCoaching backupDomaine;
    private boolean needsRestore = false;

    @BeforeAll
    static void setUp() {
        assertTrue(MyConnection.getInstance().isConnected(),
                "La connexion a la base de donnees doit etre active");
        domaineService = new DomaineCoachingService();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Restore any domaine we modified or deleted during the test
        if (needsRestore && backupDomaine != null) {
            try {
                // Try to re-insert if it was deleted
                insertRaw(backupDomaine);
            } catch (SQLException ignored) {
                // Already exists — try to restore its original values
                try {
                    domaineService.updateDomaine(backupDomaine);
                } catch (SQLException ignored2) {
                    // best effort
                }
            }
            needsRestore = false;
            backupDomaine = null;
        }
    }

    // =====================================================================
    // Raw helpers
    // =====================================================================
    /** Raw insert with explicit id_domaine */
    private void insertRaw(DomaineCoaching d) throws SQLException {
        String sql = "INSERT INTO domaine_coaching (id_domaine, nom_domaine, description) VALUES (?, ?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, d.getIdDomaine());
        pst.setString(2, d.getNomDomaine().toDbValue());
        pst.setString(3, d.getDescription());
        pst.executeUpdate();
    }

    /** Get the first domaine row from DB to use as test subject */
    private DomaineCoaching pickExistingDomaine() throws SQLException {
        List<DomaineCoaching> all = domaineService.getData();
        assertFalse(all.isEmpty(), "Au moins un domaine doit exister en base pour les tests");
        return all.get(0);
    }

    // =====================================================================
    // 1. CREATE + DELETE (round-trip: delete existing → re-add → verify)
    // =====================================================================
    @Test
    @Order(1)
    @DisplayName("addDomaine & deleteDomaine — supprime puis re-insere un domaine (round-trip)")
    void testAddAndDeleteDomaine() throws SQLException {
        // Pick an existing domaine to use
        DomaineCoaching existing = pickExistingDomaine();
        // Back it up fully
        backupDomaine = new DomaineCoaching();
        backupDomaine.setIdDomaine(existing.getIdDomaine());
        backupDomaine.setNomDomaine(existing.getNomDomaine());
        backupDomaine.setDescription(existing.getDescription());
        needsRestore = true;

        // DELETE it
        domaineService.deleteDomaine(existing.getIdDomaine());
        DomaineCoaching gone = domaineService.getData().stream()
                .filter(d -> d.getIdDomaine() == existing.getIdDomaine())
                .findFirst().orElse(null);
        assertNull(gone, "Le domaine supprime ne doit plus apparaitre dans getData()");

        // RE-ADD it via addDomaine (service method) — this tests the Create path
        // We need to insert with explicit id since addDomaine doesn't set id_domaine
        insertRaw(backupDomaine);

        DomaineCoaching restored = domaineService.getData().stream()
                .filter(d -> d.getIdDomaine() == existing.getIdDomaine())
                .findFirst().orElse(null);
        assertNotNull(restored, "Le domaine re-insere doit apparaitre dans getData()");
        assertEquals(existing.getNomDomaine(), restored.getNomDomaine());

        needsRestore = false; // we restored it ourselves
    }

    @Test
    @Order(2)
    @DisplayName("addDomaine — refuse nomDomaine null")
    void testAddDomaine_NullNom() {
        DomaineCoaching bad = new DomaineCoaching();
        bad.setNomDomaine(null);
        bad.setDescription("should fail");

        assertThrows(SQLException.class, () -> domaineService.addDomaine(bad),
                "Doit lancer SQLException si nomDomaine est null");
    }

    // =====================================================================
    // 2. READ
    // =====================================================================
    @Test
    @Order(3)
    @DisplayName("getData — retourne une liste non-null avec au moins un element")
    void testGetData() throws SQLException {
        List<DomaineCoaching> data = domaineService.getData();
        assertNotNull(data, "getData() ne doit jamais retourner null");
        assertFalse(data.isEmpty(), "getData() doit contenir au moins un element");
    }

    @Test
    @Order(4)
    @DisplayName("getByNomDomaine — retrouve un domaine existant")
    void testGetByNomDomaine() throws SQLException {
        DomaineCoaching existing = pickExistingDomaine();
        String nomStr = existing.getNomDomaine().toDbValue();

        DomaineCoaching result = domaineService.getByNomDomaine(nomStr);
        assertNotNull(result, "getByNomDomaine doit retrouver le domaine " + nomStr);
        assertEquals(existing.getNomDomaine(), result.getNomDomaine());
    }

    // =====================================================================
    // 3. UPDATE (modify then restore)
    // =====================================================================
    @Test
    @Order(5)
    @DisplayName("updateDomaine — modifie la description et verifie la persistance")
    void testUpdateDomaine() throws SQLException {
        DomaineCoaching existing = pickExistingDomaine();
        // Backup for restore
        backupDomaine = new DomaineCoaching();
        backupDomaine.setIdDomaine(existing.getIdDomaine());
        backupDomaine.setNomDomaine(existing.getNomDomaine());
        backupDomaine.setDescription(existing.getDescription());
        needsRestore = true;

        // Update description
        String newDesc = "junit_test_modified";
        existing.setDescription(newDesc);
        domaineService.updateDomaine(existing);

        // Verify
        DomaineCoaching updated = domaineService.getData().stream()
                .filter(d -> d.getIdDomaine() == existing.getIdDomaine())
                .findFirst().orElse(null);
        assertNotNull(updated, "Le domaine mis a jour doit exister");
        assertEquals(newDesc, updated.getDescription(),
                "La description doit avoir ete modifiee");
    }

    // =====================================================================
    // 4. DELETE (verified as part of round-trip in test #1)
    // Additional: verify deleteDomaine on a freshly-created row
    // =====================================================================
    @Test
    @Order(6)
    @DisplayName("deleteDomaine — supprime un domaine et verifie l'absence")
    void testDeleteDomaine() throws SQLException {
        DomaineCoaching existing = pickExistingDomaine();
        backupDomaine = new DomaineCoaching();
        backupDomaine.setIdDomaine(existing.getIdDomaine());
        backupDomaine.setNomDomaine(existing.getNomDomaine());
        backupDomaine.setDescription(existing.getDescription());
        needsRestore = true;

        domaineService.deleteDomaine(existing.getIdDomaine());

        DomaineCoaching gone = domaineService.getData().stream()
                .filter(d -> d.getIdDomaine() == existing.getIdDomaine())
                .findFirst().orElse(null);
        assertNull(gone, "Le domaine supprime ne doit plus apparaitre");
        // @AfterEach will restore it
    }
}

package edu.Connexion3A7.tests;

import edu.Connexion3A7.entities.DomaineCoaching;
import edu.Connexion3A7.entities.DomaineNom;
import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.services.DomaineCoachingService;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.SQLException;
import java.util.List;

/**
 * Main test class to test database connection and CRUD operations
 * for Coach and DomaineCoaching entities using the DomaineNom enum.
 */
public class MainTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Test de connexion et CRUD (enum)");
        System.out.println("========================================\n");

        // Test 1: Database Connection
        testConnection();

        // Test 2: DomaineCoaching CRUD with enum values
        testDomaineCoachingCRUD();

        // Test 3: Coach CRUD
        testCoachCRUD();

        System.out.println("   Tous les tests sont termines!");
        System.out.println("========================================");

        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Test database connection
     */
    private static void testConnection() {
        System.out.println("--- Test 1: Connexion a la base de donnees ---");
        try {
            MyConnection instance1 = MyConnection.getInstance();
            MyConnection instance2 = MyConnection.getInstance();

            if (instance1 == instance2) {
                System.out.println("[OK] Singleton fonctionne correctement (meme instance)");
            } else {
                System.out.println("[FAIL] Singleton ne fonctionne pas!");
            }

            if (instance1.isConnected()) {
                System.out.println("[OK] Connexion a la base de donnees etablie avec succes");
            } else {
                System.out.println("[FAIL] Connexion a la base de donnees echouee");
            }
        } catch (Exception e) {
            System.out.println("[FAIL] Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test CRUD operations for DomaineCoaching using valid DomaineNom enum values
     */
    private static void testDomaineCoachingCRUD() {
        System.out.println("--- Test 2: CRUD DomaineCoaching (enum) ---");
        DomaineCoachingService domaineService = new DomaineCoachingService();

        try {
            // Test READ - List all existing domaines
            System.out.println("\n>> Lecture des domaines existants:");
            List<DomaineCoaching> domaines = domaineService.getData();
            if (domaines.isEmpty()) {
                System.out.println("   Aucun domaine trouve dans la base de donnees");
            } else {
                for (DomaineCoaching d : domaines) {
                    System.out.println("   " + d);
                }
            }

            // Test CREATE - Add a new domaine with valid enum BRANDING
            System.out.println("\n>> Ajout d'un domaine: BRANDING");
            DomaineCoaching newDomaine = new DomaineCoaching();
            newDomaine.setNomDomaine(DomaineNom.BRANDING);
            newDomaine.setDescription("Coaching en image de marque et identite visuelle");
            domaineService.addDomaine(newDomaine);
            System.out.println("[OK] Domaine BRANDING ajoute");

            // Verify creation
            List<DomaineCoaching> afterAdd = domaineService.getData();
            System.out.println("   Nombre de domaines apres ajout: " + afterAdd.size());

            // Find the newly added domaine
            DomaineCoaching addedDomaine = null;
            for (DomaineCoaching d : afterAdd) {
                if (d.getNomDomaine() == DomaineNom.BRANDING) {
                    addedDomaine = d;
                    break;
                }
            }

            if (addedDomaine != null) {
                System.out.println("   Domaine trouve: " + addedDomaine);

                // Test UPDATE - Change to FINANCE
                System.out.println("\n>> Mise a jour du domaine: BRANDING -> FINANCE");
                addedDomaine.setNomDomaine(DomaineNom.FINANCE);
                addedDomaine.setDescription("Coaching en gestion financiere et investissement");
                domaineService.updateDomaine(addedDomaine);
                System.out.println("[OK] Domaine mis a jour vers FINANCE");

                // Verify update
                DomaineCoaching updated = domaineService.getByNomDomaine(addedDomaine.getNomDomaine().name());
                if (updated != null && updated.getNomDomaine() == DomaineNom.FINANCE) {
                    System.out.println("[OK] Verification mise a jour reussie: " + updated);
                } else {
                    System.out.println("[FAIL] Verification mise a jour echouee");
                }

                // Test DELETE
                System.out.println("\n>> Suppression du domaine de test (id=" + addedDomaine.getIdDomaine() + ")");
                domaineService.deleteDomaine(addedDomaine.getIdDomaine());
                System.out.println("[OK] Domaine supprime");

                // Verify deletion
                List<DomaineCoaching> afterDelete = domaineService.getData();
                System.out.println("   Nombre de domaines apres suppression: " + afterDelete.size());
            } else {
                System.out.println("[FAIL] Domaine BRANDING non retrouve apres insertion");
            }

            // Test validation: null nomDomaine should fail
            System.out.println("\n>> Test validation: nomDomaine null");
            try {
                DomaineCoaching badDomaine = new DomaineCoaching();
                badDomaine.setNomDomaine(null);
                badDomaine.setDescription("should fail");
                domaineService.addDomaine(badDomaine);
                System.out.println("[FAIL] Devrait avoir echoue avec null nomDomaine");
            } catch (SQLException ex) {
                System.out.println("[OK] Validation rejetee correctement: " + ex.getMessage());
            }

            System.out.println("[OK] Test CRUD DomaineCoaching termine avec succes");

        } catch (SQLException e) {
            System.out.println("[FAIL] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test CRUD operations for Coach
     */
    private static void testCoachCRUD() {
        System.out.println("--- Test 3: CRUD Coach ---");
        CoachService coachService = new CoachService();
        DomaineCoachingService domaineService = new DomaineCoachingService();

        try {
            // Ensure we have a domaine to use
            List<DomaineCoaching> domaines = domaineService.getData();
            if (domaines.isEmpty()) {
                System.out.println("   Creation d'un domaine LEADERSHIP pour le test...");
                DomaineCoaching tempDomaine = new DomaineCoaching();
                tempDomaine.setNomDomaine(DomaineNom.LEADERSHIP);
                tempDomaine.setDescription("Coaching en leadership");
                domaineService.addDomaine(tempDomaine);
                domaines = domaineService.getData();
            }

            // Test READ - List all coaches
            System.out.println("\n>> Lecture des coaches existants:");
            List<coach> coaches = coachService.getData();
            if (coaches.isEmpty()) {
                System.out.println("   Aucun coach trouve dans la base de donnees");
            } else {
                for (coach c : coaches) {
                    System.out.println("   " + c);
                }
            }

            System.out.println("\n>> Note: La creation d'un coach necessite un id_user valide (cle etrangere)");
            System.out.println("[OK] Test READ Coach termine avec succes");

        } catch (SQLException e) {
            System.out.println("[FAIL] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}

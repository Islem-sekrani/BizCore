package edu.Connexion3A7.services;

import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatbotServiceTest {

    private ChatbotService service;

    @BeforeEach
    void setUp() {
        service = ChatbotService.getInstance();
    }

    @Test
    @Order(1)
    @DisplayName("getInstance retourne toujours la meme instance (Singleton)")
    void testSingleton() {
        ChatbotService second = ChatbotService.getInstance();
        assertSame(service, second, "Doit retourner la meme instance");
    }

    @Test
    @Order(2)
    @DisplayName("isConfigured() ne leve pas d'exception")
    void testIsConfiguredDoesNotThrow() {
        assertDoesNotThrow(() -> service.isConfigured());
        System.out.println("Token HuggingFace configure : " + service.isConfigured());
    }

    @Test
    @Order(3)
    @DisplayName("Erreur claire si le token HuggingFace n'est pas configure")
    void testErrorWhenNotConfigured() throws InterruptedException {
        if (service.isConfigured()) {
            System.out.println("Test ignore : token deja configure");
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorRef = new AtomicReference<>();

        service.sendMessageAsync(
            "Bonjour",
            reply -> latch.countDown(),
            error -> { errorRef.set(error); latch.countDown(); }
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Callback non recu dans le delai");
        assertNotNull(errorRef.get(), "Un message d'erreur devrait etre retourne");
        assertTrue(errorRef.get().toLowerCase().contains("token") ||
                   errorRef.get().toLowerCase().contains("configure"),
                   "Le message d'erreur devrait mentionner le token ou la configuration");
    }

    @Test
    @Order(4)
    @DisplayName("clearHistory() ne leve pas d'exception")
    void testClearHistory() {
        assertDoesNotThrow(() -> service.clearHistory());
    }

    @Test
    @Order(5)
    @DisplayName("Appel API reel vers Hugging Face (activer manuellement)")
    @Disabled("Activer avec un vrai token hf_ dans config.properties")
    void testRealApiCall() throws InterruptedException {
        assertTrue(service.isConfigured(), "Token non configure");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> replyRef = new AtomicReference<>();
        AtomicReference<String> errorRef = new AtomicReference<>();

        service.sendMessageAsync(
            "Donne un conseil bref pour un debutant en fitness.",
            reply -> { replyRef.set(reply); latch.countDown(); },
            error -> { errorRef.set(error); latch.countDown(); }
        );

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Timeout depasse");
        assertNull(errorRef.get(), "Erreur inattendue : " + errorRef.get());
        assertNotNull(replyRef.get());
        assertFalse(replyRef.get().isBlank());
        System.out.println("Reponse HuggingFace : " + replyRef.get());
    }
}

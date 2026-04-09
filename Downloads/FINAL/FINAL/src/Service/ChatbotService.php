<?php

namespace App\Service;

use App\Repository\CoachRepository;
use Symfony\Component\HttpFoundation\RequestStack;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class ChatbotService
{
    private const SESSION_KEY = 'chatbot_history';
    private const MAX_HISTORY = 20;
    private const MAX_MESSAGE_LENGTH = 1000;
    private const API_TIMEOUT = 60; // HF models may need warm-up time

    private string $systemPrompt;

    public function __construct(
        private HttpClientInterface $httpClient,
        private CoachRepository $coachRepository,
        private RequestStack $requestStack,
        private string $aiApiUrl,
        private string $aiApiKey,
        private string $aiModel,
    ) {
        $this->systemPrompt = <<<PROMPT
Tu es un assistant intelligent spécialisé en coaching, entrepreneuriat et création d'entreprise, intégré dans la plateforme Bizcore.

Ton rôle :
1. Aider les utilisateurs à choisir le meilleur coach selon leurs objectifs, budget, disponibilité et domaine.
2. Répondre aux questions générales sur l'entrepreneuriat, la stratégie startup, le marketing, le branding, la finance, l'organisation, le leadership et le e-commerce.

Règles strictes :
- Réponds TOUJOURS en français sauf si l'utilisateur demande explicitement une autre langue.
- Quand tu recommandes des coachs, utilise UNIQUEMENT la liste réelle des coachs fournie dans le contexte. Ne JAMAIS inventer de coachs qui n'existent pas.
- Si plusieurs coachs correspondent, compare-les clairement (domaine, tarif, note, expérience, disponibilité).
- Si aucun coach ne correspond parfaitement, explique-le honnêtement et suggère les options les plus proches.
- Si les données d'un coach sont incomplètes, mentionne-le plutôt que d'inventer.
- Sois concis mais utile, structuré et pratique.
- Encourage l'utilisateur et garde un ton professionnel et bienveillant.
- Pour les questions business/entrepreneuriat non liées à un coach spécifique, donne des conseils pratiques et actionnables.
- Si l'utilisateur demande quelque chose qui combine recommandation de coach ET conseil business, fais les deux.
- Utilise des emojis avec modération pour rendre tes réponses plus engageantes.
PROMPT;
    }

    /**
     * Send a user message and get the AI response.
     */
    public function sendMessage(string $userMessage): string
    {
        $userMessage = trim($userMessage);

        if ($userMessage === '') {
            throw new \InvalidArgumentException('Le message ne peut pas être vide.');
        }

        if (mb_strlen($userMessage) > self::MAX_MESSAGE_LENGTH) {
            throw new \InvalidArgumentException(
                sprintf('Le message ne peut pas dépasser %d caractères.', self::MAX_MESSAGE_LENGTH)
            );
        }

        // Build coach context from DB
        $coachContext = $this->buildCoachContext();

        // Load conversation history from session
        $history = $this->getHistory();

        // Build messages array for the LLM
        $messages = $this->buildMessages($coachContext, $history, $userMessage);

        // Call the external AI API
        $aiResponse = $this->callAiApi($messages);

        // Save to session history
        $this->addToHistory('user', $userMessage);
        $this->addToHistory('assistant', $aiResponse);

        return $aiResponse;
    }

    /**
     * Build a textual summary of all coaches from the database.
     */
    private function buildCoachContext(): string
    {
        $coaches = $this->coachRepository->findAllWithDomaine();

        if (empty($coaches)) {
            return "Aucun coach n'est actuellement enregistré dans la base de données.";
        }

        $lines = ["Voici la liste complète des coachs disponibles sur la plateforme Bizcore :\n"];

        foreach ($coaches as $index => $coach) {
            $num = $index + 1;
            $fullName = $coach->getPrenom() . ' ' . $coach->getNom();
            $domaine = $coach->getDomaine() ?? 'Non spécifié';
            $tarif = $coach->getTarifHoraire() ? $coach->getTarifHoraire() . ' DT/H' : 'Non spécifié';
            $experience = $coach->getExperienceAnnees() !== null ? $coach->getExperienceAnnees() . ' ans' : 'Non spécifié';
            $disponibilite = $coach->getDisponibilite() ?? 'Non spécifié';
            $note = $coach->getNoteMoyenne() !== null ? number_format($coach->getNoteMoyenne(), 1) . '/5' : 'Pas encore noté';
            $bio = $coach->getBiographie() ? mb_substr($coach->getBiographie(), 0, 150) : 'Aucune biographie';
            $tel = $coach->getNumTel() ?? 'Non disponible';

            $lines[] = "Coach #{$num}: {$fullName}";
            $lines[] = "  - Domaine : {$domaine}";
            $lines[] = "  - Tarif horaire : {$tarif}";
            $lines[] = "  - Expérience : {$experience}";
            $lines[] = "  - Note moyenne : {$note}";
            $lines[] = "  - Disponibilité : {$disponibilite}";
            $lines[] = "  - Téléphone : {$tel}";
            $lines[] = "  - Bio : {$bio}";
            $lines[] = "";
        }

        return implode("\n", $lines);
    }

    /**
     * Build the full messages array for the LLM API call.
     */
    private function buildMessages(string $coachContext, array $history, string $userMessage): array
    {
        $messages = [];

        // System prompt with coach context injected
        $fullSystemPrompt = $this->systemPrompt . "\n\n--- DONNÉES DES COACHS (BASE DE DONNÉES RÉELLE) ---\n" . $coachContext;
        $messages[] = ['role' => 'system', 'content' => $fullSystemPrompt];

        // Append conversation history
        foreach ($history as $entry) {
            $messages[] = [
                'role' => $entry['role'],
                'content' => $entry['content'],
            ];
        }

        // Append current user message
        $messages[] = ['role' => 'user', 'content' => $userMessage];

        return $messages;
    }

    /**
     * Call the external AI API (OpenAI-compatible format — works with Hugging Face).
     */
    private function callAiApi(array $messages): string
    {
        if (empty($this->aiApiKey) || $this->aiApiKey === 'your-api-key-here' || $this->aiApiKey === 'hf_your-token-here') {
            throw new \RuntimeException('La clé API de l\'assistant IA n\'est pas configurée. Contactez l\'administrateur.');
        }

        try {
            $response = $this->httpClient->request('POST', $this->aiApiUrl, [
                'timeout' => self::API_TIMEOUT,
                'headers' => [
                    'Content-Type' => 'application/json',
                    'Authorization' => 'Bearer ' . $this->aiApiKey,
                ],
                'json' => [
                    'model' => $this->aiModel,
                    'messages' => $messages,
                    'max_tokens' => 1024,
                    'temperature' => 0.7,
                    'stream' => false,
                ],
            ]);

            $statusCode = $response->getStatusCode();

            // Try to parse error details from response body
            if ($statusCode !== 200) {
                $errorBody = [];
                try {
                    $errorBody = $response->toArray(false);
                } catch (\Throwable $e) {
                    // ignore parse errors
                }

                $hfError = $errorBody['error'] ?? $errorBody['message'] ?? null;

                if ($statusCode === 503) {
                    // HF model is loading (cold start)
                    $estimated = $errorBody['estimated_time'] ?? null;
                    $waitMsg = $estimated ? " Temps estimé : ~{$estimated}s." : '';
                    throw new \RuntimeException('Le modèle IA est en cours de chargement.' . $waitMsg . ' Veuillez réessayer dans quelques instants.');
                }

                if ($statusCode === 429) {
                    throw new \RuntimeException('Le service IA est temporairement surchargé. Veuillez réessayer dans quelques instants.');
                }

                if ($statusCode === 422) {
                    throw new \RuntimeException('Requête invalide envoyée au service IA. ' . ($hfError ?: 'Vérifiez la configuration.'));
                }

                if ($statusCode === 401 || $statusCode === 403) {
                    throw new \RuntimeException('Le token Hugging Face est invalide ou expiré. Contactez l\'administrateur.');
                }

                if ($statusCode >= 500) {
                    throw new \RuntimeException('Le service IA est temporairement indisponible. Veuillez réessayer plus tard.');
                }

                throw new \RuntimeException('Erreur de communication avec le service IA (code ' . $statusCode . ').' . ($hfError ? ' ' . $hfError : ''));
            }

            $data = $response->toArray(false);

            // Standard OpenAI-compatible response format
            if (isset($data['choices'][0]['message']['content'])) {
                return trim($data['choices'][0]['message']['content']);
            }

            // Fallback: some HF models return in a different structure
            if (isset($data['generated_text'])) {
                return trim($data['generated_text']);
            }

            if (isset($data[0]['generated_text'])) {
                return trim($data[0]['generated_text']);
            }

            throw new \RuntimeException('Réponse invalide du service IA.');

        } catch (\RuntimeException $e) {
            throw $e; // Re-throw our own exceptions
        } catch (\Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface $e) {
            throw new \RuntimeException('Impossible de contacter le service IA. Vérifiez votre connexion internet.');
        } catch (\Throwable $e) {
            throw new \RuntimeException('Erreur inattendue lors de la communication avec l\'IA : ' . $e->getMessage());
        }
    }

    /**
     * Get conversation history from session.
     */
    private function getHistory(): array
    {
        $session = $this->requestStack->getSession();
        return $session->get(self::SESSION_KEY, []);
    }

    /**
     * Add a message to the session conversation history.
     */
    private function addToHistory(string $role, string $content): void
    {
        $session = $this->requestStack->getSession();
        $history = $session->get(self::SESSION_KEY, []);

        $history[] = [
            'role' => $role,
            'content' => $content,
        ];

        // Keep only the last N messages
        if (count($history) > self::MAX_HISTORY) {
            $history = array_slice($history, -self::MAX_HISTORY);
        }

        $session->set(self::SESSION_KEY, $history);
    }

    /**
     * Clear conversation history from session.
     */
    public function clearHistory(): void
    {
        $session = $this->requestStack->getSession();
        $session->remove(self::SESSION_KEY);
    }
}

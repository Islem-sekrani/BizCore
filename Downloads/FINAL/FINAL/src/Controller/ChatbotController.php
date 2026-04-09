<?php

namespace App\Controller;

use App\Service\ChatbotService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/chatbot')]
class ChatbotController extends AbstractController
{
    public function __construct(
        private ChatbotService $chatbotService,
    ) {}

    /**
     * Handle a user message and return the AI response.
     */
    #[Route('/message', name: 'chatbot_message', methods: ['POST'])]
    public function message(Request $request): JsonResponse
    {
        // Require authentication
        if (!$this->getUser()) {
            return $this->json([
                'success' => false,
                'message' => 'Vous devez être connecté pour utiliser l\'assistant IA.',
            ], Response::HTTP_UNAUTHORIZED);
        }

        // Parse JSON body
        $data = json_decode($request->getContent(), true);

        if (!is_array($data) || !isset($data['message'])) {
            return $this->json([
                'success' => false,
                'message' => 'Requête invalide. Le champ "message" est requis.',
            ], Response::HTTP_BAD_REQUEST);
        }

        $userMessage = trim((string) $data['message']);

        if ($userMessage === '') {
            return $this->json([
                'success' => false,
                'message' => 'Veuillez saisir un message.',
            ], Response::HTTP_BAD_REQUEST);
        }

        if (mb_strlen($userMessage) > 1000) {
            return $this->json([
                'success' => false,
                'message' => 'Votre message est trop long (maximum 1000 caractères).',
            ], Response::HTTP_BAD_REQUEST);
        }

        try {
            $aiResponse = $this->chatbotService->sendMessage($userMessage);

            return $this->json([
                'success' => true,
                'response' => $aiResponse,
            ]);

        } catch (\InvalidArgumentException $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage(),
            ], Response::HTTP_BAD_REQUEST);

        } catch (\RuntimeException $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage(),
            ], Response::HTTP_SERVICE_UNAVAILABLE);

        } catch (\Throwable $e) {
            return $this->json([
                'success' => false,
                'message' => 'Une erreur est survenue lors de la communication avec l\'assistant.',
            ], Response::HTTP_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Clear the conversation history.
     */
    #[Route('/clear', name: 'chatbot_clear', methods: ['POST'])]
    public function clear(): JsonResponse
    {
        if (!$this->getUser()) {
            return $this->json([
                'success' => false,
                'message' => 'Vous devez être connecté.',
            ], Response::HTTP_UNAUTHORIZED);
        }

        $this->chatbotService->clearHistory();

        return $this->json([
            'success' => true,
            'message' => 'Conversation réinitialisée.',
        ]);
    }
}

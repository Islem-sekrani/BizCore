<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Sends SMS via Twilio REST API using symfony/http-client (no extra SDK needed).
 * All errors are caught and logged — SMS failure never crashes the reservation flow.
 */
class TwilioSmsService
{
    private string $accountSid;
    private string $authToken;
    private string $fromNumber;
    private string $toNumber;

    public function __construct(
        private HttpClientInterface $httpClient,
        private LoggerInterface $logger,
        string $twilioAccountSid,
        string $twilioAuthToken,
        string $twilioFromNumber,
        string $twilioToNumber,
    ) {
        $this->accountSid = $twilioAccountSid;
        $this->authToken  = $twilioAuthToken;
        $this->fromNumber = $this->normalizePhone($twilioFromNumber);
        $this->toNumber   = $this->normalizePhone($twilioToNumber);
    }

    /**
     * Send an SMS for a new reservation.
     * Returns true if the message was accepted by Twilio, false otherwise.
     * Never throws — failure is only logged.
     */
    public function sendReservationSms(
        string $userName,
        string $coachName,
        string $date,
        string $statut = 'CONFIRMEE'
    ): bool {
        $body = sprintf(
            'Nouvelle réservation : %s a réservé un coaching avec %s pour le %s. Statut : %s.',
            $userName,
            $coachName,
            $date,
            $statut
        );

        return $this->send($body);
    }

    /**
     * Send a raw SMS message to the configured TWILIO_TO_NUMBER.
     */
    public function send(string $message): bool
    {
        // Skip sending if credentials are placeholders
        if (str_contains($this->accountSid, 'xxx') || str_contains($this->authToken, 'your_auth')) {
            $this->logger->info('[Twilio] Credentials are placeholders — SMS skipped (not sent).', [
                'message' => $message,
            ]);
            return false;
        }

        $url = sprintf(
            'https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json',
            $this->accountSid
        );

        try {
            $response = $this->httpClient->request('POST', $url, [
                'auth_basic' => [$this->accountSid, $this->authToken],
                'body'       => [
                    'From' => $this->fromNumber,
                    'To'   => $this->toNumber,
                    'Body' => $message,
                ],
            ]);

            $statusCode = $response->getStatusCode();
            $data = $response->toArray(throw: false);

            if ($statusCode === 201) {
                $this->logger->info('[Twilio] SMS sent successfully.', [
                    'sid'  => $data['sid'] ?? 'N/A',
                    'to'   => $this->toNumber,
                    'body' => $message,
                ]);
                return true;
            }

            $this->logger->error('[Twilio] API returned error.', [
                'status'  => $statusCode,
                'code'    => $data['code'] ?? 'N/A',
                'message' => $data['message'] ?? 'Unknown error',
            ]);
            return false;

        } catch (\Throwable $e) {
            $this->logger->error('[Twilio] Exception while sending SMS.', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);
            return false;
        }
    }

    /**
     * Normalize a phone number to E.164 format.
     * Examples: "0698123456" → "+33698123456" is NOT done here (user must pass E.164).
     * This just ensures the + prefix is present for numbers that look like international.
     */
    private function normalizePhone(string $phone): string
    {
        $phone = preg_replace('/\s+/', '', $phone);

        // If starts with 00, replace with +
        if (str_starts_with($phone, '00')) {
            $phone = '+' . substr($phone, 2);
        }

        // Ensure it starts with +
        if (!str_starts_with($phone, '+')) {
            $phone = '+' . $phone;
        }

        return $phone;
    }
}

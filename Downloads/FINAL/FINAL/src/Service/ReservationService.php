<?php

namespace App\Service;

use App\Entity\Reservation;
use App\Entity\User;
use App\Repository\CoachRepository;
use App\Repository\DisponibiliteRepository;
use App\Repository\ReservationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Psr\Log\LoggerInterface;

/**
 * Orchestrates the full reservation creation flow:
 *   1. Business-rule validation
 *   2. Persist reservation in DB
 *   3. Create in-app notification (NotificationService)
 *   4. Send SMS via Twilio (TwilioSmsService — fire-and-forget, never throws)
 */
class ReservationService
{
    public function __construct(
        private EntityManagerInterface $em,
        private ReservationRepository $reservationRepo,
        private DisponibiliteRepository $disponibiliteRepo,
        private CoachRepository $coachRepo,
        private NotificationService $notificationService,
        private TwilioSmsService $twilioSmsService,
        private LoggerInterface $logger,
    ) {}

    /**
     * Create a reservation for the given user.
     *
     * @param User   $user     The authenticated user
     * @param int    $coachId  The coach ID to reserve
     * @param string $dateStr  The session date (YYYY-MM-DD)
     *
     * @return array{success: bool, message: string, reservation?: array, notification?: array}
     */
    public function create(User $user, int $coachId, string $dateStr): array
    {
        // 1. Validate: coach exists
        $coach = $this->coachRepo->find($coachId);
        if (!$coach) {
            return ['success' => false, 'message' => 'Coach introuvable'];
        }

        // 2. Validate: coach is globally available
        if ($coach->getDisponibilite() !== 'Disponible') {
            return ['success' => false, 'message' => 'Ce coach est actuellement indisponible'];
        }

        // 3. Validate: parse date
        try {
            $seanceDate = new \DateTime($dateStr);
            $seanceDate->setTime(0, 0, 0);
        } catch (\Exception) {
            return ['success' => false, 'message' => 'Format de date invalide. Utilisez YYYY-MM-DD'];
        }

        // 4. Validate: date must not be in the past
        $today = new \DateTime('today');
        if ($seanceDate < $today) {
            return ['success' => false, 'message' => 'La date de séance doit être aujourd\'hui ou dans le futur'];
        }

        // 5. Validate: max 12 months ahead
        $maxDate = (new \DateTime())->modify('+12 months');
        if ($seanceDate > $maxDate) {
            return ['success' => false, 'message' => 'La séance doit être prévue dans les 12 prochains mois'];
        }

        // 6. Validate: no duplicate for this coach + date
        $existing = $this->reservationRepo->findOneBy([
            'coach'      => $coach,
            'dateSeance' => $seanceDate,
            'statut'     => 'CONFIRMEE',
        ]);
        if ($existing) {
            return ['success' => false, 'message' => 'Cette séance est déjà réservée'];
        }

        // 7. Validate: no double-booking for this user on the same date
        $userExisting = $this->reservationRepo->findOneBy([
            'user'       => $user,
            'dateSeance' => $seanceDate,
            'statut'     => 'CONFIRMEE',
        ]);
        if ($userExisting) {
            return ['success' => false, 'message' => 'Vous avez déjà une réservation pour ce jour'];
        }

        // 8. Validate: specific day availability (if a Disponibilite record exists for this day)
        $dispo = $this->disponibiliteRepo->findOneBy([
            'coach' => $coach,
            'jour'  => $seanceDate,
        ]);
        if ($dispo && !$dispo->isDisponible()) {
            return ['success' => false, 'message' => 'Le coach n\'est pas disponible ce jour-là'];
        }

        // 9. Save reservation
        $reservation = new Reservation();
        $reservation->setCoach($coach);
        $reservation->setUser($user);
        $reservation->setDateSeance($seanceDate);
        $reservation->setStatut('CONFIRMEE');
        $reservation->setDateReservation(new \DateTime());

        $this->em->persist($reservation);
        $this->em->flush();

        // 10. Create in-app notification
        $notification = $this->notificationService->createReservationNotification($user, $reservation);
        $unreadCount  = $this->notificationService->getUnreadCount($user);

        // 11. Send SMS (fire-and-forget — failure is logged, never crashes)
        try {
            $smsSent = $this->twilioSmsService->sendReservationSms(
                userName:  $user->getFullName(),
                coachName: $coach->getFullName(),
                date:      $seanceDate->format('Y-m-d'),
                statut:    'CONFIRMEE'
            );
        } catch (\Throwable $e) {
            $smsSent = false;
            $this->logger->error('[ReservationService] Unexpected error in TwilioSmsService.', [
                'error' => $e->getMessage(),
            ]);
        }

        $this->logger->info('[ReservationService] Reservation created.', [
            'reservationId' => $reservation->getId(),
            'userId'        => $user->getId(),
            'coachId'       => $coach->getId(),
            'date'          => $seanceDate->format('Y-m-d'),
            'smsSent'       => $smsSent,
        ]);

        return [
            'success' => true,
            'message' => 'Votre réservation a été confirmée avec succès !',
            'reservation' => [
                'id'        => $reservation->getId(),
                'dateSeance' => $reservation->getDateSeance()->format('Y-m-d'),
                'coach'     => $coach->getFullName(),
                'statut'    => $reservation->getStatut(),
            ],
            'notification' => [
                'id'          => $notification->getId(),
                'message'     => $notification->getMessage(),
                'unreadCount' => $unreadCount,
            ],
            'smsSent' => $smsSent,
        ];
    }
}

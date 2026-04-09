<?php

namespace App\Service;

use App\Entity\Notification;
use App\Entity\Reservation;
use App\Entity\User;
use App\Repository\NotificationRepository;
use Doctrine\ORM\EntityManagerInterface;

class NotificationService
{
    public function __construct(
        private EntityManagerInterface $em,
        private NotificationRepository $notificationRepo,
    ) {}

    /**
     * Create and persist an in-app notification after a successful reservation.
     */
    public function createReservationNotification(User $user, Reservation $reservation): Notification
    {
        $coachName = $reservation->getCoach()?->getFullName() ?? 'Votre coach';
        $date      = $reservation->getDateSeance()?->format('d/m/Y') ?? '?';

        $message = sprintf(
            'Votre réservation avec %s pour le %s a bien été confirmée.',
            $coachName,
            $date
        );

        return $this->createNotification($user, $message, 'reservation');
    }

    /**
     * Create and persist an in-app notification for a cancellation.
     */
    public function createCancellationNotification(User $user, Reservation $reservation): Notification
    {
        $coachName = $reservation->getCoach()?->getFullName() ?? 'votre coach';
        $date      = $reservation->getDateSeance()?->format('d/m/Y') ?? '?';

        $message = sprintf(
            'Votre réservation avec %s pour le %s a été annulée.',
            $coachName,
            $date
        );

        return $this->createNotification($user, $message, 'annulation');
    }

    /**
     * Low-level: create any notification.
     */
    public function createNotification(User $user, string $message, string $type = 'info'): Notification
    {
        $notification = new Notification();
        $notification->setUser($user);
        $notification->setMessage($message);
        $notification->setType($type);

        $this->em->persist($notification);
        $this->em->flush();

        return $notification;
    }

    /**
     * Mark a single notification as read.
     */
    public function markAsRead(Notification $notification): void
    {
        $notification->setIsRead(true);
        $this->em->flush();
    }

    /**
     * Mark ALL notifications for a user as read (bulk update).
     * Returns the number of rows updated.
     */
    public function markAllAsRead(User $user): int
    {
        return $this->notificationRepo->markAllReadByUser($user);
    }

    /**
     * Get unread count for a user.
     */
    public function getUnreadCount(User $user): int
    {
        return $this->notificationRepo->countUnreadByUser($user);
    }

    /**
     * Get recent notifications for a user (limit 20 by default).
     */
    public function getRecentNotifications(User $user, int $limit = 20): array
    {
        return $this->notificationRepo->findByUser($user, $limit);
    }
}

<?php

namespace App\Entity;

use App\Repository\ReservationRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ReservationRepository::class)]
#[ORM\Table(name: 'reservation')]
class Reservation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_reservation')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'id_user', referencedColumnName: 'id_user', nullable: false)]
    #[Assert\NotNull(message: 'L\'utilisateur est obligatoire')]
    private ?User $user = null;

    #[ORM\ManyToOne(targetEntity: Coach::class)]
    #[ORM\JoinColumn(name: 'id_coach', referencedColumnName: 'id_coach', nullable: false)]
    #[Assert\NotNull(message: 'Le coach est obligatoire')]
    private ?Coach $coach = null;

    #[ORM\Column(name: 'date_reservation', type: 'datetime')]
    private ?\DateTimeInterface $dateReservation = null;

    #[ORM\Column(name: 'date_seance', type: 'date', nullable: true)]
    #[Assert\GreaterThanOrEqual('today', message: 'La séance doit être aujourd\'hui ou dans le futur')]
    private ?\DateTimeInterface $dateSeance = null;

    #[ORM\Column(length: 20)]
    #[Assert\Choice(choices: ['CONFIRMEE', 'ANNULEE'], message: 'Statut invalide')]
    private ?string $statut = 'CONFIRMEE';

    public function __construct()
    {
        $this->dateReservation = new \DateTime();
        $this->statut = 'CONFIRMEE';
    }

    public function getId(): ?int { return $this->id; }
    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): static { $this->user = $user; return $this; }
    public function getCoach(): ?Coach { return $this->coach; }
    public function setCoach(?Coach $coach): static { $this->coach = $coach; return $this; }
    public function getDateReservation(): ?\DateTimeInterface { return $this->dateReservation; }
    public function setDateReservation(\DateTimeInterface $d): static { $this->dateReservation = $d; return $this; }
    public function getDateSeance(): ?\DateTimeInterface { return $this->dateSeance; }
    public function setDateSeance(?\DateTimeInterface $d): static { $this->dateSeance = $d; return $this; }
    public function getStatut(): ?string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
}

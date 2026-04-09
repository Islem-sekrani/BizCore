<?php

namespace App\Entity;

use App\Repository\DisponibiliteRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: DisponibiliteRepository::class)]
#[ORM\Table(name: 'disponibilite')]
class Disponibilite
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_dispo')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Coach::class)]
    #[ORM\JoinColumn(name: 'id_coach', referencedColumnName: 'id_coach', nullable: false)]
    #[Assert\NotNull(message: 'Le coach est obligatoire')]
    private ?Coach $coach = null;

    #[ORM\Column(type: 'date')]
    #[Assert\NotNull(message: 'La date est obligatoire')]
    #[Assert\GreaterThanOrEqual('today', message: 'La date doit être aujourd\'hui ou dans le futur')]
    private ?\DateTimeInterface $jour = null;

    #[ORM\Column(length: 20)]
    #[Assert\NotBlank]
    #[Assert\Choice(choices: ['Disponible', 'Indisponible'], message: 'Statut invalide')]
    private ?string $statut = 'Disponible';

    public function getId(): ?int { return $this->id; }
    public function getCoach(): ?Coach { return $this->coach; }
    public function setCoach(?Coach $coach): static { $this->coach = $coach; return $this; }
    public function getJour(): ?\DateTimeInterface { return $this->jour; }
    public function setJour(?\DateTimeInterface $jour): static { $this->jour = $jour; return $this; }
    public function getStatut(): ?string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
    public function isDisponible(): bool { return $this->statut === 'Disponible'; }
}

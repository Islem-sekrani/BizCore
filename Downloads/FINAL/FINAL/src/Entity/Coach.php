<?php

namespace App\Entity;

use App\Repository\CoachRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: CoachRepository::class)]
#[ORM\Table(name: 'coach')]
class Coach
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_coach')]
    private ?int $idCoach = null;

    #[ORM\Column(name: 'id_user', nullable: true)]
    private ?int $idUser = null;

    #[ORM\Column(name: 'id_domaine', type: 'integer', nullable: true)]
    private ?int $idDomaine = null;

    #[ORM\Column(type: 'string', length: 20)]
    #[Assert\NotBlank(message: 'Le nom ne peut pas être vide')]
    #[Assert\Length(min: 2, minMessage: 'Le nom doit contenir au minimum 2 caractères')]
    #[Assert\Regex(pattern: '/^[a-zA-ZÀ-ÿ\s]+$/', message: 'Le nom ne peut contenir que des lettres')]
    private string $nom = '';

    #[ORM\Column(type: 'string', length: 20)]
    #[Assert\NotBlank(message: 'Le prénom ne peut pas être vide')]
    #[Assert\Length(min: 2, minMessage: 'Le prénom doit contenir au minimum 2 caractères')]
    #[Assert\Regex(pattern: '/^[a-zA-ZÀ-ÿ\s]+$/', message: 'Le prénom ne peut contenir que des lettres')]
    private string $prenom = '';

    #[ORM\Column(type: 'text', nullable: true, columnDefinition: 'TEXT DEFAULT NULL')]
    #[Assert\NotBlank(message: 'La biographie ne peut pas être vide')]
    #[Assert\Length(min: 10, minMessage: 'La biographie doit contenir au minimum 10 caractères')]
    private ?string $biographie = null;

    #[ORM\Column(name: 'experience_annees', type: 'integer', nullable: true)]
    #[Assert\NotBlank(message: 'L\'expérience ne peut pas être vide')]
    #[Assert\GreaterThanOrEqual(value: 0, message: 'L\'expérience ne peut pas être négative')]
    private ?int $experienceAnnees = null;

    #[ORM\Column(name: 'tarif_horaire', type: 'decimal', precision: 10, scale: 2, nullable: true)]
    #[Assert\NotBlank(message: 'Le tarif ne peut pas être vide')]
    #[Assert\GreaterThan(value: 0, message: 'Le tarif doit être supérieur à 0')]
    private ?string $tarifHoraire = null;

    #[ORM\Column(name: 'disponibilite', type: 'string', length: 20, nullable: true)]
    #[Assert\NotBlank(message: 'La disponibilité ne peut pas être vide')]
    #[Assert\Choice(choices: ['Disponible', 'Indisponible'], message: 'La disponibilité n\'est pas valide')]
    private ?string $disponibilite = null;

    #[ORM\Column(type: 'string', length: 50, nullable: true)]
    #[Assert\NotBlank(message: 'Le domaine ne peut pas être vide')]
    private ?string $domaine = null;

    #[ORM\Column(name: 'num_tel', type: 'string', length: 20, nullable: true)]
    #[Assert\NotBlank(message: 'Le numéro de téléphone ne peut pas être vide')]
    #[Assert\Regex(pattern: '/^\+[0-9]{8,18}$/', message: 'Le numéro de téléphone n\'est pas valide')]
    private ?string $numTel = null;

    #[ORM\Column(name: 'note_moyenne', type: 'decimal', precision: 3, scale: 2, nullable: true)]
    private ?string $noteMoyenne = null;

    public function getIdCoach(): ?int
    {
        return $this->idCoach;
    }

    public function getId(): ?int
    {
        return $this->idCoach;
    }

    public function getIdUser(): ?int
    {
        return $this->idUser;
    }

    public function setIdUser(?int $idUser): static
    {
        $this->idUser = $idUser;
        return $this;
    }

    public function getIdDomaine(): ?int
    {
        return $this->idDomaine;
    }

    public function setIdDomaine(?int $idDomaine): static
    {
        $this->idDomaine = $idDomaine;
        return $this;
    }

    public function getNom(): string
    {
        return $this->nom;
    }

    public function setNom(string $nom): static
    {
        $this->nom = trim($nom);
        return $this;
    }

    public function getPrenom(): string
    {
        return $this->prenom;
    }

    public function setPrenom(string $prenom): static
    {
        $this->prenom = trim($prenom);
        return $this;
    }

    public function getBiographie(): ?string
    {
        return $this->biographie;
    }

    public function setBiographie(?string $biographie): static
    {
        $this->biographie = $biographie ? trim($biographie) : null;
        return $this;
    }

    public function getExperienceAnnees(): ?int
    {
        return $this->experienceAnnees;
    }

    public function setExperienceAnnees(?int $experienceAnnees): static
    {
        $this->experienceAnnees = $experienceAnnees;
        return $this;
    }

    public function getTarifHoraire(): ?string
    {
        return $this->tarifHoraire;
    }

    public function setTarifHoraire(?string $tarifHoraire): static
    {
        $this->tarifHoraire = $tarifHoraire;
        return $this;
    }

    public function getDisponibilite(): ?string
    {
        return $this->disponibilite;
    }

    public function setDisponibilite(?string $disponibilite): static
    {
        $this->disponibilite = $disponibilite;
        return $this;
    }

    public function getDomaine(): ?string
    {
        return $this->domaine;
    }

    public function setDomaine(?string $domaine): static
    {
        $this->domaine = $domaine;
        return $this;
    }

    public function getNumTel(): ?string
    {
        return $this->numTel;
    }

    public function setNumTel(?string $numTel): static
    {
        $this->numTel = $numTel ? trim($numTel) : null;
        return $this;
    }

    public function getNoteMoyenne(): ?string
    {
        return $this->noteMoyenne;
    }

    public function setNoteMoyenne(?string $noteMoyenne): static
    {
        $this->noteMoyenne = $noteMoyenne;
        return $this;
    }

    public function getFullName(): string
    {
        return trim($this->prenom . ' ' . $this->nom);
    }
}

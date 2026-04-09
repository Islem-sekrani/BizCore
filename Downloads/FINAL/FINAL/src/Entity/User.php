<?php

namespace App\Entity;

use App\Repository\UserRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Validator\Constraints as Assert;
use App\Entity\Role;

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\Table(name: 'users')]
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_user')]
    private ?int $id = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Le nom est obligatoire')]
    private ?string $nom = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Le prénom est obligatoire')]
    private ?string $prenom = null;

    #[ORM\Column(length: 150, unique: true)]
    #[Assert\NotBlank(message: "L'email est obligatoire")]
    #[Assert\Email(message: "Email invalide")]
    private ?string $email = null;

    #[ORM\Column(length: 255)]
    private ?string $password = null;

    #[ORM\Column(length: 20, nullable: true)]
    #[Assert\Regex(pattern: '/^\+?[0-9]{8,15}$/', message: 'Numéro de téléphone invalide')]
    private ?string $telephone = null;

    #[ORM\Column(length: 500, nullable: true)]
    private ?string $adresse = null;

    #[ORM\Column(length: 20, nullable: true)]
    private ?string $statut = 'actif';

    #[ORM\ManyToOne(targetEntity: Role::class, fetch: 'EAGER', inversedBy: 'users')]
    #[ORM\JoinColumn(name: 'id_role', referencedColumnName: 'id_role', nullable: true)]
    private ?Role $roleEntity = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateInscription = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $derniereConnexion = null;

    // Symfony Security methods
    public function getRoles(): array
    {
        // id_role 1 = ADMIN, 2 = USER (selon ta DB bizcore)
        $roles = ['ROLE_USER']; // Default role
        
        if ($this->roleEntity !== null && $this->roleEntity->getId() === 1) {
            $roles[] = 'ROLE_ADMIN';
        }
        
        return array_unique($roles);
    }

    public function getUserIdentifier(): string
    {
        return (string) $this->email;
    }

    public function eraseCredentials(): void {}

    public function getPassword(): ?string { return $this->password; }
    public function setPassword(string $password): static { $this->password = $password; return $this; }

    public function getId(): ?int { return $this->id; }
    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): static { $this->nom = $nom; return $this; }
    public function getPrenom(): ?string { return $this->prenom; }
    public function setPrenom(string $prenom): static { $this->prenom = $prenom; return $this; }
    public function getEmail(): ?string { return $this->email; }
    public function setEmail(string $email): static { $this->email = $email; return $this; }
    public function getTelephone(): ?string { return $this->telephone; }
    public function setTelephone(?string $telephone): static { $this->telephone = $telephone; return $this; }
    public function getAdresse(): ?string { return $this->adresse; }
    public function setAdresse(?string $adresse): static { $this->adresse = $adresse; return $this; }
    public function getStatut(): ?string { return $this->statut; }
    public function setStatut(?string $statut): static { $this->statut = $statut; return $this; }
    
    public function getIdRole(): ?int { return $this->roleEntity?->getId(); }
    public function setIdRole(?int $idRole): static { return $this; }
    
    public function getRoleEntity(): ?Role { return $this->roleEntity; }
    public function setRoleEntity(?Role $roleEntity): static { $this->roleEntity = $roleEntity; return $this; }
    
    public function getDateInscription(): ?\DateTimeInterface { return $this->dateInscription; }
    public function setDateInscription(?\DateTimeInterface $dateInscription): static { $this->dateInscription = $dateInscription; return $this; }
    
    public function getDerniereConnexion(): ?\DateTimeInterface { return $this->derniereConnexion; }
    public function setDerniereConnexion(?\DateTimeInterface $derniereConnexion): static { $this->derniereConnexion = $derniereConnexion; return $this; }
    
    public function isAdmin(): bool
    {
        return $this->roleEntity !== null && $this->roleEntity->getId() === 1;
    }

    public function getFullName(): string
    {
        return trim($this->prenom . ' ' . $this->nom);
    }
}

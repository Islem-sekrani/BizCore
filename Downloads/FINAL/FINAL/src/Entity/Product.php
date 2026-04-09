<?php

namespace App\Entity;

use App\Repository\ProductRepository;
use Doctrine\ORM\Mapping as ORM;

/**
 * Entité Produit - Table: produit (même DB que prod)
 */
#[ORM\Entity(repositoryClass: ProductRepository::class)]
#[ORM\Table(name: 'produit')]
class Product
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_produit', type: 'integer')]
    private int $idProduit = 0;

    #[ORM\Column(name: 'nom_produit', type: 'string', length: 200)]
    private string $nomProduit = '';

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'prix', type: 'decimal', precision: 10, scale: 2)]
    private string $prix = '0';

    #[ORM\Column(name: 'stock_disponible', type: 'integer', nullable: true)]
    private int $stockDisponible = 0;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $categorie = null;

    #[ORM\Column(name: 'image_url', type: 'string', length: 500, nullable: true)]
    private ?string $imageUrl = null;

    #[ORM\Column(type: 'string', length: 20, nullable: true)]
    private ?string $statut = null;

    public function __construct() {}

    public static function create(
        int $idProduit,
        string $nomProduit,
        string $description,
        string $prix,
        int $stockDisponible,
        string $categorie,
        ?string $imageUrl,
        string $statut
    ): self {
        $p = new self();
        $p->idProduit       = $idProduit;
        $p->nomProduit      = $nomProduit;
        $p->description     = $description;
        $p->prix            = $prix;
        $p->stockDisponible = $stockDisponible;
        $p->categorie       = $categorie;
        $p->imageUrl        = $imageUrl;
        $p->statut          = $statut;
        return $p;
    }

    public function getIdProduit(): int          { return $this->idProduit; }
    public function getNomProduit(): string      { return $this->nomProduit; }
    public function getDescription(): ?string    { return $this->description; }
    public function getPrix(): string            { return $this->prix; }
    public function getStockDisponible(): int    { return $this->stockDisponible; }
    public function getCategorie(): ?string      { return $this->categorie; }
    public function getImageUrl(): ?string       { return $this->imageUrl; }
    public function getStatut(): ?string         { return $this->statut; }

    public function setIdProduit(int $v): void          { $this->idProduit = $v; }
    public function setNomProduit(string $v): void      { $this->nomProduit = $v; }
    public function setDescription(?string $v): void    { $this->description = $v; }
    public function setPrix(string $v): void            { $this->prix = $v; }
    public function setStockDisponible(int $v): void    { $this->stockDisponible = $v; }
    public function setCategorie(?string $v): void      { $this->categorie = $v; }
    public function setImageUrl(?string $v): void       { $this->imageUrl = $v; }
    public function setStatut(?string $v): void         { $this->statut = $v; }

    public function __toString(): string
    {
        return $this->nomProduit . ' - ' . $this->prix . ' (' . $this->statut . ')';
    }
}

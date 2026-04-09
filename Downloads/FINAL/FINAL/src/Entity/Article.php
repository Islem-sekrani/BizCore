<?php

namespace App\Entity;

use App\Repository\ArticleRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ArticleRepository::class)]
#[ORM\Table(name: 'article')]
class Article
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_article', type: Types::INTEGER)]
    private ?int $id_article = null;

    #[ORM\Column(name: 'titre', length: 255)]
    private ?string $titre = null;

    #[ORM\Column(name: 'contenu', type: Types::TEXT)]
    private ?string $contenu = null;

    #[ORM\Column(name: 'image_principale', length: 255, nullable: true)]
    private ?string $image_principale = null;

    #[ORM\Column(name: 'categorie', length: 100, nullable: true)]
    private ?string $categorie = null;

    #[ORM\Column(name: 'statut', length: 20, nullable: true)]
    private ?string $statut = null;

    #[ORM\Column(name: 'nombre_vues', type: Types::INTEGER)]
    private int $nombre_vues = 0;

    public function getIdArticle(): ?int
    {
        return $this->id_article;
    }

    public function getTitre(): ?string
    {
        return $this->titre;
    }

    public function setTitre(string $titre): static
    {
        $this->titre = $titre;
        return $this;
    }

    public function getContenu(): ?string
    {
        return $this->contenu;
    }

    public function setContenu(string $contenu): static
    {
        $this->contenu = $contenu;
        return $this;
    }

    public function getImagePrincipale(): ?string
    {
        return $this->image_principale;
    }

    public function setImagePrincipale(?string $image_principale): static
    {
        $this->image_principale = $image_principale;
        return $this;
    }

    public function getCategorie(): ?string
    {
        return $this->categorie;
    }

    public function setCategorie(?string $categorie): static
    {
        $this->categorie = $categorie;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(?string $statut): static
    {
        $this->statut = $statut;
        return $this;
    }

    public function getNombreVues(): int
    {
        return $this->nombre_vues;
    }

    public function setNombreVues(int $nombre_vues): static
    {
        $this->nombre_vues = $nombre_vues;
        return $this;
    }
}
<?php

namespace App\Form;

use App\Entity\Article;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\File;

class ArticleType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre',
                'attr'  => ['placeholder' => "Titre de l'article"],
                'constraints' => [
                    new NotBlank(message: 'Le titre est obligatoire.'),
                    new Length(
                        min: 3,
                        max: 255,
                        minMessage: 'Le titre doit contenir au moins {{ limit }} caractères.',
                        maxMessage: 'Le titre ne peut pas dépasser {{ limit }} caractères.'
                    ),
                ],
            ])
            ->add('contenu', TextareaType::class, [
                'label' => 'Contenu',
                'attr'  => ['rows' => 10, 'placeholder' => "Contenu de l'article..."],
                'constraints' => [
                    new NotBlank(message: 'Le contenu est obligatoire.'),
                    new Length(
                        min: 10,
                        minMessage: 'Le contenu doit contenir au moins {{ limit }} caractères.'
                    ),
                ],
            ])
            ->add('image_principale', FileType::class, [
                'label'    => 'Image principale',
                'mapped'   => false,
                'required' => false,
                'constraints' => [
                    new File([
                        'maxSize'             => '2M',
                        'mimeTypes'           => ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
                        'mimeTypesMessage'    => 'Veuillez télécharger une image valide (JPEG, PNG, GIF, WEBP).',
                        'maxSizeMessage'      => 'Le fichier est trop volumineux. La taille maximale est 2 Mo.',
                    ]),
                ],
            ])
            ->add('categorie', ChoiceType::class, [
                'label'       => 'Catégorie',
                'required'    => false,
                'placeholder' => '— Choisir une catégorie —',
                'choices'     => [
                    'Actualité'    => 'Actualité',
                    'Technologie'  => 'Technologie',
                    'Sport'        => 'Sport',
                    'Culture'      => 'Culture',
                    'Économie'     => 'Économie',
                    'Santé'        => 'Santé',
                    'Autre'        => 'Autre',
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'label'       => 'Statut',
                'required'    => false,
                'placeholder' => '— Choisir un statut —',
                'choices'     => [
                    'Publié'    => 'publié',
                    'Brouillon' => 'brouillon',
                    'Archivé'   => 'archivé',
                ],
            ])
            ->add('nombre_vues', IntegerType::class, [
                'label' => 'Nombre de vues',
                'attr'  => ['min' => 0],
                'data'  => 0,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Article::class,
            'attr'       => ['novalidate' => 'novalidate'],
        ]);
    }
}

<?php

namespace App\Form;

use App\Entity\Coach;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\DBAL\Connection;

class CoachType extends AbstractType
{
    public function __construct(private Connection $connection)
    {
    }

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $domaines = $this->connection->fetchAllAssociative('SELECT nom_domaine FROM domaine_coaching ORDER BY nom_domaine');
        $domaineChoices = [];
        foreach ($domaines as $row) {
            $domaineChoices[$row['nom_domaine']] = $row['nom_domaine'];
        }
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom',
                'attr'  => ['class' => 'form-control', 'placeholder' => 'Entrez le nom', 'maxlength' => 20],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Le nom ne peut pas être vide']),
                    new Assert\Length(['min' => 2, 'minMessage' => 'Le nom doit contenir au minimum {{ limit }} caractères']),
                    new Assert\Regex(['pattern' => '/^[a-zA-ZÀ-ÿ\s\-]+$/', 'message' => 'Le nom ne peut contenir que des lettres']),
                ],
            ])
            ->add('prenom', TextType::class, [
                'label' => 'Prénom',
                'attr'  => ['class' => 'form-control', 'placeholder' => 'Entrez le prénom', 'maxlength' => 20],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Le prénom ne peut pas être vide']),
                    new Assert\Length(['min' => 2, 'minMessage' => 'Le prénom doit contenir au minimum {{ limit }} caractères']),
                    new Assert\Regex(['pattern' => '/^[a-zA-ZÀ-ÿ\s\-]+$/', 'message' => 'Le prénom ne peut contenir que des lettres']),
                ],
            ])
            ->add('domaine', ChoiceType::class, [
                'label'       => 'Domaine',
                'placeholder' => '-- Sélectionnez un domaine --',
                'choices'     => $domaineChoices,
                'attr'        => ['class' => 'form-select'],
                'constraints' => [new Assert\NotBlank(['message' => 'Veuillez sélectionner un domaine'])],
            ])
            ->add('experienceAnnees', IntegerType::class, [
                'label'    => "Années d'expérience",
                'required' => false,
                'attr'     => ['class' => 'form-control', 'placeholder' => 'ex: 5', 'min' => 0, 'max' => 60],
                'constraints' => [
                    new Assert\NotBlank(['message' => "L'expérience ne peut pas être vide"]),
                    new Assert\Range([
                        'min'               => 0,
                        'max'               => 60,
                        'notInRangeMessage' => "L'expérience doit être entre {{ min }} et {{ max }} ans",
                    ]),
                ],
            ])
            ->add('tarifHoraire', NumberType::class, [
                'label'    => 'Tarif horaire (TND)',
                'required' => false,
                'scale'    => 2,
                'attr'     => ['class' => 'form-control', 'placeholder' => 'ex: 120.00', 'min' => 1, 'step' => '0.01'],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Le tarif ne peut pas être vide']),
                    new Assert\GreaterThan(['value' => 0, 'message' => 'Le tarif doit être supérieur à 0']),
                    new Assert\LessThanOrEqual(['value' => 9999.99, 'message' => 'Le tarif ne peut pas dépasser 9999.99 TND']),
                ],
            ])
            ->add('disponibilite', ChoiceType::class, [
                'label'       => 'Disponibilité',
                'placeholder' => '-- Sélectionnez --',
                'choices'     => ['Disponible' => 'Disponible', 'Indisponible' => 'Indisponible'],
                'attr'        => ['class' => 'form-select'],
                'constraints' => [new Assert\NotBlank(['message' => 'Veuillez indiquer la disponibilité'])],
            ])
            ->add('numTel', TextType::class, [
                'label'    => 'Numéro de téléphone',
                'required' => false,
                'attr'     => ['class' => 'form-control', 'maxlength' => 20],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Le numéro de téléphone ne peut pas être vide']),
                    new Assert\Regex([
                        'pattern' => '/^\+[0-9]{8,18}$/',
                        'message' => 'Format invalide (attendu: indicatif + numéro local, ex: +21612345678)',
                    ]),
                ],
            ])
            ->add('biographie', TextareaType::class, [
                'label'    => 'Biographie',
                'required' => false,
                'attr'     => ['class' => 'form-control', 'rows' => 5, 'placeholder' => 'Décrivez le parcours du coach (minimum 10 caractères)'],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'La biographie ne peut pas être vide']),
                    new Assert\Length([
                        'min'        => 10,
                        'minMessage' => 'La biographie doit contenir au minimum {{ limit }} caractères',
                        'max'        => 2000,
                        'maxMessage' => 'La biographie ne peut pas dépasser {{ limit }} caractères',
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Coach::class]);
    }
}

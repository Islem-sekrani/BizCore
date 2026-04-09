<?php

namespace App\Form;

use App\Entity\Coach;
use App\Entity\Disponibilite;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DisponibiliteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('coach', EntityType::class, [
                'class'        => Coach::class,
                'choice_label' => 'fullName',
                'label'        => 'Coach',
                'attr'         => ['class' => 'form-select'],
            ])
            ->add('jour', DateType::class, [
                'label'  => 'Date',
                'widget' => 'single_text',
                'attr'   => ['class' => 'form-control'],
            ])
            ->add('statut', ChoiceType::class, [
                'label'   => 'Statut',
                'choices' => [
                    'Disponible'   => 'Disponible',
                    'Indisponible' => 'Indisponible',
                ],
                'attr' => ['class' => 'form-select'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Disponibilite::class]);
    }
}

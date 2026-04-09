<?php

namespace App\Form;

use App\Entity\Coach;
use App\Entity\Reservation;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ReservationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('coach', EntityType::class, [
                'class'        => Coach::class,
                'choice_label' => 'fullName',
                'label'        => 'Choisir un coach',
                'attr'         => ['class' => 'form-select'],
            ])
            ->add('dateSeance', DateType::class, [
                'label'  => 'Date de la séance',
                'widget' => 'single_text',
                'attr'   => ['class' => 'form-control', 'min' => date('Y-m-d', strtotime('+1 day'))],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Reservation::class]);
    }
}

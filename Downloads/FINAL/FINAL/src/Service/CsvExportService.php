<?php

namespace App\Service;

use App\Repository\CoachRepository;
use Symfony\Component\HttpFoundation\StreamedResponse;

class CsvExportService
{
    public function __construct(private CoachRepository $repo) {}

    public function exportCoachsCsv(): StreamedResponse
    {
        $coachs = $this->repo->findAll();
        $dt = new \DateTime();
        $fileName = 'coachs_export_' . $dt->format('Y-m-d_H-i') . '.csv';

        $response = new StreamedResponse(function () use ($coachs, $dt) {
            $h = fopen('php://output', 'w');
            fwrite($h, "\xEF\xBB\xBF");
            fputcsv($h, [
                'Nom', 'Prénom', 'Domaine', 'Expérience (années)',
                'Tarif Horaire (DT)', 'Disponibilité', 'Téléphone',
                'Note Moyenne'
            ], ';');
            foreach ($coachs as $c) {
                fputcsv($h, [
                    $c->getNom(),
                    $c->getPrenom(),
                    $c->getDomaine() ?? '',
                    $c->getExperienceAnnees() ?? '',
                    $c->getTarifHoraire() ?? '',
                    $c->getDisponibilite() ?? '',
                    $c->getNumTel() ?? '',
                    $c->getNoteMoyenne() ?? '',
                ], ';');
            }
            fclose($h);
        });

        $response->headers->set('Content-Type', 'text/csv; charset=UTF-8');
        $response->headers->set('Content-Disposition', 'attachment; filename="' . $fileName . '"');

        return $response;
    }
}

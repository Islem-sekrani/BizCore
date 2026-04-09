<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class PhoneCountryDetectionService
{
    /**
     * Country metadata map: ISO-2 code => [callingCode, digits, example]
     * Structured so more countries can be added easily.
     */
    private const COUNTRY_MAP = [
        'TN' => ['+216', 8,  '12345678'],
        'FR' => ['+33',  9,  '612345678'],
        'NL' => ['+31',  9,  '612345678'],
        'DE' => ['+49',  11, '15123456789'],
        'US' => ['+1',   10, '2025551234'],
        'GB' => ['+44',  10, '7911123456'],
        'BE' => ['+32',  9,  '470123456'],
        'CH' => ['+41',  9,  '791234567'],
        'IT' => ['+39',  10, '3123456789'],
        'ES' => ['+34',  9,  '612345678'],
        'PT' => ['+351', 9,  '912345678'],
        'MA' => ['+212', 9,  '612345678'],
        'DZ' => ['+213', 9,  '551234567'],
        'LY' => ['+218', 9,  '912345678'],
        'EG' => ['+20',  10, '1001234567'],
        'SA' => ['+966', 9,  '512345678'],
        'AE' => ['+971', 9,  '501234567'],
        'QA' => ['+974', 8,  '33123456'],
        'KW' => ['+965', 8,  '50012345'],
        'CA' => ['+1',   10, '6135551234'],
        'TR' => ['+90',  10, '5321234567'],
        'PL' => ['+48',  9,  '512345678'],
        'SE' => ['+46',  9,  '701234567'],
        'NO' => ['+47',  8,  '41234567'],
        'DK' => ['+45',  8,  '20123456'],
        'FI' => ['+358', 9,  '401234567'],
        'AT' => ['+43',  10, '6641234567'],
        'IE' => ['+353', 9,  '851234567'],
        'CZ' => ['+420', 9,  '601234567'],
        'RO' => ['+40',  9,  '721234567'],
        'HU' => ['+36',  9,  '201234567'],
        'GR' => ['+30',  10, '6912345678'],
    ];

    private const DEFAULT_COUNTRY = 'TN';

    public function __construct(
        private HttpClientInterface $httpClient,
    ) {}

    /**
     * Detect country from a client IP address.
     * Returns full phone metadata for that country.
     */
    public function detectFromIp(?string $ip): array
    {
        $countryCode = self::DEFAULT_COUNTRY;

        if ($ip && $ip !== '127.0.0.1' && $ip !== '::1' && !str_starts_with($ip, '192.168.') && !str_starts_with($ip, '10.')) {
            try {
                $response = $this->httpClient->request('GET', 'https://ipapi.co/' . $ip . '/json/', [
                    'timeout' => 5,
                ]);

                $data = $response->toArray(false);

                if (!isset($data['error']) && !empty($data['country_code'])) {
                    $detected = strtoupper($data['country_code']);
                    if (isset(self::COUNTRY_MAP[$detected])) {
                        $countryCode = $detected;
                    }
                    // If detected country is not in our map, fall back to default
                }
            } catch (\Throwable $e) {
                // Geolocation failed — use default
            }
        }

        return $this->buildResult($countryCode);
    }

    /**
     * Build the phone metadata result for a given country code.
     */
    public function buildResult(string $countryCode): array
    {
        $meta = self::COUNTRY_MAP[$countryCode] ?? self::COUNTRY_MAP[self::DEFAULT_COUNTRY];
        $code = $countryCode;

        // Fallback if country code is somehow not in map
        if (!isset(self::COUNTRY_MAP[$countryCode])) {
            $code = self::DEFAULT_COUNTRY;
        }

        return [
            'countryCode' => $code,
            'callingCode' => $meta[0],
            'digits'      => $meta[1],
            'example'     => $meta[2],
            'flagUrl'     => 'https://flagcdn.com/w40/' . strtolower($code) . '.png',
        ];
    }
}

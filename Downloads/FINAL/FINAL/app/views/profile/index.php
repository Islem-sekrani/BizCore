<?php
$profile = $data['profile'] ?? null;
$fullName = $profile ? trim(($profile['prenom'] ?? '') . ' ' . ($profile['nom'] ?? '')) : 'Unknown';
$role = $profile['nom_role'] ?? 'User';
$avatarLetter = $profile ? strtoupper(substr($profile['prenom'] ?? $profile['nom'] ?? 'U', 0, 1)) : 'U';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile - BizCore</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f6ff;
            min-height: 100vh;
            display: grid;
            place-items: center;
        }
        .card {
            width: min(640px, calc(100% - 32px));
            background: white;
            border-radius: 24px;
            padding: 28px;
            box-shadow: 0 20px 60px rgba(0,0,0,.10);
        }
        .head {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-bottom: 22px;
        }
        .avatar {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            display: grid;
            place-items: center;
            background: linear-gradient(135deg, #ffb199, #ff6a88);
            color: white;
            font-size: 28px;
            font-weight: 800;
        }
        h1 {
            margin: 0;
            font-size: 28px;
        }
        .role {
            color: #667085;
            margin-top: 4px;
        }
        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px;
        }
        .item {
            padding: 14px;
            border: 1px solid #e5e7ef;
            border-radius: 16px;
            background: #fafbff;
        }
        .label {
            font-size: 12px;
            color: #667085;
            margin-bottom: 6px;
        }
        .value {
            font-size: 15px;
            font-weight: 700;
            color: #111827;
            word-break: break-word;
        }
        .actions {
            margin-top: 18px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        a {
            display: inline-block;
            padding: 11px 14px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 700;
        }
        .primary {
            background: #5b7cff;
            color: white;
        }
        .secondary {
            background: #eef2ff;
            color: #111827;
        }
        @media (max-width: 640px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="head">
            <div class="avatar"><?= htmlspecialchars($avatarLetter, ENT_QUOTES, 'UTF-8') ?></div>
            <div>
                <h1><?= htmlspecialchars($fullName, ENT_QUOTES, 'UTF-8') ?></h1>
                <div class="role"><?= htmlspecialchars($role, ENT_QUOTES, 'UTF-8') ?></div>
            </div>
        </div>

        <div class="grid">
            <div class="item">
                <div class="label">Email</div>
                <div class="value"><?= htmlspecialchars($profile['email'] ?? '-', ENT_QUOTES, 'UTF-8') ?></div>
            </div>

            <div class="item">
                <div class="label">Phone</div>
                <div class="value"><?= htmlspecialchars($profile['telephone'] ?? '-', ENT_QUOTES, 'UTF-8') ?></div>
            </div>

            <div class="item">
                <div class="label">Address</div>
                <div class="value"><?= htmlspecialchars($profile['adresse'] ?? '-', ENT_QUOTES, 'UTF-8') ?></div>
            </div>

            <div class="item">
                <div class="label">Status</div>
                <div class="value"><?= htmlspecialchars($profile['statut'] ?? '-', ENT_QUOTES, 'UTF-8') ?></div>
            </div>
        </div>

        <div class="actions">
            <a class="primary" href="<?= BASE_URL ?>/dashboard">Back to dashboard</a>
            <a class="secondary" href="<?= BASE_URL ?>/auth/logout">Logout</a>
        </div>
    </div>
</body>
</html>
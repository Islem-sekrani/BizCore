<?php
function h($value)
{
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}
function percent($val, $max)
{
    return ($max > 0) ? ($val / $max) * 100 : 0;
}

$dashboard = $data ?? [];
$stats = array_merge([
    'users' => 0,
    'active_users' => 0,
    'inactive_users' => 0,
    'recent_users' => 0,
    'dormant_users' => 0,
    'without_role' => 0,
    'incomplete_profiles' => 0,
    'products' => 0,
    'orders' => 0,
    'events' => 0,
], $dashboard['stats'] ?? []);

$authUser = $dashboard['authUser'] ?? null;
$displayName = $authUser ? trim(($authUser['prenom'] ?? '') . ' ' . ($authUser['nom'] ?? '')) : 'Guest';
$displayRole = $authUser['role'] ?? 'Visitor';
$avatarLetter = $authUser ? strtoupper(substr($authUser['prenom'] ?? $authUser['nom'] ?? 'U', 0, 1)) : 'G';

$currentMonth = (int) ($_GET['month'] ?? date('n'));
$currentYear = (int) ($_GET['year'] ?? date('Y'));

$daysInMonth = cal_days_in_month(CAL_GREGORIAN, $currentMonth, $currentYear);
$firstDayOfMonth = (int) date('N', strtotime("$currentYear-$currentMonth-01"));

$prevMonth = $currentMonth - 1;
$prevYear = $currentYear;
if ($prevMonth < 1) {
    $prevMonth = 12;
    $prevYear--;
}

$nextMonth = $currentMonth + 1;
$nextYear = $currentYear;
if ($nextMonth > 12) {
    $nextMonth = 1;
    $nextYear++;
}

$currentMonthLabel = date('F Y', strtotime("$currentYear-$currentMonth-01"));

function icon_svg(string $name): string
{
    switch ($name) {
        case 'logo':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M8 12l2 2 4-4"/></svg>';
        case 'user':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 20c1.8-3.5 5-5 8-5s6.2 1.5 8 5"/></svg>';
        case 'menu':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M4 6h16M4 12h16M4 18h16"/>
    </svg>';
        case 'analytics':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M4 20V10M10 20V4M16 20v-6M22 20v-3"/>
    </svg>';
        case 'product':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 7h16v13H4z"/><path d="M8 7V5a4 4 0 0 1 8 0v2"/></svg>';
        case 'order':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="5" width="16" height="14" rx="3"/><path d="M8 9h8M8 13h5"/></svg>';
        case 'event':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg>';
        case 'article':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M8 3h8l4 4v14H8z"/><path d="M16 3v5h5"/><path d="M10 12h4M10 16h6"/></svg>';
        case 'more':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M13 5l7 7-7 7"/></svg>';
        case 'arrow':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M5 12h14"/>
        <path d="M13 5l7 7-7 7"/>
    </svg>';
        case 'calendar':
            return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="5" width="18" height="16" rx="3"/>
        <path d="M8 3v4M16 3v4M3 10h18"/>
    </svg>';
        default:
            return '';
    }
}
?>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BizCore Dashboard</title>
    <style>
        :root {
            --bg-page: #d1d1ff;
            --bg-app: #ffffff;
            --bg-card: #f6f7fb;
            --text-main: #111827;
            --text-soft: #667085;
            --line: #e5e7ef;
            --accent: #5b7cff;
            --accent-2: #8b5cf6;
            --shadow: 0 26px 70px rgba(17, 24, 39, 0.14);
        }

        * {
            box-sizing: border-box;
        }

        html,
        body {
            min-height: 100%;
        }

        body {
            margin: 0;
            font-family: Inter, Segoe UI, Arial, sans-serif;
            background: radial-gradient(circle at top left, #d7d6ff 0%, #c9caf7 36%, #bebdf0 100%);
            color: var(--text-main);
        }

        a {
            color: inherit;
            text-decoration: none;
        }

        .shell {
            min-height: 100vh;
            padding: 28px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .app {
            width: min(1500px, 100%);
            min-height: auto;
            background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
            border-radius: 28px;
            box-shadow: var(--shadow);
            display: grid;
            grid-template-columns: 300px minmax(0, 1fr) 350px;
            overflow: hidden;
        }

        .sidebar,
        .main,
        .rightbar {
            padding: 20px;
        }

        .main {
            border-left: 1px solid var(--line);
            border-right: 1px solid var(--line);
            background: linear-gradient(180deg, #ffffff 0%, #fafbff 100%);
        }

        .card {
            background: var(--bg-app);
            border: 1px solid var(--line);
            border-radius: 22px;
            box-shadow: 0 10px 28px rgba(17, 24, 39, 0.04);
        }

        .card-pad {
            padding: 18px;
        }

        .section-title {
            margin: 0 0 14px;
            font-size: 14px;
            letter-spacing: .08em;
            color: var(--text-soft);
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 18px;
        }

        .brand strong {
            display: block;
            font-size: 22px;
        }

        .sidebar-group {
            margin-bottom: 16px;
        }

        .mini-explainer {
            min-height: 250px;
        }

        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 16px 18px;
            margin-bottom: 16px;
        }

        .topbar .title strong {
            font-size: 20px;
            display: block;
        }

        .topbar .title span {
            color: var(--text-soft);
            font-size: 12px;
        }

        .clock {
            padding: 10px 14px;
            border-radius: 14px;
            background: #f2f4fb;
            font-weight: 800;
            letter-spacing: .04em;
        }

        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 14px;
            margin-bottom: 16px;
        }

        .kpi {
            padding: 18px;
            min-height: 118px;
        }

        .kpi .label {
            color: var(--text-soft);
            font-size: 12px;
        }

        .kpi .value {
            margin-top: 12px;
            font-size: 28px;
            font-weight: 800;
            letter-spacing: -0.04em;
        }

        .kpi .sub {
            margin-top: 6px;
            font-size: 12px;
            color: var(--text-soft);
        }

        .kpi .accent-line {
            height: 4px;
            border-radius: 999px;
            background: #edf1f8;
            margin-top: 14px;
            overflow: hidden;
        }

        .kpi .accent-line i {
            display: block;
            height: 100%;
            border-radius: inherit;
            background: linear-gradient(90deg, #25c18f, #51a9ff);
        }

        .right-top {
            padding: 18px;
        }

        .section-block {
            margin-bottom: 16px;
        }

        .calendar-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
        }

        .calendar-grid {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 6px;
        }

        .weekday {
            text-align: center;
            font-size: 11px;
            color: var(--text-soft);
        }

        .day {
            aspect-ratio: 1;
            border-radius: 12px;
            display: grid;
            place-items: center;
            font-size: 12px;
            background: #f6f7fb;
        }

        .day.today {
            border: 2px solid #5b7cff;
            font-weight: bold;
        }

        .calendar-header a svg {
            width: 18px;
            height: 18px;
            stroke: var(--text-main);
        }

        .calendar-header a {
            padding: 6px;
            border-radius: 8px;
        }

        .calendar-header a:hover {
            background: #eef2ff;
        }

        .calendar-nav {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .nav-btn {
            width: 34px;
            height: 34px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .nav-btn svg {
            width: 18px;
            height: 18px;
        }

        .nav-btn.prev svg {
            transform: rotate(180deg);
        }

        .nav-btn:hover {
            background: #eef2ff;
        }

        /* PROFILE HEADER */
        .right-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
        }

        .profile-head {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .profile-meta strong {
            display: block;
            font-size: 14px;
        }

        .profile-meta span {
            font-size: 12px;
            color: var(--text-soft);
        }

        .icon-btn svg {
            width: 18px;
            height: 18px;
        }

        /* DROPDOWN */
        .profile-menu-wrap {
            position: relative;
        }

        .profile-menu {
            position: absolute;
            top: 45px;
            right: 0;
            width: 180px;
            background: white;
            border: 1px solid var(--line);
            border-radius: 14px;
            box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
            display: none;
        }

        .profile-menu a,
        .profile-menu button {
            width: 100%;
            padding: 10px 12px;
            border: none;
            background: transparent;
            display: flex;
            justify-content: space-between;
            cursor: pointer;
            font-size: 13px;
        }

        .profile-menu a:hover,
        .profile-menu button:hover {
            background: #f5f7ff;
        }

        /* SHORTCUT GRID */
        .section-card {
            padding: 14px;
            border-radius: 20px;
            background: var(--bg-app);
            border: 1px solid var(--line);
        }

        .quick-icons {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 8px;
            margin-top: 8px;
        }

        .quick-icon {
            height: 65px;
            border-radius: 14px;

            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;

            gap: 4px;

            background: #f4f6ff;
            border: 1px solid var(--line);

            transition: 0.2s ease;
            font-size: 11px;
            color: var(--text-soft);
        }

        .quick-icon svg {
            width: 20px;
            height: 20px;
        }

        .quick-icon:hover {
            transform: translateY(-4px);
            background: #eef2ff;
            color: var(--text-main);
        }

        .quick-icon.active {
            background: #5b7cff;
            color: white;
        }

        .icon-btn {
            width: 40px;
            height: 40px;
            border-radius: 14px;
            border: 1px solid var(--line);
            background: #f6f7fb;

            display: flex;
            align-items: center;
            justify-content: center;

            cursor: pointer;
            transition: 0.15s ease;
        }

        .icon-btn:hover {
            background: #eef2ff;
            transform: translateY(-1px);
        }

        .avatar {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            background: linear-gradient(135deg, #ffb199, #ff6a88);
            display: grid;
            place-items: center;
            overflow: hidden;
            flex: 0 0 auto;
        }

        .avatar span {
            color: #fff;
            font-weight: 800;
            font-size: 18px;
            line-height: 1;
            transform: translateY(1px);
        }

        .avatar-lg {
            width: 72px;
            height: 72px;
        }

        .avatar-lg span {
            font-size: 28px;
        }

        .stats-vertical {
            margin-top: 14px;
        }

        .vertical-bars {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            gap: 14px;
            height: 120px;
            margin-top: 12px;
        }

        .v-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-end;
            gap: 6px;
            flex: 1;
            height: 100%;
        }

        .v-bar {
            width: 10px;
            border-radius: 999px;
            min-height: 6px;
            display: block;
        }

        .v-bar.blue {
            background: #5b7cff;
        }

        .v-bar.green {
            background: #25c18f;
        }

        .v-bar.orange {
            background: #ff9800;
        }

        .v-bar.purple {
            background: #8b5cf6;
        }

        .v-item span {
            font-size: 11px;
            font-weight: 700;
        }

        .v-item small {
            font-size: 10px;
            color: var(--text-soft);
        }

        .kpi .accent-line i {
            transition: width 0.6s ease;
        }

        #dynamicContent {
            display: none;
        }

        .modal {
            position: fixed;
            inset: 0;
            display: none;
            z-index: 9999;
        }

        .modal.open {
            display: block;
        }

        .modal-backdrop {
            position: absolute;
            inset: 0;
            background: rgba(17, 24, 39, 0.45);
            backdrop-filter: blur(3px);
        }

        .modal-card {
            position: relative;
            width: min(760px, calc(100% - 24px));
            max-height: calc(100vh - 24px);
            margin: 12px auto;
            background: #fff;
            border-radius: 24px;
            overflow: auto;
            box-shadow: 0 30px 80px rgba(0, 0, 0, .22);
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 18px 20px;
            border-bottom: 1px solid #e5e7ef;
        }

        .modal-body {
            padding: 20px;
        }

        .modal-close {
            border: 0;
            background: #f4f6ff;
            width: 36px;
            height: 36px;
            border-radius: 12px;
            cursor: pointer;
            font-size: 22px;
            line-height: 1;
        }

        .profile-summary {
            display: flex;
            align-items: center;
            gap: 14px;
            margin-bottom: 18px;
        }

        .profile-field {
            padding: 10px 12px;
            background: #fafbff;
            border: 1px solid #e5e7ef;
            border-radius: 14px;
            margin-bottom: 10px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }

        .field {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .field.full {
            grid-column: 1 / -1;
        }

        .field input {
            padding: 11px 12px;
            border-radius: 12px;
            border: 1px solid #d7ddf2;
            outline: none;
        }

        .field input:focus {
            border-color: #5b7cff;
            box-shadow: 0 0 0 3px rgba(91, 124, 255, .12);
        }

        .modal-actions {
            display: flex;
            gap: 10px;
            padding: 0 20px 20px;
        }

        .modal-footer {
            border-top: 1px solid #e5e7ef;
            padding-top: 16px;
        }

        .btn-primary,
        .btn-secondary,
        .btn-danger {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 12px;
            padding: 10px 14px;
            font-weight: 700;
            text-decoration: none;
            border: 0;
            cursor: pointer;
        }

        .btn-primary {
            background: #5b7cff;
            color: #fff;
        }

        .btn-secondary {
            background: #eef2ff;
            color: #111827;
        }

        .btn-danger {
            background: #fee2e2;
            color: #991b1b;
        }

        #profileEditForm {
            margin-top: 12px;
        }

        #profileEditForm .modal-actions {
            padding: 18px 0 0;
            justify-content: flex-end;
        }

        .overview-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 10px;
        }

        .overview-kpi {
            padding: 12px;
            border-radius: 16px;
            background: #fafbff;
            border: 1px solid #e5e7ef;
        }

        .overview-kpi span {
            display: block;
            font-size: 11px;
            color: #667085;
            margin-bottom: 6px;
        }

        .overview-kpi strong {
            font-size: 20px;
        }

        .overview-alerts {
            margin-top: 12px;
            display: grid;
            gap: 8px;
        }

        .alert-box {
            padding: 10px 12px;
            border-radius: 14px;
            font-size: 12px;
            font-weight: 700;
        }

        .alert-box.warning {
            background: #fff7e6;
            color: #9a6700;
            border: 1px solid #ffd88a;
        }

        .alert-box.danger {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fca5a5;
        }

        .alert-box.info {
            background: #e0f2fe;
            color: #075985;
            border: 1px solid #7dd3fc;
        }

        @media (max-width: 1280px) {
            .app {
                grid-template-columns: 1fr;
            }

            .main {
                border-left: 0;
                border-right: 0;
                border-top: 1px solid var(--line);
                border-bottom: 1px solid var(--line);
            }

            .kpi-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 720px) {
            .shell {
                padding: 10px;
            }

            .sidebar,
            .main,
            .rightbar {
                padding: 14px;
            }

            .kpi-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>
    <div class="shell">
        <div class="app">
            <aside class="sidebar">
                <div class="brand">
                    <strong>BiZCoRE</strong>
                </div>

                <section class="card card-pad sidebar-group">
                    <div class="section-title">Calendar</div>

                    <div class="calendar-header">
                        <span><?= h($currentMonthLabel) ?></span>

                        <div class="calendar-nav">

                            <a class="nav-btn prev" href="?month=<?= $prevMonth ?>&year=<?= $prevYear ?>">
                                <?= icon_svg('arrow'); ?>
                            </a>

                            <a class="nav-btn today" href="?month=<?= date('n') ?>&year=<?= date('Y') ?>">
                                <?= icon_svg('calendar'); ?>
                            </a>

                            <a class="nav-btn next" href="?month=<?= $nextMonth ?>&year=<?= $nextYear ?>">
                                <?= icon_svg('arrow'); ?>
                            </a>

                        </div>
                    </div>

                    <div class="calendar-grid">
                        <?php foreach (['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] as $d): ?>
                            <div class="weekday"><?= $d ?></div>
                        <?php endforeach; ?>

                        <?php for ($i = 1; $i < $firstDayOfMonth; $i++): ?>
                            <div></div>
                        <?php endfor; ?>

                        <?php for ($day = 1; $day <= $daysInMonth; $day++): ?>
                            <?php $isToday = ($day == date('j') && $currentMonth == date('n') && $currentYear == date('Y')); ?>
                            <div class="day <?= $isToday ? 'today' : '' ?>">
                                <?= $day ?>
                            </div>
                        <?php endfor; ?>
                    </div>
                </section>

                <section class="card card-pad sidebar-group mini-explainer">
                    <div class="section-title">Overview</div>

                    <div class="overview-grid">
                        <div class="overview-kpi">
                            <span>Total</span>
                            <strong><?= h($stats['users'] ?? 0) ?></strong>
                        </div>
                        <div class="overview-kpi">
                            <span>Active</span>
                            <strong><?= h($stats['active_users'] ?? 0) ?></strong>
                        </div>
                        <div class="overview-kpi">
                            <span>New 7d</span>
                            <strong><?= h($stats['recent_users'] ?? 0) ?></strong>
                        </div>
                        <div class="overview-kpi">
                            <span>Dormant</span>
                            <strong><?= h($stats['dormant_users'] ?? 0) ?></strong>
                        </div>
                    </div>

                    <?php if (($stats['without_role'] ?? 0) > 0 || ($stats['incomplete_profiles'] ?? 0) > 0): ?>
                        <div class="overview-alerts">
                            <?php if (($stats['without_role'] ?? 0) > 0): ?>
                                <div class="alert-box warning">
                                    <?= h($stats['without_role']) ?> user(s) without role
                                </div>
                            <?php endif; ?>

                            <?php if (($stats['incomplete_profiles'] ?? 0) > 0): ?>
                                <div class="alert-box danger">
                                    <?= h($stats['incomplete_profiles']) ?> incomplete profile(s)
                                </div>
                            <?php endif; ?>

                            <?php if (($stats['dormant_users'] ?? 0) > 0): ?>
                                <div class="alert-box info">
                                    <?= h($stats['dormant_users']) ?> dormant account(s)
                                </div>
                            <?php endif; ?>
                        </div>
                    <?php endif; ?>
                </section>
            </aside>

            <main class="main">
                <div class="topbar card">
                    <div class="title">
                        <strong>Dashboard</strong>
                        <span>Welcome back, <?= h($displayName) ?></span>
                    </div>
                    <div class="clock" id="liveClock">00:00:00</div>
                </div>

                <?php
                $maxKpi = max($stats['users'], $stats['products'], $stats['orders'], $stats['events'], 1);
                ?>

                <section class="kpi-grid section-block">
                    <div class="card kpi">
                        <div class="label">Total users</div>
                        <div class="value"><?= h($stats['users']) ?></div>
                        <div class="sub">Registered accounts</div>
                        <div class="accent-line">
                            <i style="width:<?= percent($stats['users'], $maxKpi) ?>%"></i>
                        </div>
                    </div>
                    <div class="card kpi">
                        <div class="label">Products</div>
                        <div class="value"><?= h($stats['products']) ?></div>
                        <div class="sub">Catalog items</div>
                        <div class="accent-line">
                            <i style="width:<?= percent($stats['products'], $maxKpi) ?>%"></i>
                        </div>
                    </div>
                    <div class="card kpi">
                        <div class="label">Orders</div>
                        <div class="value"><?= h($stats['orders']) ?></div>
                        <div class="sub">Sales activity</div>
                        <div class="accent-line">
                            <i style="width:<?= percent($stats['orders'], $maxKpi) ?>%"></i>
                        </div>
                    </div>
                    <div class="card kpi">
                        <div class="label">Events</div>
                        <div class="value"><?= h($stats['events']) ?></div>
                        <div class="sub">Upcoming sessions</div>
                        <div class="accent-line">
                            <i style="width:<?= percent($stats['events'], $maxKpi) ?>%"></i>
                        </div>
                    </div>
                </section>

                <div id="dynamicContent" class="card" style="padding:20px; margin-top:16px;">
                    <!-- dynamic views load here -->
                </div>
            </main>

            <aside class="rightbar">
                <div class="card section-block" style="padding:18px;">

                    <!-- PROFILE -->
                    <div class="right-top">

                        <div class="profile-head">
                            <div class="avatar"><span><?= h($avatarLetter) ?></span></div>
                            <div class="profile-meta">
                                <strong><?= h($displayName) ?></strong>
                                <span><?= h($displayRole) ?></span>
                            </div>
                        </div>

                        <div class="profile-menu-wrap">
                            <button class="icon-btn" id="menuBtn">
                                <?= icon_svg('menu'); ?>
                            </button>

                            <div class="profile-menu" id="menu">
                                <a href="#" onclick="openProfileModal(); return false;">Profile <span>›</span></a>
                                <a href="<?= BASE_URL ?>/auth/logout">Logout <span>↩</span></a>
                            </div>
                        </div>

                    </div>

                    <!-- SHORTCUTS -->
                    <div class="section-card" style="margin-top:14px;">
                        <div class="section-title">
                            <strong>Shortcuts</strong>
                        </div>

                        <div class="quick-icons">
                            <a href="#" class="quick-icon" onclick="toggleView('users'); return false;">
                                <?= icon_svg('user'); ?>
                                <span>Users</span>
                            </a>
                            <a href="#" class="quick-icon" onclick="toggleView('products'); return false;">
                                <?= icon_svg('product'); ?>
                                <span>Products</span>
                            </a>
                            <a href="#" class="quick-icon" onclick="toggleView('orders'); return false;">
                                <?= icon_svg('order'); ?>
                                <span>Orders</span>
                            </a>
                            <a href="#" class="quick-icon" onclick="toggleView('events'); return false;">
                                <?= icon_svg('event'); ?>
                                <span>Events</span>
                            </a>
                            <a href="#" class="quick-icon" onclick="toggleView('analytics'); return false;">
                                <?= icon_svg('analytics'); ?>
                                <span>Analytics</span>
                            </a>
                        </div>
                    </div>

                    <div class="section-card stats-vertical">
                        <div class="section-title">
                            <strong>Statistics</strong>
                        </div>

                        <?php
                        $max = $maxKpi;
                        ?>

                        <div class="vertical-bars">

                            <div class="v-item">
                                <div class="v-bar blue"
                                    style="height: <?= (percent($stats['users'], $max) * 120) / 100 ?>px"></div>
                                <span><?= $stats['users'] ?></span>
                                <small>Users</small>
                            </div>

                            <div class="v-item">
                                <div class="v-bar green"
                                    style="height: <?= (percent($stats['products'], $max) * 120) / 100 ?>px">
                                </div>
                                <span><?= $stats['products'] ?></span>
                                <small>Products</small>
                            </div>

                            <div class="v-item">
                                <div class="v-bar orange"
                                    style="height: <?= (percent($stats['orders'], $max) * 120) / 100 ?>px"></div>
                                <span><?= $stats['orders'] ?></span>
                                <small>Orders</small>
                            </div>

                            <div class="v-item">
                                <div class="v-bar purple"
                                    style="height: <?= (percent($stats['events'], $max) * 120) / 100 ?>px"></div>
                                <span><?= $stats['events'] ?></span>
                                <small>Events</small>
                            </div>

                        </div>
                    </div>
                </div>
            </aside>
        </div>
    </div>

    <script>
        const BASE_URL = "<?= BASE_URL ?>";
        const liveClock = document.getElementById('liveClock');
        let currentView = null;
        let currentSearch = '';

        function updateClock() {
            const now = new Date();
            liveClock.textContent = now.toLocaleTimeString([], {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        }

        updateClock();
        setInterval(updateClock, 1000);

        function initMenu() {
            const btn = document.getElementById('menuBtn');
            const menu = document.getElementById('menu');

            if (!btn || !menu) return;

            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
            });

            document.addEventListener('click', () => {
                menu.style.display = 'none';
            });
        }

        function loadView(view, page = 1, search = '') {
            const container = document.getElementById('dynamicContent');
            currentSearch = search ?? '';

            fetch(`${BASE_URL}/dashboard/load?view=${encodeURIComponent(view)}&page=${encodeURIComponent(page)}&search=${encodeURIComponent(currentSearch)}`)
                .then(res => {
                    if (res.status === 401) {
                        window.location.href = `${BASE_URL}/auth/login`;
                        return null;
                    }
                    return res.text();
                })
                .then(html => {
                    if (html === null) return;
                    container.innerHTML = html;
                    container.style.display = 'block';
                    currentView = view;
                })
                .catch(() => {
                    container.innerHTML = '<p style="padding:12px; color:#b91c1c;">Failed to load content.</p>';
                    container.style.display = 'block';
                });
        }


        function toggleView(view) {
            const container = document.getElementById('dynamicContent');

            if (currentView === view && container.style.display === 'block') {
                container.innerHTML = '';
                container.style.display = 'none';
                currentView = null;
                return;
            }

            loadView(view, 1, currentSearch);
        }

        function submitUsersSearch(form) {
            const search = form.search.value.trim();
            loadView('users', 1, search);
            return false;
        }

        function goToUsersPage(page) {
            const safePage = Math.max(1, parseInt(page, 10) || 1);
            loadView('users', safePage, currentSearch);
        }

        function openProfileModal() {
            document.getElementById('profileModal').classList.add('open');
            document.getElementById('profileViewBox').style.display = 'block';
            document.getElementById('profileEditForm').style.display = 'none';
            document.getElementById('profileEditBtn').textContent = 'Edit';
        }

        function closeProfileModal() {
            document.getElementById('profileModal').classList.remove('open');
        }

        function toggleProfileEdit() {
            const view = document.getElementById('profileViewBox');
            const form = document.getElementById('profileEditForm');
            const btn = document.getElementById('profileEditBtn');

            const editing = form.style.display === 'block';
            form.style.display = editing ? 'none' : 'block';
            view.style.display = editing ? 'block' : 'none';
            btn.textContent = editing ? 'Edit' : 'Back';
        }

        function openUserCreateModal() {
            const modal = document.getElementById('userModal');
            const form = document.getElementById('userForm');
            document.getElementById('userModalTitle').textContent = 'Add user';
            form.action = `${BASE_URL}/user/store`;
            form.reset();
            document.getElementById('user_password').required = true;
            modal.classList.add('open');
        }

        function openUserEditModal(user) {
            const modal = document.getElementById('userModal');
            const form = document.getElementById('userForm');
            document.getElementById('userModalTitle').textContent = 'Edit user';
            form.action = `${BASE_URL}/user/update`;

            document.getElementById('user_id').value = user.id_user || '';
            document.getElementById('user_prenom').value = user.prenom || '';
            document.getElementById('user_nom').value = user.nom || '';
            document.getElementById('user_email').value = user.email || '';
            document.getElementById('user_telephone').value = user.telephone || '';
            document.getElementById('user_adresse').value = user.adresse || '';
            document.getElementById('user_statut').value = user.statut || 'actif';
            document.getElementById('user_role').value = user.id_role || 2;
            document.getElementById('user_password').required = false;
            document.getElementById('user_password').value = '';

            modal.classList.add('open');
        }

        function closeUserModal() {
            document.getElementById('userModal').classList.remove('open');
        }

        function deleteUser(id) {
            if (!confirm('Delete this user?')) return;
            window.location.href = `${BASE_URL}/user/delete/${id}`;
        }

        document.addEventListener('DOMContentLoaded', () => {
            initMenu();
            toggleView('users');

            const container = document.getElementById('dynamicContent');

            container.addEventListener('click', (e) => {
                const btn = e.target.closest('[data-page]');
                if (!btn) return;

                e.preventDefault();
                goToUsersPage(btn.dataset.page);
            });

            container.addEventListener('submit', (e) => {
                const form = e.target;

                if (form.classList.contains('users-search-form')) {
                    e.preventDefault();
                    submitUsersSearch(form);
                    return;
                }

                if (form.classList.contains('users-page-form')) {
                    e.preventDefault();
                    const input = form.querySelector('input[name="page"]');
                    goToUsersPage(input ? input.value : 1);
                }
            });
        });
    </script>
    <div id="profileModal" class="modal">
        <div class="modal-backdrop" onclick="closeProfileModal()"></div>

        <div class="modal-card">
            <div class="modal-header">
                <h3>My Profile</h3>
                <button type="button" class="modal-close" onclick="closeProfileModal()">×</button>
            </div>

            <div class="modal-body">
                <div class="profile-summary">
                    <div class="avatar avatar-lg"><span><?= h($avatarLetter) ?></span></div>
                    <div>
                        <div style="font-weight:800; font-size:18px;"><?= h($displayName) ?></div>
                        <div style="color:#667085;"><?= h($displayRole) ?></div>
                    </div>
                </div>

                <div id="profileViewBox">
                    <div class="profile-field"><strong>Email:</strong> <?= h($authUser['email'] ?? '-') ?></div>
                    <div class="profile-field"><strong>Phone:</strong> <?= h($authUser['telephone'] ?? '-') ?></div>
                    <div class="profile-field"><strong>Address:</strong> <?= h($authUser['adresse'] ?? '-') ?></div>
                    <div class="profile-field"><strong>Status:</strong> <?= h($authUser['statut'] ?? '-') ?></div>
                </div>

                <form id="profileEditForm" method="POST" action="<?= BASE_URL ?>/profile/update" style="display:none;">
                    <div class="form-grid">
                        <div class="field">
                            <label>First name</label>
                            <input type="text" name="prenom" value="<?= h($authUser['prenom'] ?? '') ?>" required
                                minlength="2" maxlength="50" pattern="[A-Za-zÀ-ÿ\s'-]{2,50}">
                        </div>

                        <div class="field">
                            <label>Last name</label>
                            <input type="text" name="nom" value="<?= h($authUser['nom'] ?? '') ?>" required
                                minlength="2" maxlength="50" pattern="[A-Za-zÀ-ÿ\s'-]{2,50}">
                        </div>

                        <div class="field full">
                            <label>Email</label>
                            <input type="email" name="email" value="<?= h($authUser['email'] ?? '') ?>" required
                                maxlength="100">
                        </div>

                        <div class="field">
                            <label>Phone</label>
                            <input type="tel" name="telephone" value="<?= h($authUser['telephone'] ?? '') ?>"
                                maxlength="20" pattern="[0-9+\-\s]{6,20}">
                        </div>

                        <div class="field full">
                            <label>Address</label>
                            <input type="text" name="adresse" value="<?= h($authUser['adresse'] ?? '') ?>"
                                maxlength="255">
                        </div>

                        <div class="field full">
                            <label>Current password</label>
                            <input type="password" name="current_password" minlength="6"
                                autocomplete="current-password">
                        </div>

                        <div class="field">
                            <label>New password</label>
                            <input type="password" name="new_password" minlength="8" autocomplete="new-password">
                        </div>

                        <div class="field">
                            <label>Confirm password</label>
                            <input type="password" name="confirm_password" minlength="8" autocomplete="new-password">
                        </div>
                    </div>

                    <div class="modal-actions">
                        <button type="submit" class="btn-primary">Save changes</button>
                    </div>
                </form>
            </div>

            <div class="modal-actions modal-footer">
                <button type="button" class="btn-secondary" id="profileEditBtn"
                    onclick="toggleProfileEdit()">Edit</button>
                <a class="btn-danger" href="<?= BASE_URL ?>/auth/logout">Logout</a>
            </div>
        </div>
    </div>
</body>

</html>
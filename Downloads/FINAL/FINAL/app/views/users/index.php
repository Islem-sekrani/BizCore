<?php
if (!function_exists('h')) {
    function h($value)
    {
        return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
    }
}
?>

<?php if (empty($users)): ?>

    <p style="text-align:center; color:#777; margin-top:20px;">
        No users found.
    </p>

<?php else: ?>

    <style>
        .users-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .users-toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
            margin-bottom: 14px;
        }

        .users-search {
            display: flex;
            align-items: center;
            gap: 10px;
            flex: 1;
            min-width: 240px;
        }

        .users-search input {
            width: 100%;
            padding: 10px 12px;
            border-radius: 10px;
            border: 1px solid #ddd;
            outline: none;
            font-size: 13px;
            background: #fff;
        }

        .users-search input:focus {
            border-color: #5b7cff;
            box-shadow: 0 0 0 3px rgba(91, 124, 255, 0.12);
        }

        .users-table {
            width: 100%;
            min-width: 1020px;
            border-collapse: collapse;
            font-size: 13px;
            table-layout: fixed;
            border-radius: 12px;
            overflow: hidden;
        }

        .users-table th,
        .users-table td {
            padding: 11px 10px;
            border-bottom: 1px solid #eee;
            vertical-align: middle;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            text-align: left;
        }

        .users-table th:nth-child(1),
        .users-table td:nth-child(1) {
            width: 60px;
        }

        .users-table th:nth-child(2),
        .users-table td:nth-child(2) {
            width: 180px;
        }

        .users-table th:nth-child(3),
        .users-table td:nth-child(3) {
            width: 220px;
        }

        .users-table th:nth-child(4),
        .users-table td:nth-child(4) {
            width: 130px;
        }

        .users-table th:nth-child(5),
        .users-table td:nth-child(5) {
            width: 100px;
        }

        .users-table th:nth-child(6),
        .users-table td:nth-child(6) {
            width: 120px;
        }

        .users-table th:nth-child(7),
        .users-table td:nth-child(7) {
            width: 180px;
        }

        .user-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .users-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 999px;
            background: #eef2ff;
            font-size: 11px;
        }

        .pagination {
            margin-top: 16px;
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
        }

        .page-btn {
            border: 1px solid #d7ddf2;
            background: #fff;
            color: #111827;
            padding: 8px 11px;
            border-radius: 10px;
            cursor: pointer;
            font-size: 13px;
            transition: 0.15s ease;
        }

        .page-btn:hover {
            background: #f5f7ff;
            transform: translateY(-1px);
        }

        .page-input {
            width: 70px;
            padding: 8px 10px;
            border-radius: 10px;
            border: 1px solid #ddd;
        }

        .page-btn.compact {
            padding: 7px 10px;
            font-size: 12px;
        }

        .field select {
            padding: 11px 12px;
            border-radius: 12px;
            border: 1px solid #d7ddf2;
            outline: none;
            background: #fff;
        }

        #userModal .modal-actions {
            margin-top: 18px;
            padding-top: 18px;
            justify-content: flex-end;
        }
    </style>

    <div class="users-table-wrap">
        <div style="display:flex; justify-content:space-between; align-items:center; gap:10px; margin-bottom:14px;">
            <h2 style="margin:0;">Users</h2>
            <button type="button" class="page-btn" onclick="openUserCreateModal()">Add user</button>
        </div>

        <div class="users-toolbar">
            <form class="users-search-form users-search">
                <input type="text" name="search" value="<?= htmlspecialchars($search ?? '', ENT_QUOTES, 'UTF-8') ?>"
                    placeholder="Search users..." autocomplete="off">
            </form>
        </div>

        <table class="users-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Status</th>
                    <th>Registered</th>
                    <th>Actions</th>
                </tr>
            </thead>

            <tbody>
                <?php foreach ($users as $user): ?>
                    <tr>
                        <td><?= (int) $user['id_user'] ?></td>
                        <td><?= htmlspecialchars($user['nom'] . ' ' . $user['prenom'], ENT_QUOTES, 'UTF-8') ?></td>
                        <td><?= htmlspecialchars($user['email'], ENT_QUOTES, 'UTF-8') ?></td>
                        <td><?= !empty($user['telephone']) ? htmlspecialchars($user['telephone'], ENT_QUOTES, 'UTF-8') : '<span style="color:#aaa;">-</span>' ?>
                        </td>
                        <td><span
                                class="users-badge"><?= htmlspecialchars($user['statut'] ?: 'N/A', ENT_QUOTES, 'UTF-8') ?></span>
                        </td>
                        <td><?= !empty($user['date_inscription']) ? date('Y-m-d', strtotime($user['date_inscription'])) : '-' ?>
                        </td>
                        <td>
                            <button type="button" class="page-btn"
                                onclick='openUserEditModal(<?= json_encode($user, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP) ?>)'>Edit</button>
                            <button type="button" class="page-btn"
                                onclick="deleteUser(<?= (int) $user['id_user'] ?>)">Delete</button>
                        </td>
                    </tr>
                <?php endforeach; ?>
            </tbody>
        </table>

        <div class="pagination">
            <button type="button" class="page-btn" data-page="1">First</button>
            <button type="button" class="page-btn" data-page="<?= max(1, (int) $page - 1) ?>">Prev</button>

            <form class="users-page-form" style="display:flex; align-items:center; gap:8px;">
                <input class="page-input" type="number" name="page" value="<?= (int) $page ?>" min="1"
                    max="<?= (int) $totalPages ?>">
                <span>/ <?= (int) $totalPages ?></span>
                <button type="submit" class="page-btn">Go</button>
            </form>

            <button type="button" class="page-btn" data-page="<?= min((int) $totalPages, (int) $page + 1) ?>">Next</button>
            <button type="button" class="page-btn" data-page="<?= (int) $totalPages ?>">Last</button>
        </div>
    </div>

<?php endif; ?>

<div id="userModal" class="modal">
    <div class="modal-backdrop" onclick="closeUserModal()"></div>

    <div class="modal-card">
        <div class="modal-header">
            <h3 id="userModalTitle">Add user</h3>
            <button type="button" class="modal-close" onclick="closeUserModal()">×</button>
        </div>

        <div class="modal-body">
            <form id="userForm" method="POST" action="<?= BASE_URL ?>/user/store">
                <input type="hidden" name="id_user" id="user_id">

                <div class="form-grid">
                    <div class="field">
                        <label>First name</label>
                        <input type="text" name="prenom" id="user_prenom" required minlength="2" maxlength="50"
                            pattern="[A-Za-zÀ-ÿ\s'-]{2,50}">
                    </div>

                    <div class="field">
                        <label>Last name</label>
                        <input type="text" name="nom" id="user_nom" required minlength="2" maxlength="50"
                            pattern="[A-Za-zÀ-ÿ\s'-]{2,50}">
                    </div>

                    <div class="field full">
                        <label>Email</label>
                        <input type="email" name="email" id="user_email" required maxlength="100">
                    </div>

                    <div class="field">
                        <label>Password</label>
                        <input type="password" name="password" id="user_password" minlength="8" maxlength="100">
                    </div>

                    <div class="field">
                        <label>Phone</label>
                        <input type="tel" name="telephone" id="user_telephone" maxlength="20"
                            pattern="[0-9+\-\s]{6,20}">
                    </div>

                    <div class="field full">
                        <label>Address</label>
                        <input type="text" name="adresse" id="user_adresse" maxlength="255">
                    </div>

                    <div class="field full">
                        <label>Status</label>
                        <input type="text" name="statut" id="user_statut" maxlength="20" value="actif">
                    </div>

                    <div class="field full">
                        <label>Role ID</label>
                        <select name="id_role" id="user_role">
                            <option value="">No role</option>
                            <?php foreach (($roles ?? []) as $role): ?>
                                <option value="<?= (int) $role['id_role'] ?>"><?= h($role['nom_role']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                </div>

                <div class="modal-actions" style="padding:0; margin-top:16px;">
                    <button type="submit" class="btn-primary">Save</button>
                </div>
            </form>
        </div>
    </div>
</div>
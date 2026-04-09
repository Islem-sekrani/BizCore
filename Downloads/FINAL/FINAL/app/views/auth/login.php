<?php
$error = $data['error'] ?? '';
$old = $data['old'] ?? [];
?>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - BizCore</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #d7d6ff, #c9caf7);
            min-height: 100vh;
            display: grid;
            place-items: center;
        }

        .card {
            width: min(420px, calc(100% - 32px));
            background: white;
            border-radius: 24px;
            padding: 28px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, .12);
        }

        h1 {
            margin: 0 0 8px;
            font-size: 28px;
        }

        p {
            margin: 0 0 18px;
            color: #667085;
        }

        .error {
            background: #fee2e2;
            color: #991b1b;
            padding: 10px 12px;
            border-radius: 12px;
            margin-bottom: 14px;
            font-size: 14px;
        }

        .field {
            margin-bottom: 14px;
        }

        label {
            display: block;
            margin-bottom: 6px;
            font-size: 13px;
            color: #111827;
        }

        input {
            width: 100%;
            box-sizing: border-box;
            padding: 12px 14px;
            border-radius: 12px;
            border: 1px solid #d7ddf2;
            outline: none;
            font-size: 14px;
        }

        input:focus {
            border-color: #5b7cff;
            box-shadow: 0 0 0 3px rgba(91, 124, 255, .12);
        }

        button {
            width: 100%;
            padding: 12px 14px;
            border: 0;
            border-radius: 12px;
            background: #5b7cff;
            color: white;
            font-weight: 700;
            cursor: pointer;
            margin-top: 6px;
        }

        .bottom {
            margin-top: 14px;
            font-size: 14px;
            color: #667085;
            text-align: center;
        }

        a {
            color: #5b7cff;
            text-decoration: none;
            font-weight: 700;
        }
    </style>
</head>

<body>
    <div class="card">
        <h1>Welcome back</h1>
        <p>Sign in to continue to BizCore.</p>

        <?php if ($error): ?>
            <div class="error"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div>
        <?php endif; ?>

        <form method="POST" action="<?= BASE_URL ?>/auth/login">
            <div class="field">
                <label>Email</label>
                <input type="email" name="email"
                    value="<?= htmlspecialchars($old['email'] ?? '', ENT_QUOTES, 'UTF-8') ?>" required maxlength="100"
                    autocomplete="email">
            </div>

            <div class="field">
                <label>Password</label>
                <input type="password" name="password" required minlength="6" maxlength="100"
                    autocomplete="current-password">
            </div>

            <button type="submit">Login</button>
        </form>

        <div class="bottom">
            No account yet? <a href="<?= BASE_URL ?>/auth/register">Create one</a>
        </div>
    </div>
</body>

</html>
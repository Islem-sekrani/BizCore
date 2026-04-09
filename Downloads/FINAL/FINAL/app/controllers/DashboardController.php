<?php

class DashboardController extends Controller
{
    /*public function index()
    {
        $this->requireAuth();
        $this->noCache();

        $userModel = $this->model('User');
        $productModel = $this->model('Product');
        $orderModel = $this->model('Order');
        $articleModel = $this->model('Article');
        $eventModel = $this->model('Event');

        $data = [
            'stats' => [
                'users' => $userModel->countAll(),
                'products' => $productModel->countAll(),
                'orders' => $orderModel->countAll(),
                'articles' => $articleModel->countAll(),
                'events' => $eventModel->countAll(),
            ],
            'authUser' => $this->authUser(),
        ];

        $this->view('dashboard/index', $data);
    }*/

    public function index()
    {
        $this->requireAuth();
        $this->noCache();

        $userModel = $this->model('User');
        $productModel = $this->model('Product');
        $orderModel = $this->model('Order');
        $eventModel = $this->model('Event');

        $data = [
            'stats' => [
                'users' => $userModel->countAll(),
                'active_users' => $userModel->countActive(),
                'inactive_users' => $userModel->countInactive(),
                'recent_users' => $userModel->countRecentRegistrations(7),
                'dormant_users' => $userModel->countDormant(30),
                'without_role' => $userModel->countWithoutRole(),
                'incomplete_profiles' => $userModel->countIncompleteProfiles(),

                // keep these so the main KPI cards do not warn
                'products' => $productModel->countAll(),
                'orders' => $orderModel->countAll(),
                'events' => $eventModel->countAll(),
            ],
            'overview' => [
                'latest_users' => $userModel->getLatestUsers(5),
                'attention_users' => $userModel->getUsersNeedingAttention(5),
            ],
            'authUser' => $this->authUser(),
        ];

        $this->view('dashboard/index', $data);
    }

    public function load()
    {
        $this->requireAuth(true);

        $view = $_GET['view'] ?? 'users';

        switch ($view) {
            case 'users':
                require_once BASE_PATH . '/app/models/User.php';
                require_once BASE_PATH . '/app/models/Role.php';

                $userModel = new User();
                $roleModel = new Role();

                $search = trim($_GET['search'] ?? '');
                $page = max(1, (int) ($_GET['page'] ?? 1));
                $limit = 10;
                $offset = ($page - 1) * $limit;

                $totalUsers = (int) $userModel->countSearch($search);
                $totalPages = max(1, (int) ceil($totalUsers / $limit));

                if ($page > $totalPages) {
                    $page = $totalPages;
                    $offset = ($page - 1) * $limit;
                }

                $users = $userModel->searchPaginated($search, $limit, $offset);
                $roles = $roleModel->getAll();

                require BASE_PATH . '/app/views/users/index.php';
                break;

            case 'products':
                echo "<h2>Products coming soon...</h2>";
                break;

            case 'orders':
                echo "<h2>Orders coming soon...</h2>";
                break;

            case 'events':
                echo "<h2>Events coming soon...</h2>";
                break;

            case 'analytics':
                echo "<h2>Analytics coming soon...</h2>";
                break;

            default:
                echo "View not found";
        }
    }
}
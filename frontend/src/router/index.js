import { createRouter, createWebHistory } from "vue-router";
import OrderFormView from "@/views/OrderFormView.vue";
import OrderListView from "@/views/OrderListView.vue";
import LoginPageView from "@/views/LoginPageView.vue";
import AdminPageView from "@/views/AdminPageViews/AdminPageView.vue";
import KitchenPageView from "@/views/KitchenPageView.vue";
import HallPageView from "@/views/HallPageView.vue";
import RolePageView from "@/views/RolePageView.vue";
import { hasRequiredRole } from '@/utils/auth'
import PaymentPageView from "@/views/PaymentPageView.vue";
import ChangeEventView from "@/views/AdminPageViews/ChangeEventView.vue";
import AddItemOnMenuView from "@/views/AdminPageViews/AddItemOnMenuView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/inschrijvingsformulier',
            name: 'orderform',
            component: OrderFormView
        },
        {
            path: '/rollen',
            name: 'Roles',
            component: RolePageView,
            meta: {requiresAuth: true, allowedRoles: ['ADMIN','CASHIER','KITCHEN','HALL']}
        },
        {
            path:'/admin',
            name: 'admin',
            component: AdminPageView,
            meta: {requiresAuth: true, allowedRoles: ['ADMIN']}
        },
        {
            path: '/admin/voeg-toe-op-menu',
            name: 'AddItemOnMenu',
            component: AddItemOnMenuView,
            meta: {requiresAuth: true, allowedRoles: ['ADMIN']}
        },
        {
            path: '/admin/evenement',
            name: 'AddEvent',
            component: ChangeEventView,
            meta: {requiresAuth: true, allowedRoles: ['ADMIN']}
        },
        {
            path: '/keuken',
            name: 'kitchen',
            component: KitchenPageView,
            meta: {requiresAuth: true, allowedRoles: ['KITCHEN']}
        },
        {
            path: '/zaal',
            name: 'hall',
            component: HallPageView,
            meta: {requiresAuth: true, allowedRoles: ['HALL']}
        },
        {
            path: '/kassa',
            name: 'cashier',
            component: OrderListView,
            meta: {requiresAuth: true, allowedRoles: ['CASHIER']}
        },
        {
            path: '/kassa/:id',
            name: 'paymentpage',
            component: PaymentPageView,
            meta: {requiresAuth: true, allowedRoles: ['CASHIER']}
        },
        {
            path: '/',
            name: 'LoginPage',
            component: LoginPageView
        },
        {
            path: '/unauthorized',
            name: 'Unauthorized',
            component: () => import('@/views/UnauthorizedView.vue')
        }
    ]
})

router.beforeEach((to, from, next) => {
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
    const allowedRoles = to.meta.allowedRoles || []

    if (requiresAuth) {
        const token = localStorage.getItem('token')

        if (!token) {
            next('/')
            return
        }

        if (allowedRoles.length > 0 && !hasRequiredRole(allowedRoles)) {
            next('/unauthorized')
            return
        }
    }

    next()
})

export default router
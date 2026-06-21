import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/LoginPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/RegisterPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('../layouts/AuthenticatedLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'invite',
        name: 'Invite',
        component: () => import('../pages/InvitePage.vue'),
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../pages/DashboardPage.vue'),
      },
      {
        path: 'timeline',
        name: 'Timeline',
        component: () => import('../pages/TimelinePage.vue'),
      },
      {
        path: 'event/new',
        name: 'EventNew',
        component: () => import('../pages/EventFormPage.vue'),
      },
      {
        path: 'event/:id',
        name: 'EventDetail',
        component: () => import('../pages/EventDetailPage.vue'),
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('../pages/MessagesPage.vue'),
      },
      {
        path: 'messages/:userId',
        name: 'MessageDetail',
        component: () => import('../pages/MessageDetailPage.vue'),
      },
      {
        path: 'album',
        name: 'Album',
        component: () => import('../pages/AlbumPage.vue'),
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../pages/SettingsPage.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const authPages = ['/login', '/register']

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some((route) => route.meta?.requiresAuth)

  if (requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (token && authPages.includes(to.path)) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router

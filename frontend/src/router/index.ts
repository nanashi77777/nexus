import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/workspace'
    },
    {
      path: '/workspace',
      name: 'Workspace',
      component: () => import('@/views/workspace/WorkspaceView.vue')
    },
    {
      path: '/knowledge',
      name: 'Knowledge',
      component: () => import('@/views/knowledge/KnowledgeView.vue')
    },
    {
      path: '/agent',
      name: 'Agent',
      component: () => import('@/views/agent/AgentView.vue')
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/settings/SettingsView.vue')
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    }
  ]
})

export default router

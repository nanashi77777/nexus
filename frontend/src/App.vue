<template>
  <div :class="['app-container', { 'dark-mode': isDarkMode }]">
    <header class="app-header">
      <div class="header-container">
        <div class="logo">
          <h1>Nexus 智能助手</h1>
        </div>
        <nav class="main-nav">
          <router-link to="/workspace" class="nav-link">学习空间</router-link>
          <router-link to="/knowledge" class="nav-link">知识图谱</router-link>
          <router-link to="/agent" class="nav-link">智能体对话</router-link>
          <router-link to="/settings" class="nav-link">设置</router-link>
        </nav>
        <div class="header-actions">
          <button class="theme-toggle" @click="toggleDarkMode">
            {{ isDarkMode ? '☀️' : '🌙' }}
          </button>
          <button class="logout-btn" v-if="isAuthenticated" @click="handleLogout">
            退出登录
          </button>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="app-footer">
      <p>© 2026 Nexus 智能助手. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const isDarkMode = ref(false)

const isAuthenticated = computed(() => authStore.isAuthenticated())

const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
  updateTheme()
}

const updateTheme = () => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

const handleLogout = () => {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  isDarkMode.value = savedTheme === 'dark'
  updateTheme()
})
</script>

<style>
:root {
  --primary-color: #007bff;
  --bg-color: #f4f7f6;
  --text-color: #333;
  --header-bg: white;
  --footer-bg: #f8f9fa;
  --card-bg: white;
  --border-color: #e0e0e0;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dark {
  --primary-color: #4dabf7;
  --bg-color: #1a1a2e;
  --text-color: #e0e0e0;
  --header-bg: #16213e;
  --footer-bg: #16213e;
  --card-bg: #0f3460;
  --border-color: #2d3748;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
}

.app-header {
  background-color: var(--header-bg);
  box-shadow: var(--shadow);
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
}

.logo h1 {
  font-size: 1.5rem;
  margin: 0;
  color: var(--primary-color);
}

.main-nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: var(--text-color);
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.nav-link:hover {
  background-color: var(--primary-color);
  color: white;
}

.nav-link.router-link-active {
  background-color: var(--primary-color);
  color: white;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.theme-toggle {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.theme-toggle:hover {
  background-color: var(--primary-color);
  color: white;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.logout-btn:hover {
  background-color: #c82333;
}

.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 20px auto;
  padding: 0 20px;
}

.app-footer {
  background-color: var(--footer-bg);
  padding: 20px;
  text-align: center;
  border-top: 1px solid var(--border-color);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    height: auto;
    padding: 10px 0;
  }

  .logo h1 {
    font-size: 1.2rem;
    margin-bottom: 10px;
  }

  .main-nav {
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
    margin: 10px 0;
  }

  .nav-link {
    padding: 6px 10px;
    font-size: 0.9rem;
  }

  .header-actions {
    margin-top: 10px;
  }

  .app-main {
    padding: 0 10px;
  }
}

@media (max-width: 480px) {
  .logo h1 {
    font-size: 1rem;
  }

  .main-nav {
    gap: 5px;
  }

  .nav-link {
    padding: 4px 8px;
    font-size: 0.8rem;
  }
}
</style>

<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'sidebar-collapsed': isCollapse }">
      <div class="logo">
        <span class="logo-text">Estudy</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <!-- 使用路由配置生成菜单 -->
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 没有子菜单的情况 -->
          <el-menu-item
            v-if="!route.children || route.children.length === 0"
            :index="route.path"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>
              <span>{{ route.meta?.title || route.name }}</span>
            </template>
          </el-menu-item>

          <!-- 有子菜单的情况 -->
          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title || route.name }}</span>
            </template>

            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              <template #title>
                <span>{{ child.meta?.title || child.name }}</span>
              </template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <div
      class="main-container"
      :class="{ 'main-container-collapsed': isCollapse }"
    >
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="hamburger-container" @click="toggleSideBar">
          <el-icon>
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
        </div>

        <!-- 显示当前路由信息 -->
        <div class="breadcrumb-container">
          <span class="current-route">当前位置：{{ pageTitle }}</span>
        </div>

        <div class="right-menu">
          <!-- 用户信息 -->
          <el-dropdown class="avatar-container" trigger="click">
            <div class="avatar-wrapper">
              <el-icon><User /></el-icon>
              <span class="user-name">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="key" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Expand,
  Fold,
  ArrowDown,
  User,
  SwitchButton,
  HomeFilled,
  Setting,
  Document
} from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()

const router = useRouter()
const route = useRoute()

// 响应式数据
const isCollapse = ref(false)

// 模拟路由菜单数据 (在实际项目中，这应该来自路由配置)
const menuRoutes = computed(() => [
  {
    path: '/home',
    name: '首页',
    meta: { title: '首页', icon: HomeFilled }
  },
  {
    path: '/category',
    name: '分类管理',
    meta: { title: '分类管理', icon: Document }
  },
  {
    path: '/exam',
    name: '考试管理',
    meta: { title: '考试管理', icon: Document },
    children: [
      {
        path: '/exam/paper',
        name: '试卷列表',
        meta: { title: '试卷列表' }
      },
      {
        path: '/exam/question',
        name: '题目列表',
        meta: { title: '题目列表' }
      },
      {
        path: '/exam/compose',
        name: '组卷系统',
        meta: { title: '组卷系统' }
      }
    ]
  },
  {
    path: '/user',
    name: '用户管理',
    meta: { title: '用户管理', icon: User },
    children: [
      {
        path: '/user/list',
        name: '用户列表',
        meta: { title: '用户列表' }
      },
      {
        path: '/user/examRecord',
        name: '考试记录',
        meta: { title: '考试记录' }
      }
    ]
  },
  {
    path: '/system',
    name: '系统设置',
    meta: { title: '系统设置', icon: Setting }
  }
])

// 获取当前路由信息
const currentRouteInfo = computed(() => {
  return {
    path: route.path,
    name: route.name,
    params: route.params,
    query: route.query,
    meta: route.meta,
    fullPath: route.fullPath
  }
})

// 获取页面标题
const pageTitle = computed(() => {
  return route.meta?.title || route.name || 'Estudy管理系统'
})

// 递归查找当前路由的层级信息
const findRouteHierarchy = (routes, targetPath, hierarchy = []) => {
  for (const route of routes) {
    if (route.path === targetPath) {
      hierarchy.push(route)
      return hierarchy
    }
    if (route.children) {
      const result = findRouteHierarchy(route.children, targetPath, [
        ...hierarchy,
        route
      ])
      if (result) return result
    }
  }
  return null
}

// 获取路由层级信息
const routeHierarchy = computed(() => {
  return findRouteHierarchy(menuRoutes.value, route.path) || []
})

// 计算属性
const activeMenu = computed(() => route.path)
const key = computed(() => route.path)

// 方法
const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 退出登录逻辑
    let result = await proxy.Request({
      url: proxy.Api.logout
    })
    if (!result) {
      return
    }
    sessionStorage.removeItem('token')
    proxy.Message.success('已退出登录')
    router.push('/login')
  })
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  display: flex;

  .sidebar {
    width: 210px;
    height: 100%;
    background-color: #304156;
    transition: width 0.28s;
    overflow: hidden;

    &.sidebar-collapsed {
      width: 64px;
    }

    .logo {
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #2b2f3a;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      transition: 0.5s;

      img {
        width: 32px;
        height: 32px;
        margin-right: 8px;
      }

      .logo-mini {
        font-size: 16px;
      }
    }

    .sidebar-menu {
      border: none;
      height: calc(100% - 50px);
      overflow-y: auto;
    }
  }

  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    transition: margin-left 0.28s;

    .navbar {
      height: 50px;
      background-color: #fff;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
      display: flex;
      align-items: center;
      padding: 0 15px;

      .hamburger-container {
        font-size: 20px;
        cursor: pointer;
        margin-right: 15px;

        &:hover {
          color: #409eff;
        }
      }

      .breadcrumb-container {
        flex: 1;
        display: flex;
        align-items: center;

        .current-route {
          font-size: 14px;
          color: #606266;
          margin-right: 8px;
        }

        .route-path {
          font-size: 12px;
          color: #909399;
        }
      }

      .right-menu {
        display: flex;
        align-items: center;

        .right-menu-item {
          padding: 0 8px;
          font-size: 18px;
          color: #5a5e66;
          cursor: pointer;

          &:hover {
            color: #409eff;
          }
        }

        .avatar-container {
          margin-left: 10px;

          .avatar-wrapper {
            display: flex;
            align-items: center;
            cursor: pointer;

            .user-avatar {
              width: 30px;
              height: 30px;
              border-radius: 50%;
              margin-right: 8px;
            }

            .user-name {
              margin: 0 5px;
              font-size: 14px;
            }
          }
        }
      }
    }

    .app-main {
      flex: 1;
      padding: 20px;
      overflow: auto;
      background-color: #f0f2f5;
    }
  }
}

:deep(.el-dropdown-menu__item--divided) {
  margin: 0;
}

// 过渡动画
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>

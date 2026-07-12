import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: '登录',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('../views/layout/Layout.vue'),
      redirect: '/home',
      children: [
        {
          path: '/home',
          name: '首页',
          component: () => import('../views/home/Home.vue')
        },
        {
          path: '/category',
          name: '分类',
          component: () => import('../views/category/Category.vue')
        },
        {
          path: '/exam',
          name: '考试',
          children: [
            {
              path: '/exam/paper',
              name: '试卷列表',
              component: () => import('../views/exam/Paper.vue')
            },
            {
              path: '/exam/question',
              name: '题目列表',
              component: () => import('../views/exam/Question.vue')
            },
            {
              path: '/exam/compose',
              name: '组卷系统',
              component: () => import('../views/exam/ComposePaper.vue')
            }
          ]
        },
        {
          path: '/user',
          name: '用户',
          children: [
            {
              path: '/user/list',
              name: '用户列表',
              component: () => import('../views/user/UserList.vue')
            },
            {
              path: '/user/examRecord',
              name: '考试记录',
              component: () => import('../views/user/ExamRecord.vue')
            }
          ]
        },
        {
          path: '/system',
          name: '系统设置',
          component: () => import('../views/system/System.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  if (!token && to.path !== '/login') {
    router.push('/login')
  }
  next()
})

export default router

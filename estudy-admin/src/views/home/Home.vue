<template>
  <div class="home-container">
    <!-- 数据概览卡片 -->
    <div class="overview-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon user-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.userCount }}</div>
                <div class="stat-label">用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon paper-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.paperCount }}</div>
                <div class="stat-label">试卷总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon question-icon">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.questionCount }}</div>
                <div class="stat-label">题目总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon exam-icon">
                <el-icon><DataLine /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.todayExamCount }}</div>
                <div class="stat-label">今日考试人数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <!-- 饼图：各分类试卷占比 -->
        <el-col :xs="24" :sm="12" :md="12" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <span>各分类试卷占比</span>
              </div>
            </template>
            <div class="chart-wrapper">
              <div ref="pieChartRef" class="chart" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>

        <!-- 折线图：近一周考试人数 -->
        <el-col :xs="24" :sm="12" :md="12" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <span>近一周考试人数</span>
              </div>
            </template>
            <div class="chart-wrapper">
              <div ref="lineChartRef" class="chart" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 底部统计信息 -->
    <div class="bottom-stats">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="info-header">
                <el-icon><TrendCharts /></el-icon>
                <span>考试统计</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <span class="info-label">总考试次数：</span>
                <span class="info-value">{{ statsData.totalExamCount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">通过人数：</span>
                <span class="info-value">{{ statsData.passCount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">及格率：</span>
                <span class="info-value">{{ statsData.passRate }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="info-header">
                <el-icon><Clock /></el-icon>
                <span>活跃度统计</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <span class="info-label">今日活跃用户：</span>
                <span class="info-value">{{ statsData.todayActiveUsers }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">本周活跃用户：</span>
                <span class="info-value">{{ statsData.weekActiveUsers }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">月活跃用户：</span>
                <span class="info-value">{{ statsData.monthActiveUsers }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="info-header">
                <el-icon><Star /></el-icon>
                <span>积分统计</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <span class="info-label">总积分：</span>
                <span class="info-value">{{ statsData.totalPoints }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">平均积分：</span>
                <span class="info-value">{{ statsData.avgPoints }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">最高积分：</span>
                <span class="info-value">{{ statsData.maxPoints }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  onMounted,
  onUnmounted,
  nextTick,
  getCurrentInstance
} from 'vue'
import {
  User,
  Document,
  Collection,
  DataLine,
  TrendCharts,
  Clock,
  Star
} from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()

// 引入 ECharts
let echarts = null

// DOM 引用
const pieChartRef = ref(null)
const lineChartRef = ref(null)

// 图表实例
let pieChart = null
let lineChart = null

// 数据概览
const overviewData = reactive({
  userCount: 0,
  paperCount: 0,
  questionCount: 0,
  todayExamCount: 0
})

// 统计信息
const statsData = reactive({
  totalExamCount: 0,
  passCount: 0,
  passRate: 0,
  todayActiveUsers: 0,
  weekActiveUsers: 0,
  monthActiveUsers: 0,
  totalPoints: 0,
  avgPoints: 0,
  maxPoints: 0
})

// 饼图数据
const pieChartData = ref([])

// 折线图数据
const lineChartData = ref([])

// 窗口大小变化处理
const handleResize = () => {
  if (pieChart) {
    pieChart.resize()
  }
  if (lineChart) {
    lineChart.resize()
  }
}

// 加载概览数据
const loadOverviewData = async () => {
  try {
    // TODO: 调用获取概览数据API
    let result = await proxy.Request({
      url: proxy.Api.getOverviewData
    })
    if (!result) {
      return
    }
    Object.assign(overviewData, result.data)
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 加载图表数据
const loadChartData = async () => {
  try {
    // 各分类试卷占比
    let categoryData = await proxy.Request({
      url: proxy.Api.getCategoryList
    })
    if (!categoryData) {
      return
    }
    pieChartData.value = categoryData.data.map((item) => {
      return { name: item.name, value: item.count }
    })

    // 近一周考试人数
    let weeklyExamData = await proxy.Request({
      url: proxy.Api.getWeekExamData
    })
    if (!weeklyExamData) {
      return
    }
    const dates = []
    const counts = []
    Object.keys(weeklyExamData.data)
      .sort()
      .forEach((date) => {
        dates.push(date)
        counts.push(weeklyExamData.data[date])
      })
    lineChartData.value = {
      dates: dates,
      counts: counts
    }

    statsData.weekActiveUsers = counts.reduce((acc, curr) => acc + curr, 0)
  } catch (error) {
    console.error('获取图表数据失败:', error)
  }
}

// 加载统计信息
const loadStatsData = async () => {
  try {
    let result = await proxy.Request({
      url: proxy.Api.getStatsData
    })
    if (!result) {
      return
    }
    Object.assign(statsData, result.data)
    statsData.passRate = (statsData.passRate * 100).toFixed(2)
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: pieChartData.value.map((item) => item.name)
    },
    series: [
      {
        name: '试卷分类',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: pieChartData.value
      }
    ],
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272']
  }

  pieChart.setOption(option)
}

// 初始化折线图
const initLineChart = () => {
  if (!lineChartRef.value) return

  lineChart = echarts.init(lineChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: lineChartData.value.dates,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '考试人数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#5470c6'
        },
        lineStyle: {
          color: '#5470c6',
          width: 3
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(84, 112, 198, 0.5)'
              },
              {
                offset: 1,
                color: 'rgba(84, 112, 198, 0.1)'
              }
            ]
          }
        },
        data: lineChartData.value.counts
      }
    ]
  }

  lineChart.setOption(option)
}

onMounted(async () => {
  // 动态导入 ECharts
  echarts = await import('echarts')

  // 获取数据
  await loadOverviewData()
  await loadChartData()
  await loadStatsData()

  // 初始化图表
  nextTick(() => {
    initPieChart()
    initLineChart()
  })

  // 监听窗口大小变化，重新渲染图表
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  if (pieChart) {
    pieChart.dispose()
  }
  if (lineChart) {
    lineChart.dispose()
  }

  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.home-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .overview-cards {
    margin-bottom: 20px;

    .stat-card {
      height: 120px;

      .stat-content {
        display: flex;
        align-items: center;
        height: 100%;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          font-size: 24px;
          color: #fff;

          &.user-icon {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.paper-icon {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.question-icon {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.exam-icon {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 32px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 8px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .chart-container {
    margin-bottom: 20px;

    .chart-card {
      .chart-header {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }

      .chart-wrapper {
        .chart {
          width: 100%;
        }
      }
    }
  }

  .bottom-stats {
    .info-card {
      .info-header {
        display: flex;
        align-items: center;
        font-weight: bold;
        color: #303133;

        .el-icon {
          margin-right: 8px;
          color: #409eff;
        }
      }

      .info-content {
        .info-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .info-label {
            color: #606266;
            font-size: 14px;
          }

          .info-value {
            color: #303133;
            font-weight: bold;
            font-size: 16px;
          }
        }
      }
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .home-container {
    padding: 10px;

    .overview-cards {
      .stat-card {
        height: 100px;

        .stat-content {
          .stat-icon {
            width: 50px;
            height: 50px;
            font-size: 20px;
            margin-right: 12px;
          }

          .stat-info {
            .stat-value {
              font-size: 24px;
            }

            .stat-label {
              font-size: 12px;
            }
          }
        }
      }
    }

    .chart-container,
    .bottom-stats {
      .el-col {
        margin-bottom: 15px;
      }
    }
  }
}
</style>

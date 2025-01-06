<script lang="ts" setup>
import echarts from '@/utils/echarts';
import {inject, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';
import {useSettingStore} from '@/store/modules/settingStore';
import {ElMessage} from 'element-plus';
import {
  getCallTrend,
  getExceptionData,
  getInterfaceOverview,
  getLogOverview,
  getRealTimeLog, getRunningCountData
} from "@/api/home/home";

const settingStore = useSettingStore();
const {t} = useI18n();
// 添加统一的配置对象
const CONFIG = {
  table: {
    realTimeLog: {
      pageSize: 10,
      refreshInterval: 10000 // 自动刷新时间 10秒
    },
    exception: {
      pageSize: 5
    }
  }
};

// 基础配置
const spanValue = ref(12);
const loading = ref(false);
const fontSizeObj: any = inject('sizeObjInfo');
const timeRange = ref('day');

// 在文件顶部定义图表实例
const chartInstance = ref(null);

// 接口概况数据的响应式变量
const overviewData = ref({
  registerCount: 0,  // 注册数
  publishCount: 0,   // 发布数
  stopCount: 0,      // 停用数
  runningCount: 0    // 运行数
});
// 接口运行状态数的响应式变量
const runningCountData = ref({
  normalCount: 0,  // 运行正常数
  abnormalCount: 0,   // 运行异常数
});

// 趋势图数据的响应式变量
const trendData = ref({
  chartData: {
    xAxis: [],    // x轴数据
    datas: {}     // 系列数据，改为对象格式
  }
});
// 日志概况数据
const logOverviewData = ref({
  allTotal: 0,      // 总调用量
  todayAllTotal: 0,      // 今日调用量
  allErrorTotal: 0,  // 总异常量
  todayAllErrorTotal: 0,  // 今日异常量
});

// 创建分页配置工厂函数
const createPagination = (pageSize: number) => ref({
  currentPage: 1,
  pageSize,
  total: 0,
  loading: false
});

// 使用工厂函数创建分页配
const realTimeLogPagination = createPagination(CONFIG.table.realTimeLog.pageSize);
const exceptionPagination = createPagination(CONFIG.table.exception.pageSize);

const realTimeLogPaginationConfig = ref({
  customStyle: 'default',
  hideOnSinglePage: false, //只有一页时是否隐藏
  background: false,//是否显示背景色
  layout: "prev, pager, next,sizes",//布局
  currentPage: 1,//当前页数，支持 v-model 双向绑定
  pageSize: 10,//每页显示条目个数，支持 v-model 双向绑定
  total: 0,//总条目数
  pageSizeOpts: [5,10,20,50,1000],//每页显示个数选择器的选项设置
})
const exceptionPaginationConfig = ref({
  customStyle: 'default',
  hideOnSinglePage: false, //只有一页时是否隐藏
  background: false,//是否显示背景色
  layout: "prev, pager, next,sizes",//布局
  currentPage: 1,//当前页数，支持 v-model 双向绑定
  pageSize: 5,//每页显示条目个数，支持 v-model 双向绑定
  total: 0,//总条目数
  pageSizeOpts: [5,10,20,50,1000],//每页显示个数选择器的选项设置
})
// 统一的API调用处理函数
const fetchTableData = async (
    apiFunc: Function,
    dataRef: any,
    pagination: any,
    errorMessage: string
) => {
  pagination.value.loading = true;
  try {
    const reqParam = {
      page: pagination.value.currentPage,
      limit: pagination.value.pageSize
    };
    const res = await apiFunc(reqParam);
    if (res.code === '0') {
      dataRef.value = []
      dataRef.value = res.data || [];
      pagination.value.total = res.count || 0;
    } else {
      dataRef.value = [];
      pagination.value.total = 0;
      // ElMessage.error(errorMessage);
    }
  } finally {
    pagination.value.loading = false;
  }
};

// 使用统一的API调用函数
const fetchRealTimeLog = () => fetchTableData(
    getRealTimeLog,
    realTimeLogData,
    realTimeLogPaginationConfig,
    '获取实时日志播报数据失败'
);

const fetchExceptionData = () => fetchTableData(
    getExceptionData,
    exceptionTableData,
    exceptionPaginationConfig,
    '获取接口异常情况数据失败'
);

// 获取接口概况数据的函数
async function fetchOverviewData() {
  try {
    const res = await getInterfaceOverview();
    if (res.code === '0') {
      overviewData.value.registerCount = res.data.registerCount || 0;
      overviewData.value.publishCount = res.data.publishCount || 0;
      overviewData.value.stopCount = res.data.stopCount || 0;
      overviewData.value.runningCount = res.data.runningCount || 0;
    } else {
      // ElMessage.error('获取接口概况数据失败');
    }

  } catch (error) {
    // console.error('获取接口概况数据失败:', error);
  }
};

async function fetchRunningCountData() {
  try {
    const res = await getRunningCountData();
    if (res.code === '0') {
      runningCountData.value.normalCount = res.data.normalCount || 0
      runningCountData.value.abnormalCount = res.data.abnormalCount || 0
    } else {
      // ElMessage.error('获取接口运行状态数据失败');
    }
  } catch (error) {
    // console.error('获取接口运行状态数据失败:', error);
  }
};

// 获取趋势图数据的函数
const fetchTrendData = async () => {
  try {
    const res = await getCallTrend({type: timeRange.value});
    if (res.code === '0' && res.data) {
      trendData.value.chartData = {
        xAxis: res.data.xList,
        datas: res.data.datas
      };
      updateTrendChart();
    } else {
      // ElMessage.error('获取接口趋势图数据失败');
    }
  } catch (error) {
    // console.error('获取接口趋势图数据失败:', error);
  }
};

// 获取日志概况数据的函数
async function fetchLogOverviewData() {
  try {
    const res = await getLogOverview();
    if (res.code === '0') {
      logOverviewData.value.allTotal = res.data.allTotal || 0;
      logOverviewData.value.allErrorTotal = res.data.allErrorTotal || 0;
      logOverviewData.value.todayAllTotal = res.data.todayAllTotal || 0;
      logOverviewData.value.todayAllErrorTotal = res.data.todayAllErrorTotal || 0;
    } else {
      // ElMessage.error('获取日志概况数据失败');
    }

  } catch (error) {
    // console.error('获取日志概况数据失败:', error);
  }
};

// 修改监听时间范围变化的代码
watch(() => timeRange.value, () => {
  fetchTrendData();
}, {immediate: true});

// 修改更新图表的函数
const updateTrendChart = () => {
  if (!chartInstance.value) {
    return;
  }

  const option = {
    tooltip: {
      trigger: 'item',
      axisPointer: {
        type: 'cross'
      },
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    legend: {
      top: '0%',
      left: 'center',
      selectedMode: true
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: trendData.value.chartData.xAxis || [],
      axisLabel: {
        interval: 0,
        rotate: 30,
        align: 'center'
      }
    },
    yAxis: {
      type: 'value',
      scale: true
    },
    series: Object.entries(trendData.value.chartData.datas || {}).map(([name, data]) => ({
      name,
      type: 'line',
      data: data,
      smooth: true,
    }))
  };

  chartInstance.value.setOption(option, {
    replaceMerge: ['series']
  });
};

// 修改图表初始化函数
function initChart() {
  if (chartInstance.value) {
    chartInstance.value.dispose();
    chartInstance.value = null;
  }
  const chartDom = document.getElementById('interface-call-situation');
  if (chartDom) {
    chartInstance.value = echarts.init(chartDom);
  }
}

// 添加窗口大小变化的处理
window.addEventListener('resize', () => {
  if (chartInstance.value) {
    chartInstance.value.resize({
      animation: {
        duration: 300
      }
    });
  }
});

// 修改初始化数据函数
async function initData() {
  loading.value = true;
  try {
    await Promise.all([
      fetchOverviewData(),
      fetchRunningCountData(),
      fetchTrendData(),
      fetchLogOverviewData(),
      fetchExceptionData(),
      fetchRealTimeLog()
    ]);
  } finally {
    loading.value = false;
  }
};

// 添加鼠标移入移出处理函数
const handleTableMouseEnter = () => {
  stopAutoRefresh();
};

const handleTableMouseLeave = () => {
  startAutoRefresh();
};

// 修改分页处理函数，移除重复的自动刷新控制
const handleRealTimeLogChange = (val: number) => {
  realTimeLogPaginationConfig.value.currentPage = val;
  fetchRealTimeLog();
};

const handleExceptionChange = (val: number) => {
  exceptionPaginationConfig.value.currentPage = val;
  fetchExceptionData();
};

//  应用列表 分页 触发
function handleRealTimeLogChangePageSize(pageSize) {
  realTimeLogPaginationConfig.value.pageSize = pageSize
    fetchRealTimeLog()
}
//  应用列表 分页 触发
function handleExceptionChangePageSize(pageSize) {
  exceptionPaginationConfig.value.pageSize = pageSize
  fetchExceptionData()
}
// 添加自动翻页相关的变量和函数
const autoRefreshInterval = ref<number | null>(null);

// 自动翻页函数
const autoRefreshRealTimeLog = () => {
  let currentPage = realTimeLogPagination.value.currentPage;
  const totalPages = Math.ceil(realTimeLogPagination.value.total / realTimeLogPagination.value.pageSize);

  // 如果当前是最后一页，回到第一页
  if (currentPage >= totalPages || currentPage >= 10) {
    currentPage = 1;
  } else {
    currentPage++;
  }

  realTimeLogPagination.value.currentPage = currentPage;
  fetchRealTimeLog();
};

// 开始自动翻页
const startAutoRefresh = () => {
  stopAutoRefresh(); // 先清除可能存在的定时器
  autoRefreshInterval.value = setInterval(() => {
    autoRefreshRealTimeLog(); // 修改这里，调用自动翻页函数而不是直接取数据
  }, CONFIG.table.realTimeLog.refreshInterval);
};

// 停止自动翻页
const stopAutoRefresh = () => {
  if (autoRefreshInterval.value) {
    clearInterval(autoRefreshInterval.value);
    autoRefreshInterval.value = null;
  }
};

// 修改生命周期钩子
onMounted(() => {
  initChart();
  initData();
  startAutoRefresh(); // 添加这行，启动自动翻页
  window.addEventListener('resize', () => {
    if (chartInstance.value) {
      chartInstance.value.resize();
    }
  });
});

onBeforeUnmount(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose();
    chartInstance.value = null;
  }
  stopAutoRefresh(); // 停止自动翻页
});

// 在 script 部分添加缺失的数据定义
const realTimeLogData = ref([]);
const exceptionTableData = ref([]);
</script>

<template>
  <div style="height: 100%; overflow-y: auto; overflow-x: hidden; scrollbar-width: none">
      <el-row :gutter="20" style="height: calc(100% - 7px);">
        <!-- 左侧内容 -->
        <el-col :span="11">
          <!-- 接口概况 -->
          <el-card class="box-card">
            <div class="card-header">
              <span>{{ $t('接口概况') }}</span>
              <div style="height: 32px;"></div>
            </div>
            <div class="overview-content">
              <!-- 数值展示 -->
              <div class="stat-items">
                <div class="stat-item">
                  <h2>{{ overviewData.registerCount }}</h2>
                  <span>{{ $t('注册数') }}</span>
                </div>
                <div class="stat-item">
                  <h2>{{ overviewData.publishCount }}</h2>
                  <span>{{ $t('发布数') }}</span>
                </div>
                <div class="stat-item">
                  <h2>{{ overviewData.stopCount }}</h2>
                  <span>{{ $t('停用数') }}</span>
                </div>
                <div class="stat-item">
                  <h2>{{ runningCountData.normalCount }}</h2>
                  <span>{{ $t('运行正常数') }}</span>
                </div>
                <div class="stat-item">
                  <h2>{{ runningCountData.abnormalCount }}</h2>
                  <span>{{ $t('运行异常数') }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 实时日志播报 -->
          <el-card class="box-card">
            <div class="card-header">
              <span>{{ $t('实时日志播报') }}</span>
              <!-- <el-pagination
                  v-model:current-page="realTimeLogPagination.currentPage"
                  :page-size="realTimeLogPagination.pageSize"
                  :total="realTimeLogPagination.total"
                  layout="prev, pager, next"
                  :small="true"
                  @current-change="handleRealTimeLogChange"
              /> -->
              <y9Pagination :config="realTimeLogPaginationConfig" @current-change="handleRealTimeLogChange" @size-change="handleRealTimeLogChangePageSize"></y9Pagination>
            </div>
            <el-table
                :data="realTimeLogData"
                v-loading="realTimeLogPagination.loading"
                class="custom-table real-time-log"
                @mouseenter="handleTableMouseEnter"
                @mouseleave="handleTableMouseLeave"
            >
              <el-table-column label="序号" width="60" align="center">
                <template #default="scope">
                  {{ (realTimeLogPagination.currentPage - 1) * realTimeLogPagination.pageSize + scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column prop="apiName" label="接口名称" min-width="140" align="center" show-overflow-tooltip/>
              <el-table-column prop="responseMsg" label="调用状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.responseMsg === '成功' ? 'success' : 'danger'">
                    {{ row.responseMsg }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="requestStartTime" label="调用时间" min-width="180" align="center"
                               show-overflow-tooltip/>
              <el-table-column prop="applySystemName" label="调用系统" min-width="120" align="center"
                               show-overflow-tooltip/>
            </el-table>
          </el-card>
        </el-col>

        <!-- 右侧内容 -->
        <el-col :span="13" style="margin-right: -20px;padding-right: 13px;">
          <!-- 接口调用趋势图 -->
          <el-card class="box-card">
            <div class="card-header">
              <span>{{ $t('接口调用趋势图') }}</span>
              <el-select v-model="timeRange" style="width: 120px">
                <el-option label="当天" value="day"/>
                <el-option label="近一周" value="week"/>
                <el-option label="近一月" value="month"/>
                <el-option label="近一年" value="year"/>
              </el-select>
            </div>
            <div class="trend-content">
              <!-- 上部分：统计数据 -->
              <div class="trend-statistics">
                <div class="stat-item">
                  <h3>{{ logOverviewData.allTotal }}</h3>
                  <span>{{ $t('总调用量') }}</span>
                </div>
                <div class="stat-item">
                  <h3>{{ logOverviewData.todayAllTotal }}</h3>
                  <span>{{ $t('今日调用量') }}</span>
                </div>
                <div class="stat-item">
                  <h3>{{ logOverviewData.allErrorTotal }}</h3>
                  <span>{{ $t('总异常量') }}</span>
                </div>
                <div class="stat-item">
                  <h3>{{ logOverviewData.todayAllErrorTotal }}</h3>
                  <span>{{ $t('今日异常量') }}</span>
                </div>
              </div>
              <!-- 下部分：图表 -->
              <div id="interface-call-situation" class="chart-container"></div>
            </div>
          </el-card>

          <!-- 接口异常情况 -->
          <el-card class="box-card">
            <div class="card-header">
              <span>{{ $t('接口异常情况') }}</span>
              <!-- <el-pagination
                  v-model:current-page="exceptionPagination.currentPage"
                  :page-size="exceptionPagination.pageSize"
                  :total="exceptionPagination.total"
                  layout="prev, pager, next"
                  :small="true"
                  @current-change="handleExceptionChange"
              /> -->
              <y9Pagination :config="exceptionPaginationConfig" @current-change="handleExceptionChange" @size-change="handleExceptionChangePageSize"></y9Pagination>
            </div>
            <el-table
                :data="exceptionTableData"
                v-loading="exceptionPagination.loading"
                class="custom-table exception-log"
            >
              <el-table-column label="序号" width="60" align="center">
                <template #default="scope">
                  {{ (exceptionPagination.currentPage - 1) * exceptionPagination.pageSize + scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column prop="apiName" label="接口名称" min-width="140" align="center" show-overflow-tooltip/>
              <el-table-column prop="applySystemName" label="调用系统" min-width="120" align="center"
                               show-overflow-tooltip/>
              <el-table-column prop="requestStartTime" label="调用时间" min-width="180" align="center"
                               show-overflow-tooltip/>
              <el-table-column prop="errorMessage" label="异常描述" min-width="160" align="center"
                               show-overflow-tooltip/>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
  </div>
</template>

<style lang="scss" scoped>
@font-face {
  font-family: yjsz;
  src: url('../../assets/font/yjsz.TTF');
}

.box-card {
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter);

  .el-card__header {
    padding: 12px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background-color: var(--el-bg-color);
  }

  .el-card__body {
    padding: 0px 15px 15px 15px!important;
  }
}

// 左侧卡片高度整
.el-col:first-child {
  height: 100%;

  .box-card:first-child {
    height: 25%;
    margin-bottom: 20px;
  }

  .box-card:last-child {
    height: calc(75% - 20px); // 去上面卡片高度和间距
  }
}

// 右侧卡片高度调整
.el-col:last-child {
  height: 100%;

  .box-card:first-child {
      height: 54%;
      margin-bottom: 20px;
    }

  .box-card:last-child {
      height: calc(46% - 20px); // 去上面卡片高度和间距
    }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px;
  border-bottom: 1px solid #ebeef5;
  white-space: nowrap;

  span {
    font-size: 15px;
    font-weight: 500;
  }

  :deep(.el-pagination) {
    margin: 0;
    padding: 0;

    .el-pagination__jump {
      display: none;
    }

    .btn-prev,
    .btn-next {
      min-width: 22px;
      height: 22px;
    }

    .el-pager {
      li {
        min-width: 22px;
        height: 22px;
        line-height: 22px;
      }
    }
  }
}

.statistics-content {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;

  .stat-item {
    text-align: center;

    h2 {
      font-size: 24px;
      color: var(--el-color-primary);
      margin-bottom: 8px;
    }

    span {
      font-size: 14px;
      color: #666;
    }
  }
}

.header-controls {
  display: flex;
  gap: 8px;
  align-items: center;

  .el-button {
    padding: 8px;
  }
}

:deep(.el-card__body) {
  padding: 2px 15px 15px 15px;
  height: calc(100% - 30px);
}



// 优化卡片内容区域
.el-col:last-child, .el-col:first-child {
  .box-card {
    .custom-table {
      margin: 15px 0px;
      height: calc(100% - 54px);
    }
  }
}



   // 修改表格基础样式
:deep(.el-table) {
  // 表头样式
  .el-table__header-wrapper {
    th {
      background-color: var(--el-fill-color-light) !important;
      color: var(--el-text-color-primary);
      font-weight: 500;
      height: 40px;
      padding: 8px 0;
    }
  }

  // 表主体样式
  .el-table__body-wrapper {
    .el-scrollbar__view{
      //height: 100%;

      .el-table__body{
        //height: 100%;

        tr {
          td {
            padding: 8px 0;
          }

          &:hover > td {
            background-color: var(--el-fill-color-light) !important;
          }
        }

      }
    }

  }
}

// 修改表格边框和分割线
:deep(.el-table--border) {
  border: 1px solid var(--el-border-color-lighter);

  &::after,
  &::before {
    background-color: var(--el-border-color-lighter);
  }

  .el-table__inner-wrapper::after {
    background-color: var(--el-border-color-lighter);
  }
}

// 添加接口概况相关样式
.overview-content {
  height: calc(100% - 21px);
  display: flex;
  padding: 0px 20px; // 减小上下内边距

  .stat-items {
    width: 100%;
    display: flex;
    justify-content: space-around;
    align-items: center;

    .stat-item {
      text-align: center;
      min-width: 80px;

      h2 {
        font-size: 45px; // 稍微减小字号
        color: var(--el-color-primary);
        margin-bottom: 6px; // 减小底部间距
        font-family: yjsz;
        font-weight: 400;
        margin-top: 0px;
      }

      span {
        font-size: 14px;
        color: #666;
      }
    }
  }
}

// 修改趋势图容区域的样式
.trend-content {
  height: calc(100% - 48.8px);
  display: flex;
  flex-direction: column;
  padding: 0px 15px 15px 15px;


  .trend-statistics {
    height: 60px;
    display: flex;
    justify-content: space-around;
    padding: 12px 20px; // 减小上下padding
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 5px; // 减小下边距

    .stat-item {
      text-align: center;
      padding: 0 15px;

      h3 {
        font-size: 30px;
        color: var(--el-color-primary);
        margin: 0 0 8px 0;
        font-family: yjsz;
        font-weight: 600;
      }

      span {
        font-size: 13px;
        color: #666;
        font-weight: 500;
      }
    }
  }

  #interface-call-situation {
    flex: 1;
    height: calc(100% - 190px) ;
    margin-top: 10px; // 小顶部间距
  }
}
:deep(.el-card.is-always-shadow){
  box-shadow: 2px 2px 2px 1px rgba(0,0,0,0.06);
}
</style>

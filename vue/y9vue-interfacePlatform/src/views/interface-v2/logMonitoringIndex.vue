<template>
  <div class="log-monitoring" style="height: 100%; overflow-y: auto; overflow-x: hidden; scrollbar-width: none">
    <el-form :model="searchForm" ref="searchFormRef" class="search-form">
      <!-- 第一行 -->
      <div class="form-row">
        <div class="form-item">
          <el-form-item label="请求方法">
            <el-select v-model="searchForm.interfaceType" placeholder="请选择请求方法" clearable>
              <el-option v-for="item in searchOptions.interfaceType" :key="item" :label="item" :value="item"/>
            </el-select>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="部门/系统">
            <el-cascader
              v-model="searchForm.deptSystem"
              :options="deptSystemOptions"
              :props="{
                expandTrigger: 'hover',
                value: 'value',
                label: 'label',
                children: 'children',
                multiple: true,
                checkStrictly: false
              }"
              placeholder="请选择部门/系统"
              clearable
              collapse-tags
              collapse-tags-tooltip
              :max-collapse-tags="1"
              @change="handleDeptSystemChange"
              class="custom-cascader"
            />
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="限流结果">
            <el-select v-model="searchForm.limitResult" placeholder="请选择限流结果" clearable>
              <el-option v-for="item in searchOptions.limitResult" :key="item" :label="item" :value="item"/>
            </el-select>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="是否鉴权">
            <el-select v-model="searchForm.authentic" placeholder="请选择是否鉴权" clearable>
              <el-option v-for="item in searchOptions.authentic" :key="item"
                         :label="item === 'true' ? '是' : '否'" :value="item"/>
            </el-select>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="是否限流">
            <el-select v-model="searchForm.limit" placeholder="请选择是否限流" clearable>
              <el-option v-for="item in searchOptions.limit" :key="item"
                         :label="item === 'true' ? '是' : '否'" :value="item"/>
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- 第二行 -->
      <div class="form-row">
        <div class="form-item">
          <el-form-item label="响应状态">
            <el-select v-model="searchForm.responseMsg" placeholder="请选择响应状态" clearable>
              <el-option v-for="item in searchOptions.responseMsg" :key="item" :label="item" :value="item"/>
            </el-select>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="接口调用者">
            <el-input v-model="searchForm.requestUserName" placeholder="请输入调用者名称" clearable/>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="接口申请人">
            <el-input v-model="searchForm.userName" placeholder="请输入申请人名称" clearable/>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="服务器IP">
            <el-input v-model="searchForm.serverIP" placeholder="请输入服务器IP" clearable/>
          </el-form-item>
        </div>

        <div class="form-item">
          <el-form-item label="调者IP">
            <el-input v-model="searchForm.requestIP" placeholder="请输入调用者IP" clearable/>
          </el-form-item>
        </div>
      </div>

      <!-- 第三行 -->
      <div class="form-row">
        <div class="form-item">
          <el-form-item label="接口名称">
            <el-input v-model="searchForm.apiName" placeholder="请输入接口名称" clearable/>
          </el-form-item>
        </div>

        <div class="form-item time-range-item">
          <el-form-item label="调用时间">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="[
                new Date(2000, 1, 1, 0, 0, 0),
                new Date(2000, 1, 1, 23, 59, 59),
              ]"
              :disabled-date="disabledDate"
              clearable
            />
          </el-form-item>
        </div>

        <div class="form-item"></div>

        <div class="form-item button-col">
          <div class="button-group">
            <el-button type="primary" @click="handleSearch">
              <i class="ri-search-line"></i>
              <span>{{ $t('查询') }}</span>
            </el-button>
            <el-button @click="handleReset">
              <i class="ri-refresh-line"></i>
              <span>{{ $t('重置') }}</span>
            </el-button>
          </div>
        </div>
      </div>
    </el-form>

    <!-- 添加表格部分 -->
    <y9Table
      :config="y9TableConfig"
      :filterConfig="filterOperaConfig"
      @on-curr-page-change="handlerCurrPage"
      ref="filterRef"
      @on-page-size-change="handlerPageSize"
    >
    </y9Table>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import {getLogMonitoring, getOptions} from "@/api/logMonitoring/logMonitoring";
import '@/assets/css/tablestatusfontcolor.css';

const { t } = useI18n();

// 搜索表单数据
const searchForm = ref({
  interfaceType: '',
  deptSystem: [],
  deptSystemMap: {},
  limitResult: '',
  authentic: '',
  limit: '',
  responseMsg: '',
  apiName: '',
  userName: '',
  serverIP: '',
  requestIP: '',
  requestUserName: '',
  timeRange: [],
});

// 搜索选项数据
const searchOptions = ref({
  interfaceType: [],
  limitResult: [],
  authentic: [],
  limit: [],
  responseMsg: []
});

// 处理单位和系统的级联选项
const deptSystemOptions = ref([]);

// 转换部门数据为级联选择器格式
const transformDeptData = (deptData) => {
  return Object.entries(deptData).map(([dept, systems]) => ({
    value: dept,
    label: dept,
    children: Array.isArray(systems) ? systems.map(system => ({
      value: system,
      label: system
    })) : []
  }));
};

// 添加一个过滤空值的工具函数
const filterNullValue = (value: any) => {
  if (value === null || value === 'null') {
    return '';
  }
  return value;
};

// 表格列配置
const tableItem = [
  {
    type: 'index',
    title: computed(() => t('序号')),
    width: 70,
    fixed: 'left'
  },
  {
    title: computed(() => t('请求方法')),
    key: 'interfaceType',
    sortable: true,
    minWidth: 120,
    render: (row) => filterNullValue(row.interfaceType)
  },
  {
    title: computed(() => t('接口名称')),
    key: 'apiName',
    sortable: true,
    minWidth: 160,
    render: (row) => filterNullValue(row.apiName)
  },
  {
    title: computed(() => t('接口申请人')),
    key: 'userName',
    sortable: true,
    minWidth: 120,
    render: (row) => filterNullValue(row.userName)
  },
  {
    title: computed(() => t('部门名称')),
    key: 'deptName',
    sortable: true,
    minWidth: 150,
    render: (row) => filterNullValue(row.deptName)
  },
  {
    title: computed(() => t('调用系统')),
    key: 'applySystemName',
    sortable: true,
    minWidth: 150,
    render: (row) => filterNullValue(row.applySystemName)
  },
  {
    title: computed(() => t('调用时间')),
    key: 'requestStartTime',
    sortable: true,
    minWidth: 180,
    render: (row) => filterNullValue(row.requestStartTime)
  },
  {
    title: computed(() => t('响应状态')),
    key: 'responseMsg',
    sortable: true,
    width: 120,
    render:(row)=>{
            let statusColor = "successText"
            if(row.responseMsg=="成功"){
                statusColor = "successText"
            }else if(row.responseMsg=="失败"){
                statusColor = "stopText"
            }
            return h('div', { class: statusColor}, t(filterNullValue(row.responseMsg)));
        }
  },
  {
    title: computed(() => t('服务器IP')),
    key: 'serverIP',
    sortable: true,
    minWidth: 140,
    render: (row) => filterNullValue(row.serverIP)
  },
  {
    title: computed(() => t('调用者IP')),
    key: 'requestIP',
    sortable: true,
    minWidth: 140,
    render: (row) => filterNullValue(row.requestIP)
  },
  {
    title: computed(() => t('是否鉴权')),
    key: 'authentic',
    sortable: true,
    width: 120,
    render: (row) => filterNullValue(row.authentic) ? '是' : '否'
  },
  {
    title: computed(() => t('是否限流')),
    key: 'limit',
    sortable: true,
    width: 120,
    render: (row) => filterNullValue(row.limit) ? '是' : '否'
  },
  {
    title: computed(() => t('接口调用者')),
    key: 'requestUserName',
    sortable: true,
    width: 120,
    render: (row) => filterNullValue(row.requestUserName)
  },
  {
    title: computed(() => t('限流结果')),
    key: 'limitResult',
    sortable: true,
    width: 120,
    render: (row) => filterNullValue(row.limitResult)
  },
  {
    title: computed(() => t('限流等待时长(ms)')),
    key: 'limitTime',
    sortable: true,
    width: 165,
    render: (row) => filterNullValue(row.limitTime)
  },
  {
    title: computed(() => t('限流规则')),
    key: 'rule',
    sortable: true,
    minWidth: 150,
    render: (row) => filterNullValue(row.rule)==undefined?"": filterNullValue(row.rule) 
  },
  {
    title: computed(() => t('请求参数')),
    key: 'requestParam',
    minWidth: 150,
    render: (row) => filterNullValue(row.requestParam)
  }
];

// 表格配置
const y9TableConfig = ref({
  headerBackground: true,
  pageConfig: {
    background: false,
    currentPage: 1,
    pageSize: 15,
    total: 0,
    pageSizeOpts: [5, 10, 15, 20, 30, 40, 1000]
  },
  columns: tableItem,
  tableData: [],
});

// 分页处理
const handlerPageSize = (pageSize: number) => {
  y9TableConfig.value.tableData = [];
  y9TableConfig.value.pageConfig.pageSize = pageSize;
  getDataList();
};

const handlerCurrPage = (currentPage: number) => {
  y9TableConfig.value.tableData = [];
  y9TableConfig.value.pageConfig.currentPage = currentPage;
  getDataList();
};

// 获取数据
const getDataList = async () => {
  y9TableConfig.value.loading = true;
  try {
    // 构造API所需的参数格式
    const deptSystemParam = {};
    if (searchForm.value.deptSystemMap) {
      Object.entries(searchForm.value.deptSystemMap).forEach(([parent, children]) => {
        deptSystemParam[parent] = children;
      });
    }

    // 处理时间范围
    let requestStartTime = '';
    if (searchForm.value.timeRange && searchForm.value.timeRange.length === 2) {
      const [startTime, endTime] = searchForm.value.timeRange;
      requestStartTime = `${startTime}~${endTime}`;
    }

    const params = {
      interfaceType: searchForm.value.interfaceType,
      deptSystem: deptSystemParam,
      limitResult: searchForm.value.limitResult,
      authentic: searchForm.value.authentic,
      limit: searchForm.value.limit,
      responseMsg: searchForm.value.responseMsg,
      apiName: searchForm.value.apiName,
      userName: searchForm.value.userName,
      serverIP: searchForm.value.serverIP,
      requestIP: searchForm.value.requestIP,
      requestUserName: searchForm.value.requestUserName,
      requestStartTime, // 使用拼接的时间范围
      page: y9TableConfig.value.pageConfig.currentPage,
      pageSize: y9TableConfig.value.pageConfig.pageSize
    };

    const res = await getLogMonitoring(params);
    if (res.code === '0') {
      y9TableConfig.value.tableData = res.data || [];
      y9TableConfig.value.pageConfig.total = res.count || 0;
    } else {
      ElMessage.error('获取日志监控数据失败');
    }
  } catch (error) {
    console.error('获取日志监控数据失败:', error);
    ElMessage.error('获取日志监控数据失败');
  } finally {
    y9TableConfig.value.loading = false;
  }
};

// 初始化表格数据
const initTableData = () => {
  y9TableConfig.value.pageConfig.currentPage = 1;
  y9TableConfig.value.pageConfig.pageSize = 15;
  y9TableConfig.value.tableData = [];
  getDataList();

};

// 初始化搜索选项
const initSearchOptions = (data) => {
  searchOptions.value = {
    interfaceType: data.interfaceType || [],
    limitResult: data.limitResult || [],
    authentic: data.authentic || [],
    limit: data.limit || [],
    responseMsg: data.responseMsg || []
  };
  deptSystemOptions.value = transformDeptData(data.deptName || {});
};

// 获取检索条件数据
const fetchOptions = async () => {
  try {
    const res = await getOptions();
    if (res.code === '0') {
      initSearchOptions(res.data);
    } else {
      ElMessage.error('获取检索条件数据失败');
    }
  } catch (error) {
    console.error('获取检索条件数据失败:', error);
  }
};

// 组件挂载时初始化数据
onMounted(async () => {
  await fetchOptions(); // 先获取检索条件
  initTableData();     // 再初始化表格数据
});

// 修改查询按钮的处理函数
const handleSearch = () => {
  y9TableConfig.value.pageConfig.currentPage = 1; // 重置到第一页
  getDataList(); // 获取数据
};

// 修改重置按钮的处理函数
const handleReset = () => {
  searchForm.value = {
    interfaceType: '',
    deptSystem: [],
    deptSystemMap: {}, // 添加新的映射字段
    limitResult: '',
    authentic: '',
    limit: '',
    responseMsg: '',
    apiName: '',
    userName: '',
    serverIP: '',
    requestIP: '',
    requestUserName: '',
    timeRange: [], // 重置时间范围
  };
  y9TableConfig.value.pageConfig.currentPage = 1;
  getDataList();
};

// 简化 filterOperaConfig
const filterOperaConfig = ref({
  showBorder: true,
  borderRadio: '4px'
});

// 简化 handleDeptSystemChange 函数
const handleDeptSystemChange = (values) => {
  if (!values || values.length === 0) {
    searchForm.value.deptSystem = [];
    searchForm.value.deptSystemMap = {};
    return;
  }

  const deptSystemMap = {};
  values.forEach(path => {
    if (path.length > 0) {
      const parent = path[0];
      const child = path.length > 1 ? path[1] : null;

      if (!deptSystemMap[parent]) {
        deptSystemMap[parent] = new Set();
      }

      if (child) {
        deptSystemMap[parent].add(child);
      }
    }
  });

  Object.keys(deptSystemMap).forEach(key => {
    deptSystemMap[key] = Array.from(deptSystemMap[key]);
  });

  searchForm.value.deptSystemMap = deptSystemMap;
  searchForm.value.deptSystem = values;
};

// 添加禁用日期函数
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now();
};
</script>

<style lang="scss" scoped>
.log-monitoring {
  height: 100%;

  .search-form {

    .form-row {
      display: flex;
      //gap: 20px;
      margin-bottom: 16px;

      .form-item {
        flex: 1;
        width: 0;

        &.time-range-item {
          flex: 2;
        }

        :deep(.el-form-item) {
          margin: 0;
          width: 100%;

          .el-form-item__label {
            width: 90px !important;
            justify-content: flex-end;
            padding-right: 8px;
            font-size: 14px;
          }

          .el-form-item__content {
            flex: 1;
            min-width: 0;

            .el-select,
            .el-cascader,
            .el-input {
              width: 100%;
            }
          }
        }
      }
    }

    .button-group {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      height: 32px;
      align-items: center;
    }
  }

  :deep(.y9-table-div) {
    height: calc(100% - 218px);

    .el-table {
      height: 100%;
    }
  }

  :deep(.y9-table) {
    margin-top: 20px;
    background-color: var(--el-bg-color);
    border-radius: 4px;
  }

  :deep(.custom-cascader) {
    .el-input__wrapper {
      height: 32px !important; // 固定输入框高度
      line-height: 32px !important;
    }

    .el-cascader__tags {
      flex-wrap: nowrap !important; // 防止标签换行
      overflow: hidden !important;
      padding: 0 30px 0 4px !important; // 右侧留出空间给下拉箭头
      height: 24px !important; // 控制标签区域高度

      .el-tag {
        height: 20px !important; // 降低标签高度
        line-height: 20px !important;
        padding: 0 4px !important;
        max-width: 140px !important; // 限制单个标签最大宽度
        
        .el-tag__content {
          display: inline-block !important;
          overflow: hidden !important;
          text-overflow: ellipsis !important;
          white-space: nowrap !important;
        }
      }
    }
  }

  .search-form {
    .form-row {
      .form-item {
        :deep(.el-date-editor.el-input__wrapper) {
          width: 100% !important;
        }
        
        :deep(.el-date-editor) {
          --el-date-editor-width: 100% !important;
        }
      }
    }
  }
}
</style>
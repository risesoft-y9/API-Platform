<template>
    <!-- 流程列表 -->
    <y9Table :config="y9TableConfig" :filterConfig="filterOperaConfig" @on-curr-page-change="handlerCurrPage"
        ref="filterRef" @on-page-size-change="handlerPageSize">
        <template v-slot:slotSearch>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="search">
                <i class="ri-search-line"></i>
                <span>{{ $t('查询') }}</span>
            </el-button>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="el-button el-button--default global-btn-third" @click="reset">
                <i class="ri-refresh-line"></i>
                <span>{{ $t('重置') }}</span>
            </el-button>
        </template>
    </y9Table>
    <!-- 增加页面 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
            </y9Form>
        </template>
    </y9Dialog>

    <y9Dialog v-model:config="openChartConfig">
        <template v-slot>
            <el-card class="box-card">
            <div class="card-header">
              <span>{{ $t('服务使用概况') }}</span>
              <div style="height: 32px;"></div>
            </div>
            <div class="overview-content">
              <!-- 数值展示 -->
              <div class="stat-items">
                <div class="stat-item">
                  <h2>{{ serviceUsedData.registerCount }}</h2>
                  <span>{{ $t('注册接口数') }}</span>
                </div>
                <div class="stat-item">
                  <h2>{{ serviceUsedData.aveCount }}</h2>
                  <span>{{ $t('平均每天请求次数') }}</span>
                </div>
                <div class="stat-item">
                  <h2>{{ serviceUsedData.todayCount }}</h2>
                  <span>{{ $t('今日调用次数') }}</span>
                </div>
              </div>
            </div>
          </el-card>
          <el-row class="bottomArea">
            <el-col :span="4"></el-col>
            <el-col :span="8">
                <el-row class="labelTitle"><span>系统CPU使用率</span></el-row>
                <el-row>
                    <div ref="cpuChartContainerRef" style="width: 100%; height: 200px;"></div>
                </el-row>
            </el-col>
            <el-col :span="8">
                <el-row class="labelTitle"><span>内存使用率</span></el-row>
                <el-row>
                    <div ref="memoryChartContainerRef" style="width: 100%; height: 200px;"></div>
                </el-row>
            </el-col>
            <el-col :span="4"></el-col>
          </el-row>
        </template>
    </y9Dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, nextTick } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getPage, saveInfo, delInfoById } from '@/api/execute/execute'
import {getRegisterNum} from '@/api/interface/interface'
import { getCpuUsed, getMaxMemory, getUsedMemory,getHttpServerReqs } from '@/api/serviceResourceIndicators/serviceResourceIndicators'
import {getInvokeNumToday} from "@/api/home/home";
import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus';
import '@/assets/css/tablestatusfontcolor.css';
import * as echarts from 'echarts';
import 'echarts-liquidfill';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const selectedDate = ref();
const query: any = ref({});
const filterRef = ref();
const interfaceList = ref([]);
const cpuChartContainerRef = ref();
const memoryChartContainerRef = ref();
const serviceUsedData = ref({
    registerCount:0,
    todayCount:0,
    aveCount:0
})

const limitInfo = ref(false)
const sameId = ref()
const qbaseUrl = ref()

//表格配置
let y9TableConfig = ref({
    headerBackground: true,
    pageConfig: false,
    columns: [
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
            fixed: 'left'
        },
        {
            title: computed(() => t('应用名称')),
            key: 'name'
        },
        {
            title: computed(() => t('实例ID')),
            key: 'instanceId'
        },
        {
            title: computed(() => t('域名')),
            key: 'hostName'
        },
        {
            title: computed(() => t('ip')),
            key: 'ip'
        },
        {
            title: computed(() => t('端口')),
            key: 'port'
        },
        {
            title: computed(() => t('状态')),
            key: 'status',
            render: (row) => {
                let statusText = "-"
                let textClass = ""
                if(row.status.toUpperCase() == "UP"){
                    statusText="健康"
                    textClass = "successText"
                }
                if(row.status.toUpperCase() == "DOWN"){
                    statusText="宕机"
                    textClass = "stopText"
                }
                if(row.status.toUpperCase() == "STARTING"){
                    statusText="启动中"
                }
                if(row.status.toUpperCase() == "OUT_OF_SERVICE"){
                    statusText="离线"
                    textClass = "stopText"
                }
                if(row.status.toUpperCase() == "UNKNOWN"){
                    statusText="未知"
                }
                return h('span', { class: textClass }, t(statusText)); // 输出格式化后的日期和时间
            }
        },
        {
            title: computed(() => t('注册时间')),
            key: 'createTime',
            render: (row) => {
                let date = new Date(row.createTime);
                let year = date.getFullYear();
                let month = (date.getMonth() + 1).toString().padStart(2, '0'); // 月份从0开始，需要加1并补零
                let day = date.getDate().toString().padStart(2, '0'); // 日期补零
                let hours = date.getHours().toString().padStart(2, '0'); // 小时补零
                let minutes = date.getMinutes().toString().padStart(2, '0'); // 分钟补零
                let seconds = date.getSeconds().toString().padStart(2, '0'); // 秒补零

                let formattedDateTime = year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
                return formattedDateTime; // 输出格式化后的日期和时间
            }
        },
        {
            title: computed(() => t('操作')),
            fixed: 'right',
            width: 210,
            render: (row) => {
                return h('div', [h('span', { onClick: () => { view(row) } }, t("详情")),
                h('span', { class: 'leftMargin', onClick: () => { edit(row,"OUT_OF_SERVICE") } }, t('下线')),
                h('span', { class: 'leftMargin', onClick: () => { edit(row,"UP") } }, t('上线')),
                h('span', { class: 'leftMargin', onClick: () => { openChartView(row) } }, t('服务使用情况')),
                ]);
            }
        }
    ],
    tableData: [],
});

// 过滤条件
const filterOperaConfig = ref({
    filtersValueCallBack: (filter) => {
        query.value = filter;
    },
    itemList: [
        {
            type: 'input',
            value: '',
            key: 'name',
            label: computed(() => t('名称')),
            labelWidth: '42px',
            span: settingStore.device === 'mobile' ? 24 : 6
        },
        {
            type: 'input',
            value: '',
            key: 'ip',
            label: computed(() => t('IP')),
            labelWidth: '42px',
            span: settingStore.device === 'mobile' ? 24 : 6
        },
        {
            type: 'slot',
            slotName: 'slotSearch',
            span: 6
        },
        {
            type: 'slot',
            slotName: 'slotBtns',
            span: settingStore.device === 'mobile' ? 24 : 12,
            justify: 'flex-end'
        }

    ],
    showBorder: true,
    borderRadio: '4px'
});

//获取已接入接口列表
async function getDataList(type) {
    y9TableConfig.value.loading = true;
    let res = await getPage(
        query.value
    )
    let tableData = analysisData(res)
    //遍历数组
    let traverseData = tableData
    //赋值数组
    let fzData = []
    if(type!=null){
        for(let key in query.value){
            if(fzData.length!=0){
                traverseData = fzData
                fzData = []
            }
            for(let row of traverseData){
                if(row[key].indexOf(query.value[key])!=-1){
                    fzData.push(row)
                }
            }
            if(fzData.length==0){
                break;
            }
        }
        y9TableConfig.value.tableData = fzData || []
        y9TableConfig.value.loading = false;
    }else{
        y9TableConfig.value.tableData = tableData || []
        y9TableConfig.value.loading = false;
    }
}
//解析json数据
function analysisData(data){
    //存储需要的列表数据
    let tableData = []
    //获取注册应用数据列表
    let applications = data.applications.application
    //解析转化为table数据
    for(let application of applications){
        let appName = application.name
        //解析实例
        for(let instance of application.instance){
            let instanceData = {
                name: appName,
                instanceId: instance.instanceId,
                hostName:instance.hostName,
                ip: instance.ipAddr,
                port: instance.port["$"],
                status: instance.status,
                createTime: instance.leaseInfo.registrationTimestamp,
                baseUrl: instance.healthCheckUrl.slice(0,-6)
            }
            tableData.push(instanceData)
        }
    }
    return tableData
}
//查询按钮
function search() {
    y9TableConfig.value.tableData = []
    getDataList(true)
}
//重置按钮
function reset() {
    filterRef.value.elTableFilterRef.onReset()
    y9TableConfig.value.tableData = []
    query.value = {}
    getDataList(null)
}
//初始化table数据
function initTableData() {
    y9TableConfig.value.tableData = []
    getDataList(null)
}
initTableData()
// 应用 添加 修改表单ref
const ruleFormRef = ref();
const validateNumber = (rule: any, value: any, callback: any) => {
    let result = $validCheck('number', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};
// 服务器使用情况
let openChartConfig = ref({
    show: false,
    title: computed(() => t('')),
    showFooter: true,
    onOkLoading: true,
    onOk:(newConfig) =>{
        openChartView(qbaseUrl.value)
    }
});

// 增加 修改应用 弹框的变量配置 控制
let addDialogConfig = ref({
    show: false,
    title: computed(() => t('新增权限配置')),
    showFooter: true,
    onOkLoading: true,
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            const y9RuleFormInstance = ruleFormRef.value?.elFormRef;
            await y9RuleFormInstance.validate(async (valid) => {
                if (valid) {
                    let data = ruleFormRef.value.model;
                    let formData = new FormData();
                    for (let key in data) {
                        if (data[key] != null && key != "createTime" && key != "updateTime")
                            formData.append(key, data[key])
                    }
                    let res = await saveInfo(
                        formData
                    )
                    if (res.code == 0) {
                        ElMessage({
                            message: '数据保存成功',
                            type: 'success'
                        })
                        resolve();
                        getDataList(null);
                    }
                    reject();
                } else {
                    reject();
                }
            });
        });
    }
});
//校验ip格式
const validateIP = (rule: any, value: any, callback: any) => {
    let regular = /^[0-9.,]+$/;
    if(!regular.test(value)){
        callback(new Error("输入了非法字符请检查"));
    }else{
        let ipList = value.split(",")
        for(let it of ipList){
            let ipItems = it.split(".")
            if(ipItems.length!=4){
                callback(new Error("IP"+it+"输入不合法请检查"));
            }else{
                for(let ipItem of ipItems){
                    if(ipItem!=null && ipItem!="" && ipItem.length!=0){
                        if(Number(ipItem)<0||Number(ipItem)>255){
                            callback(new Error("IP "+it+" 输入不合法请检查"));
                        }
                    }else{
                        callback(new Error("IP "+it+" 输入不合法请检查"));
                    }
                }
            }
        }
        callback();
    }
};
// 接口表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        isPrimary: 'Y',
        isEnable: false
    },
    itemList: [
        {
            type: 'textarea',
            label: computed(() => t('应用名称')),
            prop: 'name',
            props:{
                rows:2
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('实例ID')),
            prop: 'instanceId',
            props:{
                rows:2
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('域名')),
            prop: 'hostName',
            props:{
                rows:2
            }
        },
        {
            type: 'input',
            label: computed(() => t('IP')),
            prop: 'ip',
        },
        {
            type: 'input',
            label: computed(() => t('端口')),
            prop: 'port',
        },
        {
            type: 'input',
            label: computed(() => t('状态')),
            prop: 'status'
        },
        {
            type: 'input',
            label: computed(() => t('注册时间')),
            prop: 'createTime',
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
//编辑
async function edit(row,type) {
    let alertText = "下线"
    if (type == "UP") {
        alertText = "上线"
    }
    ElMessageBox.confirm(
        '是否确认' + alertText + "该服务实例",
        alertText+'确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
            draggable: true
        }
    ).then(() => {
        let data = {
            url: "/" + row.name + "/" + row.instanceId + "/status?value=" + type,
            data: { "status": type }
        }
        saveInfo(data).then((res) => {
            getDataList(null)
            ElMessageBox.alert("实例正在" + alertText + "，请稍后刷新页面！", "实例" + alertText + "确认", {
                confirmButtonText: '确认'
            })
        })
    }).catch(() => {

    })
}
//详情
async function view(data) {
    let row = JSON.parse(JSON.stringify(data))
    let statusText = "-"
    if (row.status.toUpperCase() == "UP") {
        statusText = "健康"
    }
    if (row.status.toUpperCase() == "DOWN") {
        statusText = "宕机"
    }
    if (row.status.toUpperCase() == "STARTING") {
        statusText = "启动中"
    }
    if (row.status.toUpperCase() == "OUT_OF_SERVICE") {
        statusText = "离线"
    }
    if (row.status.toUpperCase() == "UNKNOWN") {
        statusText = "未知"
    }
    row.status = statusText
    let date = new Date(row.createTime);
    let year = date.getFullYear();
    let month = (date.getMonth() + 1).toString().padStart(2, '0'); // 月份从0开始，需要加1并补零
    let day = date.getDate().toString().padStart(2, '0'); // 日期补零
    let hours = date.getHours().toString().padStart(2, '0'); // 小时补零
    let minutes = date.getMinutes().toString().padStart(2, '0'); // 分钟补零
    let seconds = date.getSeconds().toString().padStart(2, '0'); // 秒补零

    let formattedDateTime = year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
    console.log(formattedDateTime)
    row.createTime = formattedDateTime
    ruleFormConfig.value.model = row
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props == undefined) {
            it.props = {
                disabled: true
            }
        } else {
            it.props.disabled = true
        }
    }
    addDialogConfig.value.okText = false
    addDialogConfig.value.title = computed(() => t('查看实例信息'))
    addDialogConfig.value.show = true
}
const openChartView = (data)=>{
    qbaseUrl.value = data
    openChartConfig.value.okText = "刷新"
    openChartConfig.value.title = computed(() => t('查看服务使用情况'))
    openChartConfig.value.show = true
    let baseData = {
        baseUrl:data.baseUrl
    }
    getCpuUsed(baseData).then((res)=>{
        console.log(res)
        nextTick(() => {
            const myChart = echarts.init(cpuChartContainerRef.value)
            myChart.setOption(buildOptions(parseFloat(res.measurements[0].value.toFixed(4))))
        })
    })
    getMaxMemory(baseData).then((res) => {
        let maxMemory = res.measurements[0].value
        getUsedMemory(baseData).then((res) => {
            let usedMemory = res.measurements[0].value;
            let used = (usedMemory/maxMemory).toFixed(4);
            nextTick(() => {
                const memoryChart = echarts.init(memoryChartContainerRef.value)
                memoryChart.setOption(buildOptions(parseFloat(used)))
            })
        })
    })

    let param = {
        id:data.instanceId
    }
    getRegisterNum(param).then((res)=>{
        serviceUsedData.value.registerCount = res.data[0].num
    })
    getInvokeNumToday(param).then((res)=>{
        serviceUsedData.value.todayCount = res.data
    })
    getHttpServerReqs(baseData).then((res)=>{
        let measurements = res.measurements;
        for(let it of measurements){
            if(it.statistic.toUpperCase()=="COUNT"){
                serviceUsedData.value.aveCount = calculateAverageDailyCallCount(data.createTime,it.value)
                break;
            }
        }
    })
}
// 计算平均每天调用次数
const calculateAverageDailyCallCount = (registrationDate,totalCallCount) => {
    const now = new Date();
    const registrationDateObj = new Date(registrationDate);
    const daysSinceRegistration = (now - registrationDateObj) / (1000 * 60 * 60 * 24);
    if(daysSinceRegistration<1){
        return totalCallCount;
    }
    const averageDailyCallCount = totalCallCount / daysSinceRegistration;
    return averageDailyCallCount.toFixed(2); // 保留两位小数
};

function Pie() {
  let dataArr = [];
  for (var i = 0; i < 150; i++) {
      if (i % 2 === 0) {
          dataArr.push({
              name: (i + 1).toString(),
              value: 50,
              itemStyle: {
                  normal: {
                      color: "#00AFFF",
                      borderWidth: 0,
                      borderColor: "rgba(0,0,0,0)",
                  }
              }
          })
      } else {
          dataArr.push({
              name: (i + 1).toString(),
              value: 100,
              itemStyle: {
                  normal: {
                      color: "rgba(0,0,0,0)",
                      borderWidth: 0,
                      borderColor: "rgba(0,0,0,0)"
                  }
              }
          })
      }
  }
  return dataArr
}
function buildOptions(usedData){
    let options = {
    backgroundColor: 'transparent', // 画布背景色
    series: [
      {
        // value: 50, //  内容 配合formatter
        type: 'liquidFill',
        radius: '70%', // 控制中间圆球的尺寸（此处可以理解为距离外圈圆的距离控制）
        center: ['50%', '50%'],
        data: [usedData, {
          value: usedData,
          direction: 'left', //波浪方向
        }], // data个数代表波浪数
        backgroundStyle: {
          borderWidth: 1,
          color: 'rgba(62, 208, 255, 1)' // 球体本景色
        },
        amplitude: '6  %',//波浪的振幅
        // 修改波浪颜色
        // color: ['#0286ea', 'l#0b99ff'], // 每个波浪不同颜色，颜色数组长度为对应的波浪个数
        color: [{
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 1,
              color: '#6CDEFC',
            },
            {
              offset: 0,
              color: '#429BF7',
            },
          ],
          globalCoord: false,
        },
        ],
        label: {
          normal: {
            formatter: (usedData * 100).toFixed(2)+'%',
            //  formatter: function(params){
            //     return params.value* 100 + " \n%";
            // },
            rich: {
              d: {
                fontSize: 20,
              }
            },
            textStyle: {
              fontSize: 32,
              color: '#fff'
            }
          },
 
        },
        outline: {
          show: false
        }
      },
      {
        type: 'pie',
        z: 1,
        center: ['50%', '50%'],
        radius: ['72%', '73.5%'], // 控制外圈圆的粗细
        hoverAnimation: false,
        data: [
          {
            name: '',
            value: 500,
            labelLine: {
              show: false
            },
            itemStyle: {
              color: '#00AFFF'
            },
            emphasis: {
              labelLine: {
                show: false
              },
              itemStyle: {
                color: '#00AFFF'
              }
            }
          }
        ]
      },
      { //外发光
        type: 'pie',
        z: 1,
        zlevel: -1,
        radius: ['70%', '90.5%'],
        center: ["50%", "50%"],
        hoverAnimation: false,
        clockWise: false,
        itemStyle: {
          normal: {
            borderWidth: 20,
            color: 'rgba(224,242,255,1)',
          }
        },
        label: {
          show: false
        },
        data: [100]
      },
      { //底层外发光
        type: 'pie',
        z:1,
        zlevel: -2,
        radius: ['70%', '100%'],
        center: ["50%", "50%"],
        hoverAnimation: false,
        clockWise: false,
        itemStyle: {
          normal: {
            borderWidth: 20,
            color: 'rgba(224,242,255,.4)',
          }
        },
        label: {
          show: false
        },
        data: [100]
      },
      // 虚点
      {
        type: 'pie',
        zlevel: 0,
        silent: true,
        radius: ['78%', '80%'],
        z: 1,
        label: {
            normal: {
                show: false
            },
        },
        labelLine: {
            normal: {
                show: false
            }
        },
        data: Pie()
    },
    ]
  }
  return options
}
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
</style>
<style scoped>
.box-card {
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter);
}
:deep(.el-card__header) {
    padding: 12px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background-color: var(--el-bg-color);
}

:deep(.el-card__body) {
    height: 130px !important;
    padding: 0px 15px 15px 15px !important;
}
.overview-content {
  height: calc(100% - 21px);
  display: flex;
  padding: 0px 20px;

  .stat-items {
    width: 100%;
    display: flex;
    justify-content: space-around;
    align-items: center;

    .stat-item {
      text-align: center;
      min-width: 80px;

      h2 {
        font-size: 45px;
        color: var(--el-color-primary);
        margin-bottom: 6px;
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
.labelTitle{
    text-align: center;
    display: flex; justify-content: center;
    span {
        font-size: 16px;
        color: #666;
      }
}
.bottomArea{
    margin-top: 10px;
}
</style>
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

</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getPage, saveInfo, getInfoById, delInfoById, updateEnable } from '@/api/execute/execute'
import {getMayApplyInterfaceList} from '@/api/interface/interface'
import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus';
import '@/assets/css/tablestatusfontcolor.css';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const selectedDate = ref();
const query: any = ref({});
const filterRef = ref();
const interfaceList = ref([]);

const limitInfo = ref(false)
const sameId = ref()

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
            render: (row) => {
                return h('div', [h('span', { onClick: () => { view(row) } }, t("详情")),
                h('span', { class: 'leftMargin', onClick: () => { edit(row,"OUT_OF_SERVICE") } }, t('下线')),
                h('span', { class: 'leftMargin', onClick: () => { edit(row,"UP") } }, t('上线')),
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
            labelWidth: '82px',
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
                createTime: instance.leaseInfo.registrationTimestamp
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
function addDialog() {
    initInterfaceList()
    ruleFormConfig.value.model = {isEnable: true }
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props == undefined) {
            it.props = {
                disabled: false
            }
        } else {
            it.props.disabled = false
        }
    }
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('新增黑名单信息'))
    addDialogConfig.value.show = true
}


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
//删除
async function delData(id) {
    ElMessageBox.confirm(
        '是否确认删除这条数据',
        '删除数据确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
            draggable: true
        }
    ).then(() => {
        let para = {
            id: id
        }
        delInfoById(para).then((res) => {
            if (res.status == "success") {
                ElMessage({ type: 'info', message: '删除成功' })
                getDataList(null)
            } else {
                ElMessage({ type: 'warning', message: "删除失败" + res.msg })
            }
        })
    }).catch(() => {

    })
}
async function initInterfaceList() {
    let para = {
    page:1,
    limit:9999999,
    mayApply:'发布'
}
getMayApplyInterfaceList(para).then((res) => {
    interfaceList.value = []
    for (let it of res.data) {
        let item = {
            label: it.interfaceName+"-"+it.version,
            value: it.id
        }
        interfaceList.value.push(item)
    }

    for(let it of ruleFormConfig.value.itemList){
        if(it.prop=='interfaceIds'){
            it.props.options = interfaceList.value
        }
    }
})
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
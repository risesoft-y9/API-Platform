<template>
    <y9Table :config="y9TableConfig" :filterConfig="filterOperaConfig" @on-curr-page-change="handlerCurrPage"
        ref="filterRef" @on-page-size-change="handlerPageSize" v-model:selectedVal="selectedCheckboxVal">
        <template v-slot:slotSearch>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="search">
                <i class="ri-search-line"></i>
                <span>{{ $t('搜索') }}</span>
            </el-button>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="el-button el-button--default global-btn-third" @click="reset">
                <i class="ri-refresh-line"></i>
                <span>{{ $t('重置') }}</span>
            </el-button>
        </template>
        <template v-slot:slotBtns>
            <!-- <el-button v-if="props.status != '发布' && props.status != '申请'" :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                @click="addDialog">{{ $t('新增') }}
            </el-button> -->
            <div class="btnDiv">
                <el-button :size="fontSizeObj.buttonSize" v-if="props.status != '发布' && props.status != '申请'"
                    :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                    @click="uploadFile">
                    <i class="ri-download-2-line"></i>
                    <span>{{ $t('导入') }}</span>
                </el-button>
                <el-button :size="fontSizeObj.buttonSize" v-if="props.status != '发布' && props.status != '申请'"
                    :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                    @click="exportFile">
                    <i class="ri-upload-2-line"></i>
                    <span>{{ $t('导出') }}</span>
                </el-button>
            </div>
        </template>
    </y9Table>
    <pubOrStop ref="pubOrStopRef" @getDataListParent="getDataListParent" />
    <interfaceApply ref="interfaceApplyRef" @getDataListParent="getDataListParent" />
    <flowShow ref="flowShowRef" v-model:open-dialog="isFlowShow" v-model:interfaceId="flowInterfaceId"
        v-model:type="flowType" v-model:applyType="applyType"></flowShow>
    <interfaceDialog ref="interfaceDialogRef" @getDataListParent="getDataListParent" v-model:isShow="isShow" />
    <interfaceApplyTable ref="interfaceApplyTableRef"></interfaceApplyTable>

    <!-- 导入界面 -->
    <el-dialog v-model="uploadOpen" :title="uploadTitle">
        <el-upload ref="uploadRef" :data="param" class="upload-demo" :on-success="handleSuccess" :file-list="fileList"
            :on-change="handleChange" :on-remove="handleRemove" drag :action="acction" :auto-upload="false" :limit="1"
            :accept="accept" :before-upload="beforeUpload" :headers="headers">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
                拖拽上传或者<em>点击上传文件</em>
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    {{ uploadTip }}
                </div>
            </template>
        </el-upload>
        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main" @click="confirDialog()"
                v-loading.fullscreen.lock="uploadLoading">
                <span>{{ $t('保存') }}</span>
            </el-button>
            <el-button @click="closeDialog()">
                <span>{{ $t('关闭') }}</span>
            </el-button>
        </template>
    </el-dialog>
    <el-dialog v-model="previewOpen" :title="uploadTitle" width="70%" class="dialog" ref="previewDialogRef">
        <iframe :src="pdfUrl" frameborder="0" width="100%" :height="ifReameHeight" ref="myIframeRef"></iframe>
        <template #footer>
            <el-button @click="closePreviewDialog()">关闭</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, nextTick } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getInterfaceListByPersonId, delInterfaceById, getMayApplyInterfaceList, getAlreadyApplyInterfaceList, exportInterface,downLoadInterfaceFile } from '@/api/interface/interface'
import {findIsPass} from '@/api/apply/apply';
import { getFlowSelect } from '@/api/flownode/flownode';
import { ElMessage, ElMessageBox } from 'element-plus';
import pubOrStop from './pubOrStop.vue';
import flowShow from './flowShow.vue';
import { useRoute } from 'vue-router';
import interfaceDialog from './interfaceDialog.vue';
import interfaceApply from './interfaceApply.vue';
import interfaceApplyTable from './interfaceApplyTable.vue';
import y9_storage from '@/utils/storage';
import settings from '@/settings';
import '@/assets/css/tablestatusfontcolor.css';


// 注入 字体对象
const appIndex = import.meta.env.VUE_APP_HOST_INDEX;
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const access_token = y9_storage.getObjectItem(settings.siteTokenKey, 'access_token');
const headers = ref({})
if (access_token) {
    headers.value = {
        Authorization: 'Bearer ' + access_token
    }
}
const acction = ref(import.meta.env.VUE_APP_CONTEXT+"api/rest/interface/uploadInterface")
const VUE_APP_CONTEXT = ref(import.meta.env.VUE_APP_CONTEXT)
const uploadOpen = ref(false)
const uploadTitle = ref("导入")
const accept = ref(".zip,.json")
const uploadTip = ref("zip/json 格式的文件，不超过10MB。")
const fileMaxSize = ref(10)
const isOverWrite = ref(true)
const param = ref({
    isOverWrite: isOverWrite.value
})
const fileList = ref()
const uploadLoading =ref(false)
const isSelectFile = ref(false)
const route = useRoute();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const pubOrStopRef = ref();
const uploadRef = ref();
const interfaceApplyRef = ref();
const interfaceDialogRef = ref()
const interfaceApplyTableRef = ref()
const authApplyInfoRef = ref()
const isFlowShow = ref(false)
const flowInterfaceId = ref()
const isShow = ref(true)
const pdfUrl = ref()
const myIframeRef = ref()
const previewOpen = ref(false)
const ifReameHeight = ref("835px")
const previewDialogRef = ref()
const props = defineProps({
    status: {
        type: String,
        default: () => "其他"
    }
})
const flowType = ref(true)
const applyType = ref(true)
const selectedCheckboxVal = ref([])
//校验数字格式
const validateNumber = (rule: any, value: any, callback: any) => {
    let result = $validCheck('number', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};
const tableItem = [
    {
        type: 'index',
        title: computed(() => t('序号')),
        width: 80,
        fixed: 'left'
    },
    {
        title: computed(() => t('接口名称')),
        key: 'interfaceName',
        sortable: true
    },
    {
        title: computed(() => t('接口类型')),
        key: 'interfaceType',
        sortable: true
    },
]
if (props.status != '申请' && props.status != '发布') {
    let selection = {
        type: "selection",
        width: 60,
    }
    tableItem.unshift(selection);
    tableItem.push({
        title: computed(() => t('接口状态')),
        key: 'interfaceStatus',
        sortable: true,
        render:(row)=>{
            let statusColor = "successText"
            if(row.interfaceStatus=="发布"){
                statusColor = "successText"
            }else if(row.interfaceStatus=="停用"){
                statusColor = "stopText"
            }else{
                statusColor = "otherText"
            }
            return h('div', { class: statusColor}, t(row.interfaceStatus));
        }
    })
    tableItem.push({
        title: computed(() => t('审核状态')),
        key: 'approveStatus',
        render: (row) => {
            if (row.approveStatus.indexOf("不通过") != '-1') {
                return h('div', "不通过");
            } else {
                return h('div', row.approveStatus);
            }
        },
        sortable: true
    })

}
const endTableItem = [{
    title: computed(() => t('请求方式')),
    key: 'interfaceMethod',
    sortable: true
},
{
    title: computed(() => t('是否限流')),
    key: 'isLimit',
    sortable: true
},
{
    title: computed(() => t('是否鉴权')),
    key: 'isAuth',
    sortable: true
},
{
    title: computed(() => t('接口说明')),
    key: 'illustrate'
},
{
    title: computed(() => t('版本')),
    key: 'version'
},
{
    title: computed(() => t('备注')),
    key: 'notes'
}]
let interfaceFileitem = {
    title: computed(() => t('接口文档操作')),
    width: 110,
    fixed: 'right',
    render: (row) => {
        let btns = []
        let uploadBtn = h('span', { class: 'operate', onClick: () => { uploadInterfaceFile(row.id) } }, t("上传"));
        let viewBtn = h('span', { class: 'leftMargin operate', onClick: () => { viewInterfaceFile(row.interfaceFileUrl,row.id) } }, t("预览"));
        if(props.status!='发布' && props.status != '申请'){
            btns.push(uploadBtn)
        }
        btns.push(viewBtn)
        return h('div', btns);
    }
}
let operateItem = {
    title: computed(() => t('操作')),
    width: (props.status == '发布') ? 120 : (props.status == '申请') ? 120 : 265,
    fixed: 'right',
    render: (row) => {
        let btns = []
        btns = rtBtns(row)
        return h('div', btns);
    }
}
for (let it of endTableItem) {
    tableItem.push(it)
}
if(props.status!='发布'){
    tableItem.push(interfaceFileitem)
}
tableItem.push(operateItem)
//表格配置
let y9TableConfig = ref({
    headerBackground: true,
    pageConfig: {
        background: false, //是否显示背景色
        currentPage: 1, //当前页数，支持 v-model 双向绑定
        pageSize: 15, //每页显示条目个数，支持 v-model 双向绑定
        total: 0, //总条目数
        pageSizeOpts: [5, 10, 15, 20, 30, 40, 1000]
    },
    columns: tableItem,
    tableData: [],
});
//权限按钮控制
function rtBtns(row) {
    let btns = []
    let viewBtn = h('span', { class: 'operate', onClick: () => { view(row.id) } }, t("详情"));
    let editBtn = h('span', { class: 'leftMargin operate', onClick: () => { edit(row.id) } }, t('编辑'));
    let delBtn = h('span', { class: 'leftMargin operate', onClick: () => { delSpanById(row.id) } }, t('删除'));
    let pubBtn = h('span', { class: 'leftMargin operate', onClick: () => { pubData(row.id, "发布",row.interfaceFileUrl) } }, t('发布'));
    let stopBtn = h('span', { class: 'leftMargin operate', onClick: () => { pubData(row.id, "停用") } }, t('停用'));
    let upBtn = h('span', { class: 'leftMargin operate', onClick: () => { updateVersion(row.id) } }, t('版本升级维护'));
    let flowBtn = h('span', { class: 'leftMargin operate', onClick: () => { openFlowShow(row.id) } }, t('审批进度'));
    let flowInfoBtn = h('span', { class: 'leftMargin operate', onClick: () => { openFlowShow(row.id) } }, t('审批详情'));
    let applyBtn = h('span', { class: 'leftMargin operate', onClick: () => { apply(row.id, "", row.isLimitData) } }, t("申请"));
    let alreadyApplyBtn = h('span', { class: 'leftMargin operate', onClick: () => { alreadyApply(row.id, "申请", row.approveStatus, row.isLimitData) } }, t("变更申请"));
    let applyRecordBtn = h('span', { class: 'leftMargin operate', onClick: () => { applyRecord(row.id, row.isLimitData, row.interfaceName) } }, t("申请详情"));
    let interfaceViewBtn = h('span', { class: 'leftMargin operate', onClick: () => { interfaceView(row.id) } }, t("接口详情"));
    let applyInfoBtn = h('span', { class: 'leftMargin operate', onClick: () => { authApply(row.id, "申请", row.interfaceName, row.isLimitData) } }, t("申请详情"));
    let testBtn = h('span', { class: 'leftMargin operate', onClick: () => { openTest(row.id) } }, t("测试"));
    if (props.status == '发布') {
        btns.push(applyBtn)
    } else if (props.status == '申请') {
        btns.push(applyRecordBtn)
    } else if (row.interfaceStatus == "待发布" && row.approveStatus == "未提交") {
        btns.push(viewBtn)
        btns.push(editBtn)
        btns.push(delBtn)
        btns.push(pubBtn)
        btns.push(testBtn)
    } else if (row.interfaceStatus == "待发布" && row.approveStatus == "未审批") {
        btns.push(viewBtn)
        btns.push(flowBtn)
        btns.push(testBtn)
    } else if (row.interfaceStatus == "发布" && row.approveStatus == "未审批") {
        btns.push(viewBtn)
        btns.push(flowBtn)
        btns.push(testBtn)
    } else if (row.interfaceStatus == "发布") {
        btns.push(viewBtn)
        btns.push(flowInfoBtn)
        btns.push(upBtn)
        btns.push(stopBtn)
        btns.push(testBtn)
    } else if (row.interfaceStatus == "待发布" && row.approveStatus.indexOf("不通过") != -1) {
        btns.push(viewBtn)
        btns.push(flowInfoBtn)
        btns.push(pubBtn)
        btns.push(editBtn)
        btns.push(testBtn)
    } else if (row.interfaceStatus == "停用") {
        btns.push(viewBtn)
        btns.push(flowInfoBtn)
        btns.push(pubBtn)
        btns.push(editBtn)
        btns.push(testBtn)
    } else {
        btns.push(viewBtn)
        btns.push(flowBtn)
    }
    return btns;
}
const itemListFilter = [{
    type: 'input',
    value: '',
    key: 'interfaceName',
    label: computed(() => t('接口名称')),
    labelWidth: '62px',
    span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请") ? 4 : 3
},
{
    type: 'select',
    value: '',
    key: 'interfaceType',
    label: computed(() => t('接口类型')),
    labelWidth: '62px',
    span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请") ? 4 : 3,
    props: {
        options: [
            {
                label: "webService",
                value: "webService"
            },
            {
                label: "Rest",
                value: "Rest"
            }
        ]
    }
},
]
if (props.status == "发布" || props.status == "申请") {

} else {
    itemListFilter.push({
        type: 'select',
        value: '',
        key: 'interfaceStatus',
        label: computed(() => t('接口状态')),
        labelWidth: '62px',
        span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请") ? 4 : 3,
        props: {
            options: [
                {
                    label: "待发布",
                    value: "待发布"
                },
                {
                    label: "发布",
                    value: "发布"
                },
                {
                    label: "停用",
                    value: "停用"
                }
            ]
        }
    })
    // itemListFilter.push({
    //     type: 'select',
    //     value: '',
    //     key: 'approveStatus',
    //     label: computed(() => t('审核状态')),
    //     labelWidth: '62px',
    //     span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请")?4:3,
    //     props: {
    //         options: [
    //             {
    //                 label: "未审批",
    //                 value: "未审批"
    //             },
    //         ]
    //     }
    // })
}
let endArry = [{
    type: 'select',
    value: '',
    key: 'interfaceMethod',
    label: computed(() => t('请求方式')),
    labelWidth: '62px',
    span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请") ? 4 : 3,
    props: {
        options: [
            {
                label: "get",
                value: "get"
            },
            {
                label: "post",
                value: "post"
            },
        ]
    }
},
{
    type: 'select',
    value: '',
    key: 'isLimit',
    label: computed(() => t('是否限流')),
    labelWidth: '62px',
    span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请") ? 4 : 3,
    props: {
        options: [
            {
                label: "是",
                value: "是"
            },
            {
                label: "否",
                value: "否"
            },
        ]
    }
},
{
    type: 'select',
    value: '',
    key: 'isAuth',
    label: computed(() => t('是否鉴权')),
    labelWidth: '62px',
    span: settingStore.device === 'mobile' ? 24 : (props.status == "发布" || props.status == "申请") ? 4 : 3,
    props: {
        options: [
            {
                label: "是",
                value: "是"
            },
            {
                label: "否",
                value: "否"
            },
        ]
    }
},
{
    type: 'slot',
    slotName: 'slotSearch',
    span: 3
},
{    type: 'slot',
    slotName: 'slotBtns',
    span:3}
]
for (let it of endArry) {
    itemListFilter.push(it)
}
// 过滤条件
const filterOperaConfig = ref({
    filtersValueCallBack: (filter) => {
        query.value = filter;
    },
    itemList: itemListFilter,
    showBorder: true,
    borderRadio: '4px'
});

const getDataListParent = () => {
    getDataList()
}
//获取已接入接口列表
async function getDataList() {
    y9TableConfig.value.loading = true;
    query.value.page = y9TableConfig.value.pageConfig.currentPage;
    query.value.limit = y9TableConfig.value.pageConfig.pageSize;
    let res;
    if (props.status == "其他") {
        res = await getInterfaceListByPersonId(
            query.value
        )
    } else if (props.status == "发布") {
        query.value.mayApply = props.status
        res = await getMayApplyInterfaceList(
            query.value
        )
    } else if (props.status == "申请") {
        query.value.mayApply = props.status
        res = await getAlreadyApplyInterfaceList(
            query.value
        )
    }
    y9TableConfig.value.tableData = res.data || []
    y9TableConfig.value.pageConfig.total = res.count || 0;
    y9TableConfig.value.loading = false;
}
//  应用列表 分页 触发
function handlerPageSize(pageSize) {
    y9TableConfig.value.tableData = []
    y9TableConfig.value.pageConfig.pageSize = pageSize
    getDataList()
}
//切换页面除触发
function handlerCurrPage(currentPage) {
    y9TableConfig.value.tableData = []
    y9TableConfig.value.pageConfig.currentPage = currentPage
    getDataList()
}
//查询按钮
function search() {
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 15;
    y9TableConfig.value.tableData = []
    getDataList()
}
//重置按钮
function reset() {
    filterRef.value.elTableFilterRef.onReset()
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 15;
    y9TableConfig.value.tableData = []
    query.value = {}
    getDataList()
}
//初始化table数据
function initTableData() {
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 15;
    y9TableConfig.value.tableData = []
    getDataList()
}
initTableData()
//新增按钮
async function addDialog() {
    interfaceDialogRef.value.addDialog()
}

//编辑按钮
async function edit(id) {
    interfaceDialogRef.value.edit(id)
}
//版本更新按钮
async function updateVersion(id) {
    interfaceDialogRef.value.updateVersion(id)
}
//详情按钮
async function view(id) {
    isShow.value = true
    interfaceDialogRef.value.view(id, true)
}
//接口详情按钮
function interfaceView(id) {
    isShow.value = false
    interfaceDialogRef.value.view(id, false)
}
//删除按钮方法
const delSpanById = (id) => {
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
        delData(id)
    }).catch(() => {
    })
}
//删除数据
async function delData(id) {
    let para = {
        id: id
    }
    let res = await delInterfaceById(para)
    if (res.status == "success") {
        ElMessage({ type: 'info', message: '删除成功' })
    } else {
        ElMessage({ type: 'warning', message: res.msg })
    }
    getDataList()
}
//发布接口数据
function pubData(id, type, url) {
    if (type == "发布") {
        if (url != null && url != undefined && url != "") {
            pubOrStopRef.value.openPubDialog(id, type)
        } else {
            ElMessage({ type: 'warning', message: '未上传接口文档无法发布！' })
        }
    }else{
        pubOrStopRef.value.openPubDialog(id, type)
    }
}
//申请按钮
function apply(id, type, isAuth) {
    getFlowSelect().then((res) => {
        interfaceApplyRef.value.openPubDialog(id, type, "", isAuth, res.data);
    })
}
//申请记录按钮
function applyRecord(id, isAuth, interfaceName) {
    interfaceApplyTableRef.value.openPubDialog(id, isAuth, interfaceName);
}
//已经申请按钮
function alreadyApply(id, type, status, isAuth) {
    if (status == "高院审批通过") {
        interfaceApplyRef.value.openPubDialog(id, type, status, isAuth);
    } else {
        apply(id, type, isAuth)
    }
}
//申请详情按钮
function authApply(id, type, name, isAuth) {
    authApplyInfoRef.value.openPubDialog(id, type, name, isAuth);
}
//查看办理进度
function openFlowShow(id) {
    isFlowShow.value = true
    flowInterfaceId.value = id
    flowType.value = true
    if (props.status == "申请") {
        applyType.value = true
    } else {
        applyType.value = false
    }
}

//导出接口信息
const exportFile = () => {
    if(selectedCheckboxVal.value.length==0){
        ElMessage({ type: 'warning', message: "未选择导出项" })
        return;
    }
    let ids =""
    for(let it of selectedCheckboxVal.value){
        ids+=it.id+","
    }
    
    let param = {
        ids: ids.substring(0,ids.length-1)
    }
    exportInterface(param).then((res) => {
        const a = document.createElement('a')
        a.href = URL.createObjectURL(res)
        a.download = "接口信息导出" + '.zip'
        a.click()
    })
}
//导入接口信息
const uploadFile = () => {
    param.value = {
        isOverWrite: isOverWrite.value
    }
    acction.value = VUE_APP_CONTEXT.value + "api/rest/interface/uploadInterface"
    uploadTitle.value = "导入"
    accept.value = ".zip,.json"
    uploadTip.value = "zip/json 格式的文件，不超过10MB。"
    fileMaxSize.value = 10
    uploadOpen.value = true
}
//导入界面确认按钮
const confirDialog = ()=>{
    if (!isSelectFile.value) {
        ElMessage({ type: 'warning', message: "未选择文件" });
        return
    }
    uploadLoading.value = true
    uploadRef.value!.submit()
    isSelectFile.value = false
}
//导入界面取消按钮
const closeDialog = ()=>{
    uploadOpen.value = false
    fileList.value = []
    isSelectFile.value = false
}
//文件改变的钩子函数
const handleChange = (files)=>{
    console.log(files.length)
    if(files){
        isSelectFile.value = true;
    }else{
        isSelectFile.value = false;
    }
}
//文件删除时的钩子函数
const handleRemove = ()=>{
    isSelectFile.value = false;
}
//导入界面上传前的钩子函数，用于判断文件大小
const beforeUpload = (file) =>{
    const isLt500M = file.size / 1024 / 1024 / 1024 < fileMaxSize.value;
      if (!isLt500M) {
        ElMessage({ type: 'warning', message: "上传的文件大小不能超过 "+fileMaxSize.value+" MB!" });
      }
      return isLt500M;
}
//上传成功的钩子函数
const handleSuccess = (response,uploadFile,uploadFiles)=>{
    uploadOpen.value = false
    uploadLoading.value = false
    fileList.value = []
    if(response.status==true){
        ElMessage({ type: 'success', message: uploadTitle.value+"成功！" });
        getDataList()
    }else{
        ElMessage({ type: 'warning', message: uploadTitle.value + "失败！"+response.msg });
    }
}
//接口文档上传按钮
const uploadInterfaceFile = (id) => {
    param.value = {
        interfaceId: id
    }
    acction.value = VUE_APP_CONTEXT.value + "api/rest/interface/uploadInterfaceFile"
    uploadTitle.value = "上传接口文档"
    accept.value = ".pdf"
    uploadTip.value = "pdf格式的文件，不超过100MB。"
    fileMaxSize.value = 100
    uploadOpen.value = true
}
//接口文档预览按钮
const viewInterfaceFile = (url, interfaceId) => {

    if (url != null && url != undefined && url != "") {
        if (props.status == "发布" || props.status == "申请") {
            let para = {
                id: interfaceId
            }
            findIsPass(para).then((res) => {
                if (res.isPass) {
                    downLoadInterfaceFile(url).then((response) => {
                        const binaryData = [];
                        binaryData.push(response);
                        const url = window.URL.createObjectURL(new Blob(binaryData, { type: 'application/pdf' }));
                        window.open(url);
                    })
                } else {
                    ElMessage({ type: 'warning', message: "您所申请的接口调用申请尚未通过，暂无法查看接口文档！" });
                }
            })
        } else {
            downLoadInterfaceFile(url).then((response) => {
                const binaryData = [];
                binaryData.push(response);
                const url = window.URL.createObjectURL(new Blob(binaryData, { type: 'application/pdf' }));
                window.open(url);
                // pdfUrl.value = url
                // previewOpen.value = true
                // uploadTitle.value = "接口文档预览"
                // nextTick(()=>{
                //     let dheight = previewDialogRef.value.offsetHeight - 40 -48
                //     console.log(dheight)
                //     // ifReameHeight.value = dheight + "px"
                // })
            })
        }
    } else {
        ElMessage({ type: 'warning', message: "未上传接口文档，无法预览！" });
    }
}
//关闭预览页面
const closePreviewDialog = () => {
    previewOpen.value = false
}

//打开测试页面
const openTest =(id)=>{
    window.open(appIndex+"interfaceTest?id="+id,"_blank")
}
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}

.dialog {
    margin-top: 0px !important;
    margin-bottom: 0px !important;
    height: 100% !important;
}
.btnDiv{
    width: 100%;
    text-align: right;
}
</style>
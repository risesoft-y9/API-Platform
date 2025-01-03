<template>
    <!--申请详情列表 -->
    <el-dialog v-model="openShow" :title="limitTitle" width="55%">
        <y9Table :config="y9TableConfig" @on-curr-page-change="handlerCurrPage" ref="filterRef"
            @on-page-size-change="handlerPageSize">
            <template v-slot:slotSearch>
                <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main" type="primary" @click="search">{{ $t('查询') }}
                </el-button>
                <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="el-button el-button--default global-btn-third" @click="reset">{{ $t('重置') }}
                </el-button>
            </template>
        </y9Table>

        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
                @click="confirDialog('1')">确定</el-button>
            <el-button @click="closeDialog('1')">取消</el-button>
        </template>
    </el-dialog>
    <interfaceApplyInfo ref="interfaceApplyInfoRef" v-model:isView="isView"></interfaceApplyInfo>
    <interfaceDialog ref="interfaceDialogRef" v-model:isShow="isShow" />
    <interfaceApply ref="interfaceApplyRef" @getDataListParent="getDataList" />
    <flowShow ref="flowShowRef" v-model:open-dialog="isFlowShow" v-model:interfaceId="flowInterfaceId"
    v-model:type="flowType" v-model:applyType="applyType"></flowShow>
    <authApplyInfo ref="authApplyInfoRef"></authApplyInfo>
</template>
<script lang="ts" setup>
import { computed, ref ,nextTick,h,inject} from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import {getApplyListById,getApplyInfoById} from '@/api/apply/apply'
import { getFlowSelect } from '@/api/flownode/flownode';
import interfaceApplyInfo from './interfaceApplyInfo.vue';
import interfaceDialog from './interfaceDialogApplyTable.vue';
import interfaceApply from './interfaceApplyDialog.vue';
import flowShow from './flowShow.vue';
import authApplyInfo from './authApplyInfo.vue';

// 注入 字体对象
const { t } = useI18n();
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const filterRef = ref();
const interfaceApplyRef = ref();
const interfaceApplyInfoRef = ref()
const query: any = ref({});
const limitTitle = ref("申请详情")
const openShow = ref(false)
const isView = ref()
const isShow = ref(true)
const isAuth = ref(false)
const interfaceDialogRef = ref()
const isFlowShow = ref(false)
const flowInterfaceId = ref()
const flowType = ref()
const applyType = ref()
const authApplyInfoRef = ref()
const interfaceName = ref("")

const props = defineProps({
    openDialog:{
        type:Boolean,
        default:()=>false
    },
    isView:{
        type:Boolean,
        default:()=>false
    },
    interfaceId:{
        type:String,
    },
    selectData:{
        type:String
    }
})
const emit = defineEmits(['update:openDialog','update:selectData','getDataListParent'])

//权限按钮控制
function rtBtns(row){
    let btns = []
    let viewBtn = h('span', {class: 'operate', onClick: () => { view(row.id) } }, t("详情"));
    let viewInfoBtn = h('span', {class: 'operate', onClick: () => { authApply(row.id,"","","") } }, t("详情"))
    let interfaceViewBtn = h('span', { class: 'leftMargin operate', onClick: () => { interfaceView(row.interfaceId) } }, t("接口详情"))
    let alreadyApplyBtn = h('span', { class: 'leftMargin operate', onClick: () => { alreadyApply(row.id, "申请", row.approveStatus,row.isAuth) } }, t("变更申请"));
    let flowBtn = h('span', { class: 'leftMargin operate', onClick: () => { openFlowShow(row.approveId) } }, t('审批进度'));
    let flowInfoBtn = h('span', { class: 'leftMargin operate', onClick: () => { openFlowShow(row.approveId) } }, t('审批详情'));
    if (row.approveStatus == "通过") {
        btns.push(viewInfoBtn)
        btns.push(alreadyApplyBtn)
        btns.push(interfaceViewBtn)
        btns.push(flowInfoBtn)
    } else if(row.approveStatus == "不通过"){
        btns.push(viewBtn)
        btns.push(alreadyApplyBtn)
        btns.push(flowInfoBtn)
    }else{
        btns.push(viewBtn)
    }

    return btns;
}

//表格配置
let y9TableConfig = ref({
    headerBackground: true,
    pageConfig: {
        background: false, //是否显示背景色
        currentPage: 1, //当前页数，支持 v-model 双向绑定
        pageSize: 10, //每页显示条目个数，支持 v-model 双向绑定
        total: 0, //总条目数
    },
    columns: [
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
            fixed: 'left'
        },
        {
            title: computed(() => t('申请事由')),
            key: 'applyReason'
        },
        {
            title: computed(() => t('申请人名称')),
            key: 'applyPersonName'
        },
        {
            title: computed(() => t('申请时间')),
            key: 'createTime'
        },
        {
            title: computed(() => t('审批状态')),
            key: 'approveStatus'
        },
        {
            title: computed(() => t('操作')),
            width: 260,
            fixed: 'right',
            render: (row) => {
                let btns = []
                btns = rtBtns(row)
                return h('div', btns);
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
            key: 'interfaceName',
            label: computed(() => t('接口名称')),
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
    showBorder: true
    // borderRadio: '4px'
});

//获取已接入接口列表
async function getDataList(){
    y9TableConfig.value.loading = true;
    query.value.page = y9TableConfig.value.pageConfig.currentPage;
    query.value.limit = y9TableConfig.value.pageConfig.pageSize;
    let res;
    res = await getApplyListById(
        query.value
    )
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
function search(){
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 10;
    y9TableConfig.value.tableData = []
    getDataList()
}
//重置按钮
function reset(){
    filterRef.value.elTableFilterRef.onReset()
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 10;
    y9TableConfig.value.tableData = []
    query.value = {}
    getDataList()
}
//初始化table数据
function initTableData(){
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 10;
    y9TableConfig.value.tableData = []
    getDataList()
}

//关闭配置信息
function closeDialog(type){
    openShow.value = false
}
//确定配置信息
function confirDialog(type){
    openShow.value = false
}
function openPubDialog(id,auth,name){
    query.value.id = id
    openShow.value = true
    isAuth.value = auth 
    interfaceName.value = name
    getDataList()
}
const view = (id)=>{
    let para = {
        id:id
    }
    getApplyInfoById(para).then((res)=>{
        interfaceApplyInfoRef.value.initFormData(res.data,isAuth.value)
    })
}
//申请详情按钮
function authApply(id, type, name,auth) {
    authApplyInfoRef.value.openPubDialog(id, type, interfaceName.value, isAuth.value);
}
//接口详情按钮
function interfaceView(id) {
    interfaceDialogRef.value.view(id, false)
}
//变更申请按钮
function alreadyApply(id, type, status,auth) {
    interfaceApplyRef.value.openPubDialog(id, type, status,isAuth.value);
}
//查看办理进度
function openFlowShow(id) {
    isFlowShow.value = true
    flowInterfaceId.value = id
    flowType.value = false
    applyType.value = true
}
defineExpose({openPubDialog})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
</style>
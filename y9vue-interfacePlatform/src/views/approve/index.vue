<template>
    <!-- 接口审批列表页面 -->
    <y9Table :config="y9TableConfig" :filterConfig="filterOperaConfig" @on-curr-page-change="handlerCurrPage"
        ref="filterRef" @on-page-size-change="handlerPageSize">
        <template v-slot:slotDate>
            <el-form-item :label="$t('审批发起时间')" :size="fontSizeObj.buttonSize">
                <el-date-picker v-model="selectedDate" :end-placeholder="$t('结束时间')" :range-separator="$t('至')"
                    :start-placeholder="$t('开始时间')" format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%; height: var(--el-input-height)" type="datetimerange"
                    value-format="YYYY-MM-DD HH:mm:ss" @change="selectdDate()"></el-date-picker>
            </el-form-item>
        </template>
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
        <template v-slot:slotBtns>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary">{{ $t('新增') }}
            </el-button>
        </template>
    </y9Table>

    <approve ref="approveRef" @getDataListParent="getDataList" />
    <interfaceDialog ref="interfaceDialogRef" @getDataListParent="getDataList" />
    <flowShow ref="flowShowRef" v-model:open-dialog="isFlowShow" v-model:type="flowType"
        v-model:interfaceId="flowInterfaceId" />
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, effect } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import {getApproveList} from '@/api/approve/approve'
import { ElMessage, ElMessageBox } from 'element-plus';
import approve from './approve.vue';
import interfaceDialog from '@/views/interface/interfaceDialog.vue';
import flowShow from '../interface/flowShow.vue';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const interfaceDialogRef = ref()
const flowShowRef = ref()
const isFlowShow = ref(false)
const flowType = ref(false)
const flowInterfaceId = ref()
const approveRef = ref()
const selectedDate = ref()
const props = defineProps({
    status:{
        type:String,
        default:()=>"其他"
    }
})

//表格配置
let y9TableConfig = ref({
    headerBackground: true,
    pageConfig: {
        background: false, //是否显示背景色
        currentPage: 1, //当前页数，支持 v-model 双向绑定
        pageSize: 5, //每页显示条目个数，支持 v-model 双向绑定
        total: 0, //总条目数
        pageSizeOpts:[5,10,15,20,30,40,1000]
        
    },
    columns: [
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
            fixed: 'left'
        },
        {
            title: computed(() => t('接口名称')),
            key: 'interfaceName'
        },
        {
            title: computed(() => t('接口版本')),
            key: 'version'
        },
        {
            title: computed(() => t('接口状态')),
            key: 'interfaceStatus'
        },
        {
            title: computed(() => t('审批状态')),
            key: 'approveStatus'
        },
        {
            title: computed(() => t('申请类型')),
            key: 'applyType',
            render:(row)=>{
                if(row.applyType==0){
                    return h('div', "调用");
                }else if(row.applyType==1){
                    return h('div', "停用");
                }else if(row.applyType==2){
                    return h('div', "发布");
                }else{
                    return h('div', "-");
                }
            }
        },
        {
            title: computed(() => t('申请人')),
            key: 'applyPersonName'
        },
        {
            title: computed(() => t('审批发起时间')),
            key: 'applyTime',
            sortable: true
        },
        {
            title: computed(() => t('操作')),
            width: 200,
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
//权限按钮控制
function rtBtns(row){
    let btns = []
    let viewBtn = h('span', {class: 'operate', onClick: () => { view(row.interfaceId,row.applyType) } }, t("接口详情"));
    let editBtn = h('span', { class: 'leftMargin operate', onClick: () => { edit(row.approveId,row.applyType,row.approveInterfaceStatus,row.isLimitData) } }, t('审批'));
    let flowInfoBtn = h('span', { class: 'leftMargin operate', onClick: () => { openFlowShow(row.approveId) } }, t('审批详情'));
    let flowBtn = h('span', { class: 'leftMargin operate', onClick: () => { openFlowShow(row.approveId) } }, t('审批进度'));
    if(row.isNow=="Y"){
        btns.push(viewBtn)
        btns.push(editBtn)
    }else{
        btns.push(viewBtn)
        btns.push(flowInfoBtn)
    }
    return btns;
}
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
            span: settingStore.device === 'mobile' ? 24 : 4
        },
        {
            type: 'select',
            value: '',
            key: 'applyType',
            label: computed(() => t('申请类型')),
            labelWidth: '82px',
            span: settingStore.device === 'mobile' ? 24 : 4,
            props:{
                options:[
                    {
                        label:"调用",
                        value:"0"
                    },
                    {
                        label:"停用",
                        value:"1"
                    },
                    {
                        label:"发布",
                        value:"2"
                    }
                ]
            }
        },
        {
            type: 'select',
            value: '',
            key: 'approveStatus',
            label: computed(() => t('审批状态')),
            labelWidth: '82px',
            span: settingStore.device === 'mobile' ? 24 : 4,
            props:{
                options:[
                    {
                        label:"未审批",
                        value:"-1"
                    },
                    {
                        label:"已审批",
                        value:"-2"
                    }
                ]
            }
        },
        {
            type:'slot',
            slotName:'slotDate',
            span:7
        },
        {
            type: 'slot',
            slotName: 'slotSearch',
            span: 4
        }

    ],
    showBorder: true,
    borderRadio: '4px'
});

//获取已接入接口列表
async function getDataList(){
    delete query.value.interfaceStatus;
    y9TableConfig.value.loading = true;
    query.value.page = y9TableConfig.value.pageConfig.currentPage;
    query.value.limit = y9TableConfig.value.pageConfig.pageSize;
    let res;
    res = await getApproveList(
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
    y9TableConfig.value.pageConfig.pageSize = 15;
    y9TableConfig.value.tableData = []
    getDataList()
}
//重置按钮
function reset(){
    filterRef.value.elTableFilterRef.onReset()
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 15;
    y9TableConfig.value.tableData = []
    query.value = {}
    selectedDate.value = []
    getDataList()
}
//初始化table数据
function initTableData(){
    y9TableConfig.value.pageConfig.currentPage = 1;
    y9TableConfig.value.pageConfig.pageSize = 15;
    y9TableConfig.value.tableData = []
    getDataList()
}
initTableData()

//校验手机号
const validatePhone = (rule: any, value: any, callback: any) => {
    let result = $validCheck('phone', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};


//查看办理进度
function openFlowShow(id){
    isFlowShow.value = true
    flowInterfaceId.value = id
    flowType.value = false
}


//编辑按钮
async function edit(id,applyType,interfaceStatus,isLimitData) {
    approveRef.value.openApproveDialog(id,applyType,interfaceStatus,isLimitData)
}
//删除按钮方法
const delSpanById = (id) =>{
    ElMessageBox.confirm(
        '是否确认删除这条数据',
        '删除数据确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
            draggable: true
        }
    ).then(()=>{
        delData(id)
    }).catch(()=>{
        console.log("爆粗")
    })
}
//删除数据
async function delData(id) {
    let para = {
        id: id
    }
    let res = ""
    if (res.status == "success") {
        ElMessage({ type: 'info', message: '删除成功' })
    } else {
        ElMessage({ type: 'warning', message: res.msg })
    }
    getDataList()
}

//详情按钮
async function view(id) {
    interfaceDialogRef.value.approveView(id)
}
//选择时间
const selectdDate = ()=>{
    query.value.startDate = selectedDate.value[0]
    query.value.endDate = selectedDate.value[1]
}
</script>
<style scoped>
.el-form-item--default{
    margin-bottom: 0px!important;
}
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
</style>
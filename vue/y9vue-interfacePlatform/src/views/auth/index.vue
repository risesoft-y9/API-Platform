<template>
    <!-- 权限配置管理页面 -->
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
        <template v-slot:slotBtns>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="addDialog">
                <i class="ri-add-line"></i>
                <span>{{ $t('新增') }}</span>
            </el-button>
        </template>
    </y9Table>
    <!-- 增加参数页面 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #isLimit>
                    <el-switch v-model="value3" active-text="是" inactive-text="否"></el-switch><el-tooltip effect="light"
                        content="点击配置限流信息" placement="top-start"><el-icon class="operate" @click="openView('1')">
                            <Warning></Warning>
                        </el-icon></el-tooltip>
                </template>
                <template #isAuth>
                    <el-switch v-model="value3" active-text="是" inactive-text="否"></el-switch><el-tooltip effect="light"
                        content="点击配置鉴权信息" placement="top-start"><el-icon class="operate" @click="openView('2')">
                            <Warning></Warning>
                        </el-icon></el-tooltip>
                </template>
            </y9Form>
        </template>
    </y9Dialog>
    <!-- 配置权限值弹窗 -->
    <el-dialog @closed="closeDialog" @opened="dialoogOpened" v-model="limitInfo" :title="limitTitle">
        <el-divider />
        <dictVal v-model:isView="isView" ref="dictValRef" />
        <template #footer>
            <el-button class="el-button el-button--default global-btn-third"
                @click="closeDialog()">关闭</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getAuthKeyList, saveAuthInfo, getAuthInfoById, delAuthInfoById } from '@/api/authInterface/authInterface'
import { ElMessage, ElMessageBox } from 'element-plus';
import dictVal from './dictVal.vue';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const selectedDate = ref();
const query: any = ref({});
const filterRef = ref();
const y9UserInfo = JSON.parse(sessionStorage.getItem('ssoUserInfo'));

const limitInfo = ref(false)
const limitTitle = ref("配置权限值")
const value3 = ref()
const parameterId = ref()
const isPrimary = ref()
const parameterName = ref()
const fieldName = ref()
const isTree = ref()
const isView = ref(true)
const parameterType = ref()
const dictValRef = ref()

//表格配置
let y9TableConfig = ref({
    headerBackground: true,
    pageConfig: {
        background: false, //是否显示背景色
        currentPage: 1, //当前页数，支持 v-model 双向绑定
        pageSize: 5, //每页显示条目个数，支持 v-model 双向绑定
        total: 0, //总条目数
        pageSizeOpts: [5, 10, 15, 20, 30, 40, 1000]
    },
    columns: [
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
            fixed: 'left'
        },
        {
            title: computed(() => t('参数名称')),
            key: 'parameterName'
        },
        {
            title: computed(() => t('参数key')),
            key: 'fieldName'
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType'
        },
        {
            title: computed(() => t('排序')),
            key: 'sort'
        },
        {
            title: computed(() => t('操作')),
            width: 300,
            fixed: 'right',
            render: (row) => {
                if(row.parameterType=="公有" && y9UserInfo.managerLevel!==1){
                    return h('div', [h('span', { onClick: () => { view(row.id) } }, t("详情")),
                h('span', { class: 'leftMargin', onClick: () => { openDictValView(row.parameterId, 'N', row.parameterName, row.fieldName, row.isTree, true) } }, t('查看权限值')),
                ]);
                }else{
                    return h('div', [h('span', { onClick: () => { view(row.id) } }, t("详情")),
                h('span', { class: 'leftMargin', onClick: () => { edit(row.id) } }, t('编辑')),
                h('span', { class: 'leftMargin', onClick: () => { delData(row.id) } }, t('删除')),
                h('span', { class: 'leftMargin', onClick: () => { openDictValView(row.parameterId, 'N', row.parameterName, row.fieldName, row.isTree, false) } }, t('配置权限值')),
                ]);
                }

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
            key: 'parameterName',
            label: computed(() => t('参数名称')),
            labelWidth: '82px',
            span: settingStore.device === 'mobile' ? 24 : 6
        },
        {
            type: 'select',
            value: '',
            key: 'parameterType',
            label: computed(() => t('参数类型')),
            labelWidth: '82px',
            span: settingStore.device === 'mobile' ? 24 : 6,
            props: {
                options: [
                    {
                        label: "公有",
                        value: "公有"
                    },
                    {
                        label: "私有",
                        value: "私有"
                    }
                ]
            }
        },
        {
            type: 'slot',
            slotName: 'slotSearch',
            span: 6
        },
        {
            type: 'slot',
            slotName: 'slotBtns',
            span: settingStore.device === 'mobile' ? 24 : 6,
            justify: 'flex-end'
        }

    ],
    showBorder: true,
    borderRadio: '4px'
});

//获取已接入接口列表
async function getDataList() {
    y9TableConfig.value.loading = true;
    query.value.page = y9TableConfig.value.pageConfig.currentPage;
    query.value.limit = y9TableConfig.value.pageConfig.pageSize;
    let res = await getAuthKeyList(
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
                    let res = await saveAuthInfo(
                        formData
                    )
                    if (res.code == 0) {
                        ElMessage({
                            message: '数据保存成功',
                            type: 'success'
                        })
                        resolve();
                        getDataList();
                    }
                    reject();
                } else {
                    reject();
                }
            });
        });
    }
});

// 接口表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        isPrimary: 'Y',
        parameterType: '公有'
    },
    rules: {
        //	表单验证规则。类型：FormRules
        parameterName: [{ required: true, message: computed(() => t('名称不能为空')), trigger: 'blur' }],
        fieldName: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        isTree: [{ required: true, message: computed(() => t('是否树形不能为空')), trigger: 'blur' }],
        sort: [{ required: true, message: computed(() => t('排序不能为空')), trigger: 'blur' }
            , { validator: validateNumber, trigger: 'blur' }
        ],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t('参数名称')),
            prop: 'parameterName'
        },
        {
            type: 'input',
            label: computed(() => t('参数key')),
            prop: 'fieldName'
        },
        {
            type: 'select',
            label: computed(() => t('是否树形')),
            prop: 'isTree',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('是')), value: '是' },
                    { label: computed(() => t('否')), value: '否' }
                ]
            }
        },
        {
            type: 'input',
            label: computed(() => t('排序')),
            prop: 'sort',
            props: {
                type: "number",
                max: 999,
                min: 0
            }
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
function addDialog() {
    ruleFormConfig.value.model = { isPrimary: 'Y', parameterType: '公有' }
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
    addDialogConfig.value.title = computed(() => t('新增权限信息'))
    addDialogConfig.value.show = true
}
//打开配置值页面
function openDictValView(id, primary, name1, name2, tree, view) {
    parameterId.value = id
    isPrimary.value = primary
    parameterName.value = name1
    fieldName.value = name2
    isTree.value = tree
    parameterType.value = "公有"
    limitInfo.value = true
    isView.value = view
}
//弹窗打开后调用方法
function dialoogOpened() {
    dictValRef.value.initTableData(parameterId.value, isPrimary.value, parameterName.value, fieldName.value, isTree.value, parameterType.value)
}
//编辑
async function edit(id) {
    let para = {
        id: id
    }
    let res = await getAuthInfoById(para)
    ruleFormConfig.value.model = res.data
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props == undefined) {
            it.props = {
                disabled: false
            }
        } else {
            it.props.disabled = false
        }
    }
    addDialogConfig.value.title = computed(() => t('编辑权限信息'))
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.show = true
}
//详情
async function view(id) {
    let para = {
        id: id
    }
    let res = await getAuthInfoById(para)
    ruleFormConfig.value.model = res.data
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
    addDialogConfig.value.title = computed(() => t('查看权限信息'))
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
        delAuthInfoById(para).then((res) => {
            if (res.status == "success") {
                ElMessage({ type: 'info', message: '删除成功' })
                getDataList()
            } else {
                ElMessage({ type: 'warning', message: "删除失败" + res.msg })
            }
        })
    }).catch(() => {
    })
}
function closeDialog() {
    limitInfo.value = false
    dictValRef.value.resetTable()
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
<template>
    <!-- 接口注册时使用的列表页面 -->
    <y9Table v-model:selectedVal="selectedCheckboxVal" :filterConfig="filterOperaConfig" :config="y9TableConfig"
        ref="filterRef" v-if="props.isShow">
        <template v-slot:slotSearch>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="search">{{ $t('查询') }}
            </el-button>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="el-button el-button--default global-btn-third" @click="reset">{{ $t('重置') }}
            </el-button>
        </template>
        <template v-slot:slotBtns>
            <el-button v-if="props.isView" :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                @click="addDialog">{{ $t('新增') }}
            </el-button>
        </template>
    </y9Table>
    <!-- 增加参数页面 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
            </y9Form>
        </template>
    </y9Dialog>
    <!-- 配置权限值弹窗 -->
    <el-dialog @closed="closeDialog" @opened="dialoogOpened" v-model="limitInfo" :title="limitTitle">
        <el-divider />
        <dictVal ref="dictValRef" v-model:isView="isView" />
        <template #footer>
            <el-button class="el-button el-button--default global-btn-third"
                @click="confirmData()">关闭</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getAuthKeyList, saveAuthInfo, getAuthInfoById, delAuthInfoById,getAuthKeyView } from '@/api/authInterface/authInterface'
import { ElMessage, ElMessageBox } from 'element-plus';
import dictVal from './interfaceDictVal.vue';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const limitInfo = ref(false)
const limitTitle = ref("查看权限值")
const value3 = ref()
const parameterId = ref()
const isPrimary = ref()
const parameterName = ref()
const fieldName = ref()
const isTree = ref()
const parameterType = ref()
const dictValRef = ref()
const isView = ref(false)
const selectedCheckboxVal = ref()

const props = defineProps({
    isView: {
        type: Boolean,
        default: () => false
    },
    isDisabled: {
        type: Boolean,
        default: () => false
    },
    selectData: {
        type: String
    },
    isShow: {
        type: Boolean,
        default: () => true
    }
})
//表格配置
let y9TableConfig = ref({
    headerBackground: true,
    pageConfig: false,
    height: 200,
    columns: [
        {
            type: "selection",
            width: 60,
            selectable: () => {
                if (props.isView && props.isDisabled) {
                    return true;
                } else {
                    return false;
                }
            }
        },
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
                if (row.parameterType == '私有' && props.isView) {
                    return h('div', [h('span', { onClick: () => { view(row.id) } }, t("详情")),
                    h('span', { class: 'leftMargin', onClick: () => { edit(row.id) } }, t('编辑')),
                    h('span', { class: 'leftMargin', onClick: () => { delData(row.id) } }, t('删除')),
                    h('span', { class: 'leftMargin', onClick: () => { openEditDictValView(row.parameterId, 'N', row.parameterName, row.fieldName, row.isTree) } }, t('配置权限值')),
                    ]);
                } else {
                    return h('div', [h('span', { onClick: () => { view(row.id) } }, t("详情")),
                    h('span', { class: 'leftMargin', onClick: () => { openDictValView(row.parameterId, 'N', row.parameterName, row.fieldName, row.isTree) } }, t('查看权限值'))
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
        // {
        //     type: 'input',
        //     value: '',
        //     key: 'parameterName',
        //     label: computed(() => t('参数名称')),
        //     labelWidth: '82px',
        //     span: settingStore.device === 'mobile' ? 24 : 6
        // },
        // {
        //     type: 'slot',
        //     slotName: 'slotSearch',
        //     span: 6
        // },
        {
            type: 'slot',
            slotName: 'slotBtns',
            span: settingStore.device === 'mobile' ? 24 : 24,
            justify: 'flex-end'
        }

    ],
    showBorder: true
    // borderRadio: '4px'
});

//获取已接入接口列表
async function getDataList() {
    y9TableConfig.value.loading = true;
    query.value.page = 1;
    query.value.limit = 999999;
    let res;
    console.log(props.isView)
    if(!props.isView){
        let param = {
            checkedIds : (props.selectData==null||props.selectData==""||props.selectData==undefined) ? "-1":props.selectData,
            page:1,
            limit:999999
        }
        res = await getAuthKeyView(param)
    }else{
        res = await getAuthKeyList(
        query.value
        )
    }
    y9TableConfig.value.tableData = res.data || []
    y9TableConfig.value.loading = false;
    let index = 0
    for (let it of y9TableConfig.value.tableData) {
        if (props.selectData.indexOf(it.id) != -1) {
            console.log(it)
            filterRef.value.elTableRef.toggleRowSelection(it.id, true)
            index++;
        }
    }
}
//查询按钮
function search() {
    y9TableConfig.value.tableData = []
    getDataList()
}
//重置按钮
function reset() {
    filterRef.value.elTableFilterRef.onReset()
    y9TableConfig.value.tableData = []
    query.value.fieldName = ""
    query.value.parameterName = ""
    getDataList()
}
//初始化table数据
function initTableData() {
    y9TableConfig.value.tableData = []
    getDataList()
}
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
        parameterType: '私有',
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
    ruleFormConfig.value.model = { isPrimary: 'Y', parameterType: '私有' }
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
function openDictValView(id, primary, name1, name2, tree) {
    parameterId.value = id
    isPrimary.value = primary
    parameterName.value = name1
    fieldName.value = name2
    isTree.value = tree
    parameterType.value = "私有"
    isView.value = false
    limitInfo.value = true
}

//打开配置值页面
function openEditDictValView(id, primary, name1, name2, tree) {
    parameterId.value = id
    isPrimary.value = primary
    parameterName.value = name1
    fieldName.value = name2
    isTree.value = tree
    parameterType.value = "私有"
    isView.value = true
    limitInfo.value = true
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
            id: id,
            type: "interface"
        }
        delAuthInfoById(para).then((res) => {
            if (res.status == "success") {
                ElMessage({ type: 'info', message: '删除成功' })
            } else {
                ElMessage({ type: 'warning', message: "删除失败" + res.msg })
            }
        })
    }).catch(() => {

    })
}
function closeDialog() {
    dictValRef.value.resetTable()
    limitInfo.value = false
}
function confirmData() {
    limitInfo.value = false
}
function getCheckData() {
    console.log(selectedCheckboxVal.value)
    let selectData = ""
    if (selectedCheckboxVal.value != undefined) {
        for (let it of selectedCheckboxVal.value) {
            selectData += it.parameterId + ","
        }
        if (selectData.length != 0) {
            selectData = selectData.slice(0, selectData.length - 1)
        }
    }
    return selectData;
}
function getCheckDataKeys() {
    let selectData = []
    if (selectedCheckboxVal.value != undefined) {
        for (let it of selectedCheckboxVal.value) {
            selectData.push(it.fieldName)
        }
    }
    return selectData;
}
const restTable = () => {
    y9TableConfig.value.tableData = []
}

defineExpose({ initTableData, getCheckData, restTable, getCheckDataKeys })
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
</style>
<template>
    <!-- 数据权限值配置列表页面 -->
    <y9Table :config="y9TableConfig" :filterConfig="filterOperaConfig" @on-curr-page-change="handlerCurrPage"
        ref="filterRef" @on-page-size-change="handlerPageSize">
        <template v-slot:slotSearch>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="search">{{ $t('查询') }}
            </el-button>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="el-button el-button--default global-btn-third" @click="reset">{{ $t('重置') }}
            </el-button>
        </template>
        <template v-slot:slotBtns>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="addDialog">{{ $t('新增') }}
            </el-button>
        </template>
    </y9Table>
    <!-- 增加权限值信息 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
            </y9Form>
        </template>
    </y9Dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, defineProps } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getAuthValList, saveAuthInfo, getAuthInfoById, getPidList, delAuthInfoById } from '@/api/authInterface/authInterface'
import { ElMessage, ElMessageBox } from 'element-plus';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const selectedDate = ref();
const query: any = ref({});
const filterRef = ref();
const fieldName = ref()
const parameterName = ref()
const isPrimary = ref()
const parameterId = ref()
const isTree = ref()
const parameterType = ref()
//懒加载方法
const load = (row, treeNode, resolve) => {
    query.value.parameterId = parameterId.value;
    query.value.isPrimary = isPrimary.value;
    query.value.pid = row.id
    getAuthValList(
        query.value
    ).then((res) => {
        resolve(res.data)
    })
}
//表格配置
let y9TableConfig = ref({
    rowKey: "id",
    load: load,
    lazy: true,
    headerBackground: true,
    pageConfig: {
        background: false, //是否显示背景色
        currentPage: 1, //当前页数，支持 v-model 双向绑定
        pageSize: 5, //每页显示条目个数，支持 v-model 双向绑定
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
            title: computed(() => t('显示值')),
            key: 'showVal'
        },
        {
            title: computed(() => t('字段值')),
            key: 'fieldVal'
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
            title: computed(() => t('排序')),
            key: 'sort'
        },
        {
            title: computed(() => t('操作')),
            width: 300,
            fixed: 'right',
            render: (row) => {
                return h('div', [h('span', { onClick: () => { view(row.id) } }, t("详情")),
                h('span', { class: 'leftMargin', onClick: () => { edit(row.id) } }, t('编辑')),
                h('span', { class: 'leftMargin', onClick: () => { delData(row.id) } }, t('删除')),
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
            key: 'fieldVal',
            label: computed(() => t('字段值')),
            labelWidth: '42px',
            span: settingStore.device === 'mobile' ? 24 : 6
        },
        {
            type: 'input',
            value: '',
            key: 'showVal',
            label: computed(() => t('显示值')),
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
    query.value.parameterId = parameterId.value;
    query.value.isPrimary = isPrimary.value;
    if (isTree.value == "是" && query.value.pid == undefined) {
        delete y9TableConfig.value.pageConfig;
        y9TableConfig.value.pageConfig = false
        query.value.page = 1;
        query.value.limit = 9999999;
        query.value.pid = "0"
    } else if (isTree.value == "否") {
        query.value.page = y9TableConfig.value.pageConfig.currentPage;
        query.value.limit = y9TableConfig.value.pageConfig.pageSize;
        delete query.value.pid;
    }
    let res = await getAuthValList(
        query.value
    )
    y9TableConfig.value.tableData = res.data || []
    if (isTree.value == "否") {
        y9TableConfig.value.pageConfig.total = res.count || 0;
    }
    y9TableConfig.value.loading = false;
}

//获取加载数据
function loadData(pid) {
    query.value.parameterId = parameterId.value;
    query.value.isPrimary = isPrimary.value;
    query.value.pid = pid
    let res = getAuthValList(
        query.value
    )
    return res;
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
    if (y9TableConfig.value.pageConfig) {
        y9TableConfig.value.pageConfig.currentPage = 1;
        y9TableConfig.value.pageConfig.pageSize = 5;
    } else {
        query.value.page = 1;
        query.value.limit = 9999999;
    }
    if (!((query.value.fieldVal == null || query.value.fieldVal == "") && (query.value.showVal == null || query.value.showVal == ""))) {
        query.value.pid = "-1"
    }
    y9TableConfig.value.tableData = []
    getDataList()
}
//重置按钮
function reset() {
    filterRef.value.elTableFilterRef.onReset()
    if (y9TableConfig.value.pageConfig) {
        y9TableConfig.value.pageConfig.currentPage = 1;
        y9TableConfig.value.pageConfig.pageSize = 5;
    }
    y9TableConfig.value.tableData = []
    query.value.fieldVal = ""
    query.value.showVal = ""
    query.value.pid = "0"
    getDataList()
}
//初始化table数据
function initTableData(id, primary, name1, name2, tree, type) {
    if (tree == "否") {
        delete y9TableConfig.value.pageConfig;
        y9TableConfig.value.pageConfig = {
            background: false, //是否显示背景色
            currentPage: 1, //当前页数，支持 v-model 双向绑定
            pageSize: 5, //每页显示条目个数，支持 v-model 双向绑定
            total: 0, //总条目数
        }
        y9TableConfig.value.pageConfig.currentPage = 1;
        y9TableConfig.value.pageConfig.pageSize = 5;
        y9TableConfig.value.tableData = []
    }
    parameterId.value = id
    isPrimary.value = primary
    parameterName.value = name1
    fieldName.value = name2
    isTree.value = tree
    parameterType.value = type
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
    title: computed(() => t('新增权限值')),
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
                        reset()
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
        parameterName: parameterName.value,
        fieldName: fieldName.value,
        parameterId: parameterId.value,
        isPrimary: isPrimary.value,
        isTree: isTree.value,
        parameterType: parameterType.value
    },
    rules: {
        //	表单验证规则。类型：FormRules
        parameterName: [{ required: true, message: computed(() => t('名称不能为空')), trigger: 'blur' }],
        fieldName: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        isTree: [{ required: true, message: computed(() => t('是否树形不能为空')), trigger: 'blur' }],
        sort: [{ required: true, message: computed(() => t('排序不能为空')), trigger: 'blur' }
            , { validator: validateNumber, trigger: 'blur' }
        ],
        fieldVal: [{ required: true, message: computed(() => t('字段值不能为空')), trigger: 'blur' }],
        showVal: [{ required: true, message: computed(() => t('显示值不能为空')), trigger: 'blur' }],
        pid: [{ required: true, message: computed(() => t('父级不能为空')), trigger: 'blur' }],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t('显示值')),
            prop: 'showVal'
        },
        {
            type: 'input',
            label: computed(() => t('字段值')),
            prop: 'fieldVal'
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
        },
        {
            type: 'input',
            label: computed(() => t('参数名称')),
            prop: 'parameterName',
            props: {
                disabled: true
            }
        },
        {
            type: 'input',
            label: computed(() => t('参数key')),
            prop: 'fieldName',
            props: {
                disabled: true
            }
        },
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
function getPid(id, type) {
    if (isTree.value == "是") {
        let item = {
            type: 'select',
            label: computed(() => t('选择父级')),
            prop: 'pid',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('本级')), value: '0' },
                ]
            }
        }
        let pidQuery = {
            parameterId: parameterId.value,
            limit: 100000,
            page: 1
        }
        getAuthValList(pidQuery).then((pidData) => {
            for (let it of pidData.data) {
                if (it.id != ruleFormConfig.value.model.id) {
                    let selectItem = { label: computed(() => t(it.showVal)), value: it.id }
                    item.props.options.push(selectItem)
                }
            }
            if (ruleFormConfig.value.itemList[0].prop == 'pid') {
                ruleFormConfig.value.itemList[0].props.options = item.props.options
            } else {
                ruleFormConfig.value.itemList.unshift(item)
            }
            if (id == "") {
                for (let it of ruleFormConfig.value.itemList) {
                    if (it.prop != "parameterName" && it.prop != "fieldName") {
                        if (it.props == undefined) {
                            it.props = {
                                disabled: false
                            }
                        } else {
                            it.props.disabled = false
                        }
                    }
                }
            } else {
                let para = {
                    id: id
                }
                getAuthInfoById(para).then((res) => {
                    ruleFormConfig.value.model = res.data
                    if (type) {
                        for (let it of ruleFormConfig.value.itemList) {
                            if (it.props == undefined) {
                                it.props = {
                                    disabled: true
                                }
                            } else {
                                it.props.disabled = true
                            }
                        }
                    } else {
                        for (let it of ruleFormConfig.value.itemList) {
                            if (it.prop != "parameterName" && it.prop != "fieldName") {
                                if (it.props == undefined) {
                                    it.props = {
                                        disabled: false
                                    }
                                } else {
                                    it.props.disabled = false
                                }
                            }
                        }
                    }

                })
            }
        })
    } else {
        if (ruleFormConfig.value.itemList[0].prop == "pid") {
            ruleFormConfig.value.itemList.splice(0, 1)
        }
    }
}

function addDialog() {
    addDialogConfig.value.show = true
    ruleFormConfig.value.model = {}
    ruleFormConfig.value.model.parameterName = parameterName.value;
    ruleFormConfig.value.model.fieldName = fieldName.value;
    ruleFormConfig.value.model.parameterId = parameterId.value;
    ruleFormConfig.value.model.isPrimary = isPrimary.value;
    ruleFormConfig.value.model.isTree = isTree.value;
    ruleFormConfig.value.model.parameterType = parameterType.value
    for (let it of ruleFormConfig.value.itemList) {
        if (it.prop != "parameterName" && it.prop != "fieldName") {
            if (it.props == undefined) {
                it.props = {
                    disabled: false
                }
            } else {
                it.props.disabled = false
            }
        }
    }
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('新增权限信息'))
    getPid("", false)
}

//编辑
async function edit(id) {
    ruleFormConfig.value.model.id = id
    getPid(id, false)
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('编辑权限信息'))
    addDialogConfig.value.show = true
}
//详情
async function view(id) {
    ruleFormConfig.value.model.id = id
    getPid(id, true)
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
        ElMessage({ type: 'info', message: '删除失败' })
    })
}
const resetTable = () => {
    query.value.pid = undefined
    y9TableConfig.value.tableData = []
}
defineExpose({
    initTableData, resetTable
})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
</style>
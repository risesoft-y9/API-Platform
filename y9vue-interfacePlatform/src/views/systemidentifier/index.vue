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
                class="global-btn-main" type="primary" @click="addDialog('0')">
                <i class="ri-add-line"></i>
                <span>{{ $t('录入单位') }}</span>
            </el-button>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="addDialog('1')">
                <i class="ri-add-line"></i>
                <span>{{ $t('录入系统') }}</span>
            </el-button>
        </template>
    </y9Table>
    <!-- 增加单位信息 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
            </y9Form>
        </template>
    </y9Dialog>

    <!-- 增加系统信息 -->
    <y9Dialog v-model:config="addDialogConfigSystem">
        <template v-slot>
            <y9Form ref="ruleFormSystemRef" :config="ruleFormConfigSystem">
            </y9Form>
        </template>
    </y9Dialog>

</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { saveAuthInfo, getAuthInfoById, delAuthInfoById } from '@/api/authInterface/authInterface'
import { getPage, getListByType, saveInfo, getInfoById, delInfoById } from '@/api/systemidentifier/systemidentifier'
import { ElMessage, ElMessageBox } from 'element-plus';
import dictVal from './dictVal.vue';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const selectedDate = ref();
const query: any = ref({});
const filterRef = ref();

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
const deptList = ref([])

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
            title: computed(() => t('名称')),
            key: 'name'
        },
        {
            title: computed(() => t('类型')),
            key: 'parameterType',
            render: (row) => {
                if (row.parameterType == "0") {
                    return h('span', t("单位"));
                } else {
                    return h('span', t("系统"));
                }
            }
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
                return h('div', [h('span', { onClick: () => { view(row.id, row.parameterType) } }, t("详情")),
                h('span', { class: 'leftMargin', onClick: () => { edit(row.id, row.parameterType) } }, t('编辑')),
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
            key: 'name',
            label: computed(() => t('名称')),
            labelWidth: '82px',
            span: settingStore.device === 'mobile' ? 24 : 6
        },
        {
            type: 'select',
            value: '',
            key: 'parameterType',
            label: computed(() => t('数据类型')),
            labelWidth: '82px',
            span: settingStore.device === 'mobile' ? 24 : 6,
            props: {
                options: [
                    {
                        label: "单位",
                        value: "0"
                    },
                    {
                        label: "系统",
                        value: "1"
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
    let res = await getPage(
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
const ruleFormSystemRef = ref();
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
//增加
let addDialogConfigSystem = ref({
    show: false,
    title: computed(() => t('新增系统信息')),
    showFooter: true,
    onOkLoading: true,
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            const y9RuleFormInstance = ruleFormSystemRef.value?.elFormRef;
            await y9RuleFormInstance.validate(async (valid) => {
                if (valid) {
                    let data = ruleFormSystemRef.value.model;
                    for (let it of deptList.value) {
                        if (data.pid == it.value) {
                            data.pname = it.label
                        }
                    }
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
                        getDataList();
                    }
                    reject();
                } else {
                    reject();
                }
            });
        });
    },
    visibleChange: (visible) => {
        if (visible) {

        } else {
            deptList.value = []
        }
    }
});
const itemName = ref("单位名称")
// 接口表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        isPrimary: 'Y',
        parameterType: '0'
    },
    rules: {
        //	表单验证规则。类型：FormRules
        name: [{ required: true, message: computed(() => t(itemName.value+'不能为空')), trigger: 'blur' }],
        fieldName: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        isTree: [{ required: true, message: computed(() => t('是否树形不能为空')), trigger: 'blur' }],
        sort: [{ required: true, message: computed(() => t('排序不能为空')), trigger: 'blur' }
            , { validator: validateNumber, trigger: 'blur' }
        ],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t(itemName.value)),
            prop: 'name'
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
//系统录入
let ruleFormConfigSystem = ref({
    //表单配置
    model: {
        parameterType: '1'
    },
    rules: {
        //	表单验证规则。类型：FormRules
        name: [{ required: true, message: computed(() => t(itemName.value+'不能为空')), trigger: 'blur' }],
        pid: [{ required: true, message: computed(() => t('单位名称不能为空')), trigger: 'blur' }],
        isTree: [{ required: true, message: computed(() => t('是否树形不能为空')), trigger: 'blur' }],
        sort: [{ required: true, message: computed(() => t('排序不能为空')), trigger: 'blur' }
            , { validator: validateNumber, trigger: 'blur' }
        ],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t(itemName.value)),
            prop: 'name'
        },
        {
            type: 'select',
            prop: 'pid',
            label: computed(() => t('单位名称')),
            props: {
                options: deptList.value
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
function addDialog(type) {
    if (type == '0') {
        addDialogConfig.value.title = computed(() => t('新增单位信息'))
        itemName.value = "单位名称"
        ruleFormConfig.value.model = { parameterType: type }
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
        addDialogConfig.value.show = true
    } else {
        // deptList.value = []
        addDialogConfigSystem.value.title = computed(() => t('新增系统信息'))
        let param = {
            parameterType: "0"
        }
        getListByType(param).then((res) => {
            // debugger
            for (let it of res.data) {
                let item = {
                    label: it.name,
                    value: it.id
                }
                deptList.value.push(item)
            }
            itemName.value = "系统名称"
            addDialogConfigSystem.value.model = { parameterType: type }
            for (let it of ruleFormConfigSystem.value.itemList) {
                if (it.props == undefined) {
                    it.props = {
                        disabled: false
                    }
                } else {
                    it.props.disabled = false
                }
                if(it.prop == 'pid'){
                    it.props.options = deptList.value 
                }
            }
            addDialogConfigSystem.value.okText = "保存"
            addDialogConfigSystem.value.show = true
        })
    }
}
//打开配置值页面
function openDictValView(id, primary, name1, name2, tree) {
    parameterId.value = id
    isPrimary.value = primary
    parameterName.value = name1
    fieldName.value = name2
    isTree.value = tree
    parameterType.value = "公有"
    limitInfo.value = true
}
//弹窗打开后调用方法
function dialoogOpened() {
    dictValRef.value.initTableData(parameterId.value, isPrimary.value, parameterName.value, fieldName.value, isTree.value, parameterType.value)
}
//编辑
async function edit(id,type) {
    let para = {
        id: id
    }
    let res = await getInfoById(para)
    if (type == "0") {
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
        itemName.value = "单位名称"
        addDialogConfig.value.title = computed(() => t('编辑单位信息'))
        addDialogConfig.value.okText = "保存"
        addDialogConfig.value.show = true
    }else{
        ruleFormConfigSystem.value.model = res.data
        for (let it of ruleFormConfigSystem.value.itemList) {
            if (it.props == undefined) {
                it.props = {
                    disabled: false
                }
            } else {
                it.props.disabled = false
            }
        }
        let param = {
            parameterType: "0"
        }
        getListByType(param).then((res) => {
            // debugger
            for (let it of res.data) {
                let item = {
                    label: it.name,
                    value: it.id
                }
                deptList.value.push(item)
            }
            itemName.value = "系统名称"
            addDialogConfigSystem.value.title = computed(() => t('编辑系统信息'))
            addDialogConfigSystem.value.okText = "保存"
            addDialogConfigSystem.value.show = true
        })

    }

}
//详情
async function view(id, type) {
    let para = {
        id: id
    }
    let res = await getInfoById(para)
    if (type == "0") {
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
        itemName.value = "单位名称"
        addDialogConfig.value.okText = false
        addDialogConfig.value.title = computed(() => t('查看单位信息'))
        addDialogConfig.value.show = true
    } else {
        ruleFormConfigSystem.value.model = res.data
        for (let it of ruleFormConfigSystem.value.itemList) {
            if (it.props == undefined) {
                it.props = {
                    disabled: true
                }
            } else {
                it.props.disabled = true
            }
        }
        let param = {
            parameterType: "0"
        }
        getListByType(param).then((res) => {
            // debugger
            for (let it of res.data) {
                let item = {
                    label: it.name,
                    value: it.id
                }
                deptList.value.push(item)
            }
            itemName.value = "系统名称"
            addDialogConfigSystem.value.okText = false
            addDialogConfigSystem.value.title = computed(() => t('查看系统信息'))
            addDialogConfigSystem.value.show = true
        })
    }
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
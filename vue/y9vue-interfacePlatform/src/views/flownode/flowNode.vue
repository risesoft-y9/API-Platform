<template>
    <!-- 流程节点列表 -->
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
                class="global-btn-main" type="primary" @click="changeTableSort">{{ $t('节点顺序批量变更') }}
            </el-button>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="addDialog">{{ $t('新增') }}
            </el-button>
        </template>
    </y9Table>
    <!-- 增加流程节点信息 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #selectuser>
                    <el-input v-model="userData" @click="openSelectUser()" readonly v-if="isShow"></el-input>
                    <el-input v-model="userData" disabled v-if="!isShow"></el-input>
                </template>
            </y9Form>
        </template>
    </y9Dialog>
    <!--节点信息 -->
    <el-dialog v-model="openAdd" :title="userTitle" v-if="openAdd" :close-on-click-modal="false"
        :close-on-press-escape="false">
        <el-divider />
        <selectUser ref="selectUserRef" />
        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
                @click="submitData()">确定</el-button>
            <el-button @click="closeDialog()">取消</el-button>
        </template>
    </el-dialog>
    <flowNodeSort ref="flowNodeSortRef" @getDataListParent="getDataList"></flowNodeSort>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, defineProps, nextTick } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { getFlowNode, saveFlowNodeInfo, delFlowNodeInfoById, getFlowNodeInfoById } from '@/api/flownode/flownode'
import { ElMessage, ElMessageBox } from 'element-plus';
import selectUser from '@/layouts/components/selectuser/index.vue'
import flowNodeSort from './flowNodeSort.vue';
import {getSelectUserList} from '@/api/flownode/flownode'

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const sameId = ref()
const openAdd = ref(false)
const userTitle = ref("人员选择")
const userData = ref("")
const isShow = ref(false)
const selectUserRef = ref()
const flowNodeSortRef = ref()

//表格配置
let y9TableConfig = ref({
    rowKey: "id",
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
            title: computed(() => t('节点名称')),
            key: 'nodeName'
        },
        {
            title: computed(() => t('节点描述')),
            key: 'illustrate'
        },
        // {
        //     title: computed(() => t('审批人是否由发起人选择')),
        //     key: 'isPromoterSelect',
        //     render:(row)=>{
        //         if(row.isPromoterSelect==="Y"){
        //             return '是'
        //         }else{
        //             return '否'
        //         }
        //     }
        // },
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
            key: 'nodeName',
            label: computed(() => t('节点名称')),
            labelWidth: '62px',
            span: settingStore.device === 'mobile' ? 24 : 6,
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
async function getDataList() {
    y9TableConfig.value.loading = true;
    query.value.sameId = sameId.value;
    let res = await getFlowNode(
        query.value
    )
    y9TableConfig.value.tableData = res.data || []
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
function initTableData(id) {
    sameId.value = id
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
                    data.isPromoterSelect = 'Y'
                    let formData = new FormData();
                    for (let key in data) {
                        if (data[key] != null && key != "createTime" && key != "updateTime")
                            formData.append(key, data[key])
                    }
                    let res = await saveFlowNodeInfo(
                        formData
                    )
                    if (res.code == 0) {
                        ElMessage({
                            message: '数据保存成功',
                            type: 'success'
                        })
                        getDataList()
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
        sameId: sameId.value,
        isPrimary: 'N',
        isPromoterSelect: 'Y'
    },
    rules: {
        //	表单验证规则。类型：FormRules
        nodeName: [{ required: true, message: computed(() => t('节点名称不能为空')), trigger: 'blur' }],
        fieldName: [{ required: true, message: computed(() => t('字段名称不能为空')), trigger: 'blur' }],
        isPromoterSelect: [{ required: true, message: computed(() => t('该项必须选择')), trigger: 'blur' }],
        sort: [{ required: true, message: computed(() => t('序号不能为空')), trigger: 'blur' }
            , { validator: validateNumber, trigger: 'blur' }
        ],
        fieldVal: [{ required: true, message: computed(() => t('字段值不能为空')), trigger: 'blur' }],
        showVal: [{ required: true, message: computed(() => t('显示值不能为空')), trigger: 'blur' }],
        pid: [{ required: true, message: computed(() => t('父级不能为空')), trigger: 'blur' }],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t('节点名称')),
            prop: 'nodeName'
        },
        {
            type: 'textarea',
            label: computed(() => t('节点描述')),
            prop: 'illustrate',
            props: {
                rows: 3
            }
        },
        {
            type: 'slot',
            label: computed(() => t('审批人')),
            prop: 'approveUserName',
            props: {
                slotName: "selectuser"
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
        },
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});


function addDialog() {
    addDialogConfig.value.show = true
    ruleFormConfig.value.model = {}
    initItemList(false)
    userData.value = ""
    ruleFormConfig.value.model.sameId = sameId.value
    ruleFormConfig.value.model.isPrimary = "N"
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('新增节点信息'))
}

//编辑
function edit(id) {
    ruleFormConfig.value.model.id = id
    getInfoById(id)
    initItemList(false)
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('编辑权限信息'))
    addDialogConfig.value.show = true
}
//详情
function view(id) {
    ruleFormConfig.value.model.id = id
    getInfoById(id)
    initItemList(true)
    addDialogConfig.value.okText = false
    addDialogConfig.value.title = computed(() => t('查看权限信息'))
    addDialogConfig.value.show = true
}
//根据id获取数据
async function getInfoById(id) {
    let formData = {
        id: id
    }
    let res = await getFlowNodeInfoById(formData)
    ruleFormConfig.value.model = res.data
    userData.value = res.data.approveUserName
}
//刷新itemList
function initItemList(disabled) {
    isShow.value = !disabled
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props != undefined && it.props != null && it.props != "") {
            it.props.disabled = disabled
        } else {
            it.props = {
                disabled: disabled
            }
        }
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
        delFlowNodeInfoById(para).then((res) => {
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
//确定按钮
const submitData = () => {
    userData.value = ""
    let data = selectUserRef.value.getCheckedUser()
    if (data.length > 1) {
        openPromptDialog("选择人员确认", "超过最大选中人数，当前最多可以选择一位")
        return
    }
    let userId = ""
    for (let it of data) {
        userData.value += it.label + "，"
        userId += it.id + "，"
    }
    userData.value = userData.value.substring(0, userData.value.length - 1)
    userId = userId.substring(0, userId.length - 1)
    ruleFormConfig.value.model = ruleFormRef.value.model
    ruleFormConfig.value.model.approveUserId = userId
    ruleFormConfig.value.model.approveUserName = userData.value
    openAdd.value = false
}
//取消按钮
const closeDialog = () => {
    openAdd.value = false
}
const openSelectUser = () => {
    let para = {}
    getSelectUserList(para).then((res) => {
        openAdd.value = true
        let data = [
            {
                id: "1",
                label: "高院",
                type: "dept",
                children: [{
                    id: "29558739",
                    label: "双林",
                    type: "user",
                },
                {
                    id: "3",
                    label: "赵六",
                    type: "user",
                }
                ]
            }
        ]
        let checkedIds = []
        if (ruleFormConfig.value.model.approveUserId != undefined && ruleFormConfig.value.model.approveUserId != "" && ruleFormConfig.value.model.approveUserId != null) {
            checkedIds = ruleFormConfig.value.model.approveUserId.split("，")
        }
        if(res.code=="0"){
            data = res.userData
        }
        nextTick(() => {
            selectUserRef.value.initData(data, checkedIds)
        })
    })
}
//弹出提示信息
function openPromptDialog(title, msg) {
    ElMessageBox.alert(msg, title, {
        confirmButtonText: '确认'
    })
}
//节点顺序批量变更
const changeTableSort = ()=>{
    flowNodeSortRef.value.initTableData(sameId.value)
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
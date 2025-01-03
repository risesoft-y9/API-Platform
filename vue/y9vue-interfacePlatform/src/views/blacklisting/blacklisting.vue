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
        <template v-slot:slotBtns>
            <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main" type="primary" @click="addDialog">
                <i class="ri-add-line"></i>
                <span>{{ $t('新增') }}</span>
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
import { getPage, saveInfo, getInfoById, delInfoById, updateEnable } from '@/api/blacklisting/blacklisting'
import {getMayApplyInterfaceList} from '@/api/interface/interface'
import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus';

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
            width: "320",
            title: computed(() => t('ip')),
            key: 'ip'
        },
        {
            title: computed(() => t('备注')),
            key: 'notes'
        },
        {
            title: computed(() => t('排序')),
            key: 'sort'
        },
        {
            title: computed(() => t('创建时间')),
            key: 'createTime'
        },
        {
            title: computed(() => t('是否启用')),
            key: '',
            render: (row) => {
                if (row.sameId == '25cee41e73dd4281a93a9cb799dd9d12') {
                    return h(ElSwitch, {
                        modelValue: JSON.parse(row.isEnable)
                    });
                } else {
                    return h(ElSwitch, {
                        modelValue: JSON.parse(row.isEnable),
                        'onUpdate:modelValue': (val) => {
                            let formData = new FormData();
                            formData.append("id", row.id)
                            formData.append("isEnable", val)
                            updateEnable(formData).then((res) => {
                                if (res.status == 'success') {
                                    row.isEnable = val
                                } else {
                                    ElMessage({
                                        message: '启用失败，节点信息未补充',
                                        type: 'success'
                                    })
                                }
                            })
                        },
                    });
                }
            }
        },
        {
            title: computed(() => t('操作')),
            width: 300,
            fixed: 'right',
            render: (row) => {
                if (row.sameId == '25cee41e73dd4281a93a9cb799dd9d12') {

                } else {
                    return h('div', [h('span', { onClick: () => { view(row.id) } }, t("详情")),
                    h('span', { class: 'leftMargin', onClick: () => { edit(row.id) } }, t('编辑')),
                    h('span', { class: 'leftMargin', onClick: () => { delData(row.id) } }, t('删除')),
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
    rules: {
        //	表单验证规则。类型：FormRules
        name: [{ required: true, message: computed(() => t('名称不能为空')), trigger: 'blur' }],
        ip: [{ required: true, message: computed(() => t('ip不能为空')), trigger: 'blur' },{ validator: validateIP, trigger: 'blur' }],
        interfaceIds: [{ required: true, message: computed(() => t('应用接口不能为空')), trigger: 'blur' }],
        sort: [{ required: true, message: computed(() => t('排序不能为空')), trigger: 'blur' }
            , { validator: validateNumber, trigger: 'blur' }
        ],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t('名称')),
            prop: 'name',
            props:{
                maxlength:100,
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('ip')),
            prop: 'ip',
            props:{
                rows:3,
                maxlength:300,
                showWordLimit:true,
                placeholder:"请输入IP，多个IP使用英文逗号隔开，例如：123.123.123.123,1.1.1.1"
            }
        },
        {
            type: 'select',
            label: computed(() => t('应用接口')),
            prop: 'interfaceIds',
            props:{
                options: interfaceList.value,
                multiple:true,
                clearable:true
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('备注')),
            prop: 'notes',
            props:{
                rows:3,
                maxlength:254,
                showWordLimit:true
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
async function edit(id) {
    initInterfaceList()
    let para = {
        id: id
    }
    let res = await getInfoById(para)
    res.data.interfaceIds = res.data.interfaceIds.split(",")
    res.data.interfaceIds.shift()
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
    addDialogConfig.value.title = computed(() => t('编辑黑名单信息'))
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.show = true
}
//详情
async function view(id) {
    initInterfaceList()
    let para = {
        id: id
    }
    let res = await getInfoById(para)
    res.data.interfaceIds = res.data.interfaceIds.split(",")
    res.data.interfaceIds.shift()
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
    addDialogConfig.value.title = computed(() => t('查看黑名单信息'))
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
                getDataList()
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
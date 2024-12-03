<template>
    <y9Table :config="y9TableConfig" :filterConfig="filterOperaConfig" v-model:selectedVal="selectedCheckboxVal"
        ref="filterRef">
        <template v-slot:slotBtns>
            <el-button v-if="props.isView" :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                @click="addDialog">
                <i class="ri-add-line"></i>
                <span>{{ $t('新增') }}</span>
            </el-button>
            <el-button v-if="isView" :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="el-button el-button--default global-btn-third" @click="delParameter()">
                <i class="ri-delete-bin-line"></i>
                <span>{{ $t('删除') }}</span>
            </el-button>
        </template>
    </y9Table>
    <!-- 增加接口信息 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
            </y9Form>
        </template>
    </y9Dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject,defineProps } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import { ElMessage,ElMessageBox } from 'element-plus';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const isEdit = ref(false)
const selectedCheckboxVal = ref([])

const props = defineProps({
    parameterStatus:{
        type: String,
        default:()=>"3"
    },
    data:{
        type:Array,
        default:()=>[]
    },
    isView:{
        type:Boolean,
        default:()=>false
    }
})
let id=10000;
//表格配置
let y9TableConfig = ref({
    rowKey:"id",
    headerBackground: true,
    pageConfig: false,
    border:true,
    defaultExpandAll:true,
    height:"200px",
    columns: [
        {
            type: "selection",
            width: 60,
        },
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
            fixed: 'left'
        },
        {
            title: computed(() => t('参数key')),
            width: 180,
            key: 'parameterKey',
            render: (row) => {
                if(JSON.parse(row.isItems)){
                    return h('span', {class:'isItems'},row.parameterKey);
                }else{
                    return row.parameterKey;
                }
            }
        },
        {
            title: computed(() => t('层级')),
            key: 'level',
            render: (row) => {
                return h('span',tranNum(row.level)+"级");
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType'
        },
        {
            title: computed(() => t('是否必填')),
            key: 'required'
        },
        // {
        //     title: computed(() => t('默认值')),
        //     key: 'defaultVal'
        // },
        {
            title: computed(() => t('参数描述')),
            key: 'notes'
        },
        {
            title: computed(() => t('操作')),
            width: 150,
            fixed: 'right',
            render: (row) => {
                return h('div',rtBtns(row) );
            }
        }
    ],
    tableData: props.data,
});
//权限按钮控制
function rtBtns(row){
    let btns = []
    let viewBtn = h('span', { onClick: () => { view(row) } }, t("详情"));
    let addDataBtn = h('span', { class: 'leftMargin', onClick: () => { addData(row) } }, t('添加'));
    let editBtn = h('span', { class: 'leftMargin', onClick: () => { edit(row) } }, t('编辑'));
    let delBtn = h('span', { class: 'leftMargin', onClick: () => { delData(row.id) } }, t('删除'));
    if(props.isView){
        btns.push(viewBtn)
        btns.push(editBtn)
        if (row.parameterType == "map") {
            btns.push(addDataBtn)
        }
        if (!(row.isItems != undefined && row.isItems)) {
            btns.push(delBtn)
        }
    }else{
        btns.push(viewBtn)
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
            type: 'slot',
            slotName: 'slotBtns',
            span: settingStore.device === 'mobile' ? 24 : 24,
            justify: 'flex-end'
        }

    ],
    showBorder: true
});


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
    title: computed(() => t('新增参数')),
    showFooter:true,
    onOkLoading: true,
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            const y9RuleFormInstance = ruleFormRef.value?.elFormRef;
            await y9RuleFormInstance.validate(async (valid) => {
                let data = ruleFormRef.value.model;
                if (valid) {
                    if(data.pid!="0"){
                        let arData = []
                        for (let it of y9TableConfig.value.tableData) {
                            if (it.children instanceof Array) {
                                selectChild(it.children, arData);
                            }
                            arData.push(it);
                        }
                        for (let it of arData) {
                            //对编辑做处理
                            if(isEdit.value){
                                if (it.id == data.id) {
                                    editData(it,data)
                                    break;
                                }
                            }else {
                                if (it.id == data.pid) {
                                    if (!it.children) {
                                        it.children = []
                                    }
                                    if(data.parameterType=="array"){
                                        data.children = [{id:id++,pid:data.id,parameterType:"String",parameterStatus:props.parameterStatus,parameterKey:"items",required:"否",isItems:true,level:data.level+1}]
                                    }
                                    it.children.push(data)
                                    break;
                                }
                            }
                        }
                        resolve();
                    }else{
                        if(isEdit.value){
                            let arData = []
                            for (let it of y9TableConfig.value.tableData) {
                                if (it.children instanceof Array) {
                                    selectChild(it.children, arData);
                                }
                                arData.push(it);
                            }
                            for (let it of arData) {
                                //对编辑做处理
                                if (it.id == data.id) {
                                    editData(it,data)
                                    break;
                                }
                            }
                            resolve();
                        }else{
                            if (data.parameterType == "array") {
                                data.children = [{ id: id++, pid: data.id, parameterType: "String",parameterStatus:props.parameterStatus, parameterKey: "items", required: "否", isItems: true,level:data.level+1 }]
                            }
                            y9TableConfig.value.tableData.push(data)
                            resolve();
                        }
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

    },
    rules: {
        //	表单验证规则。类型：FormRules
        parameterKey: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        parameterType:[{required: true, message: computed(() => t('字段类型不能为空')), trigger: 'blur'}],
        required:[{required: true, message: computed(() => t('是否必填不能为空')), trigger: 'blur'}],
        pid:[{required: true, message: computed(() => t('父级不能不选择')), trigger: 'blur'}],
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t('参数key')),
            prop: 'parameterKey'
        },
        {
            type: 'select',
            label: computed(() => t('字段类型')),
            prop: 'parameterType',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('String')), value: 'String' },
                    { label: computed(() => t('integer')), value: 'integer' },
                    { label: computed(() => t('boolean')), value: 'boolean' },
                    { label: computed(() => t('double')), value: 'double' },
                    { label: computed(() => t('array')), value: 'array' },
                    { label: computed(() => t('map')), value: 'map' },
                    { label: computed(() => t('number')), value: 'number' },
                ],
                events:{
                    change:(val)=>{
                        if(ruleFormConfig.value.model.parameterType!=undefined){
                            if(ruleFormConfig.value.model.parameterType=="map" || ruleFormConfig.value.model.parameterType=="array"){
                                ElMessageBox.alert("参数类型由\""+ruleFormConfig.value.model.parameterType+"\"变更为\""+val+"\"将会覆盖子级节点数据", "参数类型变更确认", {
                                    confirmButtonText: '确认'
                                })
                            }                            
                        }                        
                    }
                }
            }
        },
        {
            type: 'select',
            label: computed(() => t('是否必填')),
            prop: 'required',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('是')), value: '是' },
                    { label: computed(() => t('否')), value: '否' }
                ]
            }
        },
        // {
        //     type: 'input',
        //     label: computed(() => t('默认值')),
        //     prop: 'defaultVal'
        // },
        {
            type: 'input',
            label: computed(() => t('参数描述')),
            prop: 'notes'
        },
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
function getPid(id,type){
    let item = {
        type: 'select',
        label: computed(() => t('选择父级')),
        prop: 'pid',
        props: {
            options: [
                //选项列表
                { label: computed(() => t('本级')), value: '0' },
            ],
            disabled:true
        }
    }
    let arData = []
    for (let it of y9TableConfig.value.tableData) {
        if (it.children instanceof Array) {
            selectChild(it.children, arData);
        } 
        arData.push(it);
        
    }
    for (let it of arData) {
        if (it.id != ruleFormConfig.value.model.id) {
            let selectItem = { label: computed(() => t(it.parameterKey)), value: it.id }
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
            if(it.prop!="pid"){
                if (it.props == undefined) {
                    it.props = {
                        disabled: false
                    }
                } else {
                    it.props.disabled = false
                }
            }
        }
    }else{
        for (let it of ruleFormConfig.value.itemList) {
            if (it.props == undefined) {
                it.props = {
                    disabled: true
                }
            } else {
                it.props.disabled = true
            }
        }
    }
}

//递归遍历数据
function selectChild(child,arrayData){
    if(child!=undefined){
        let indexSort = 1;
        for (let it of child) {
            if (it.children instanceof Array) {
                selectChild(it.children, arrayData)
            }
            it.sort = indexSort
            indexSort++;
            arrayData.push(it)
        }
    }
}
let outSort = 0
//外层添加按钮
function addDialog(){
    isEdit.value = false
    addDialogConfig.value.show = true
    ruleFormConfig.value.model = {pid:"0",id:id++,isItems:false,parameterStatus:props.parameterStatus,level:1,sort:++outSort}
    getPid("",false)
    for(let it of ruleFormConfig.value.itemList){
        if (it.prop != "pid") {
            if (it.props == undefined) {
                it.props = {
                    disabled: false
                }
            } else {
                it.props.disabled = false
            }
        }
        if(it.prop == "pid"){
            if (it.props == undefined) {
                it.props = {
                    disabled: true
                }
            } else {
                it.props.disabled = true
            }
        }
    }
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('新增参数信息'))
}

//行内添加按钮
const addData = (row) => {
    isEdit.value = false
    addDialogConfig.value.show = true
    ruleFormConfig.value.model = {pid:row.id,id:id++,isItems:false,parameterStatus:props.parameterStatus,level:row.level+1}
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('新增参数信息'))
    getPid("",false)
}
//编辑
function edit(row) {
    isEdit.value = true
    ruleFormConfig.value.model = row
    getPid("",false)
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.title = computed(() => t('编辑权限信息'))
    if(row.isItems){
        for (let it of ruleFormConfig.value.itemList) {
            if(it.prop=="parameterKey"){
                if (it.props == undefined) {
                    it.props = {
                        disabled: true
                    }
                } else {
                    it.props.disabled = true
                }
            }
        }
    }
    addDialogConfig.value.show = true
}
//详情
async function view(row) {
    ruleFormConfig.value.model = row
    getPid(row.id,true)
    addDialogConfig.value.okText = false
    addDialogConfig.value.title = computed(() => t('查看权限信息'))
    addDialogConfig.value.show = true
}
//删除
async function delData(id) {
    ElMessageBox.confirm(
        '是否确认删除这条数据，删除会同步删除该节点下的所有数据',
        '删除数据确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
            draggable: true
        }
    ).then(() => {
        let index = 0;
        for(let it of y9TableConfig.value.tableData){
            if(it.id==id){
                y9TableConfig.value.tableData.splice(index,1)
                break;
            }
            if(it.children){
                delChildData(it.children,id)
            }
            index++;
        }
    }).catch(() => {
        ElMessage({ type: 'info', message: '删除取消' })
    })
}
//遍历节点删除数据
const delChildData = (arr,id)=>{
    let index = 0;
    for(let it of arr){
        if(it.id==id){
            arr.splice(index,1)
            break;
        }
        if (it.children) {
            delChildData(it.children, id)
        }
        index++;
    }
}
//对编辑进行处理
const editData = (it,data)=>{
    let oldIsMap = false;
    let isOverwriteChildren = false

    let oldIsArray = false;
    let newIsArray = false;
    if(it.parameterType == "array"){
        oldIsArray=true
    }
    if(data.parameterType == "array"){
        newIsArray=true
    }

    if (it.parameterType == "map") {
        oldIsMap = true;
    }
    if (oldIsMap && data.parameterType != "map") {
        isOverwriteChildren = true
    }
    for (let objKey in it) {
        it[objKey] = data[objKey]
    }
    if (isOverwriteChildren) {
        it.children = []
    }
    if(oldIsArray){
        if(newIsArray){

        }else{
            it.children = []
        }
    }else{
        if (newIsArray) {
            it.children = [{id:id++,pid:data.id,parameterType:"String",parameterStatus:props.parameterStatus,parameterKey:"items",required:"否",isItems:true,level:it.level+1}]
        }
    }
}
//阿拉伯数字转大写数字
function tranNum(num){
    switch(num){
        case 1: return '一';break;
        case 2: return '二';break;
        case 3: return '三';break;
        case 4: return '四';break;
        case 5: return '五';break;
        case 6: return '六';break;
        case 7: return '七';break;
        case 8: return '八';break;
        case 9: return '九';break;
        case 10: return '十';break;
        default: return '十一'
    }
}
const resetTable = ()=>{
    y9TableConfig.value.tableData = []
}
const getTableData = ()=>{
    let arData = []
    let indexSort = 1;
    for (let it of y9TableConfig.value.tableData) {
        if (it.children instanceof Array) {
            selectChild(it.children, arData);
        } 
        it.sort = indexSort;
        indexSort++;
    }
    return y9TableConfig.value.tableData;
}
//删除
const delParameter = ()=>{
    if(selectedCheckboxVal.value.length==0){
        ElMessage({ type: 'warning', message: '未选择数据，无法删除' })
        return
    }
    for(let item of selectedCheckboxVal.value){
        let index = 0;
        for (let it of y9TableConfig.value.tableData) {
            if (it.id == item.id) {
                y9TableConfig.value.tableData.splice(index, 1)
                break;
            }
            if (it.children) {
                delChildData(it.children, item.id)
            }
            index++;
        }
    }
}
defineExpose({
    resetTable,getTableData
})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.isItems{
    color: #409eff !important;
}
</style>
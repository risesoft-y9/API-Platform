<template>
    <!--审批页面 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <el-divider content-position="left">申请信息</el-divider>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig" v-if="ruleFormConfig.model.applyType=='1' || ruleFormConfig.model.applyType=='2'">
                <template #stopdate>
                    <el-date-picker :disabled="true" v-model="ruleFormConfig.model.applyStopTime" type="datetime" placeholder="选择时间"/>
                </template>
            </y9Form>
            <applyInfo v-if="ruleFormConfig.model.applyType=='0'" v-model:isLimitData="isLimitData" v-model:isView="isView" ref="applyInfoRef" v-model:select-data="selectData" v-model:interfaceId="interfaceId"></applyInfo>
            <el-divider content-position="left">审批信息</el-divider>
            <y9Form ref="approveRef" :config="approveFormConfig">
            </y9Form>
        </template>
    </y9Dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useI18n } from 'vue-i18n';
import {getApplyInfoById} from '@/api/apply/apply';
import {getApproveById,submitApproveData} from '@/api/approve/approve';
import applyInfo from '@/views/approve/applyInfo.vue';

// 注入 字体对象
const { t } = useI18n();
const ruleFormRef = ref()
const approveRef = ref()
const applyInfoRef = ref()
const interfaceId = ref()
const isView = ref(true)
const isLimitData = ref("否")

const props = defineProps({
    openDialog:{
        type:Boolean,
        default:()=>false
    },
    isView:{
        type:Boolean,
        default:()=>false
    },
    selectData:{
        type:String
    }
})
const selectData = ref()
const emit = defineEmits(['update:openDialog','update:selectData','getDataListParent'])


// 增加 修改应用 弹框的变量配置 控制
let addDialogConfig = ref({
    show: props.openDialog,
    title: computed(() => t('发布接口信息')),
    showFooter:true,
    onOkLoading: true,
    margin: "5vh auto",
    okText:"确定",
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            const y9RuleFormInstance = approveRef.value?.elFormRef;
            await y9RuleFormInstance.validate(async (valid) => {
                if (valid) {
                    let data = approveRef.value.model;
                    let formData = new FormData();

                    for (let key in data) {
                        if(data[key]!=null && key != "createTime" && key !="updateTime")
                        formData.append(key,data[key])
                    }

                    let res = await submitApproveData(
                        data
                    )
                    if (res.code == 0) {
                        if (res.status == "success") {
                            ElMessage({
                                message: '审批成功',
                                type: 'success'
                            })
                            emit('getDataListParent')
                            resolve();
                        } else {
                            ElMessage({
                                message: '' + res.msg,
                                type: 'success'
                            })
                            reject();
                        }
                    }
                    
                    reject();
                } else {
                    reject();
                }
            });
        });
    },
    visibleChange:(visible)=>{
        emit("update:openDialog",visible)
    }
});

// 发布表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        applyStopTime: ""
    },
    rules: {
        //	表单验证规则。类型：FormRules
        applyReason: [{ required: true, message: computed(() => t('申请事由不能为空')), trigger: 'blur' }],
    },
    itemList: [
        {
            type: 'textarea',
            label: computed(() => t('申请事由')),
            prop: 'applyReason',
            props: {
                disabled:true,
                //文本域类型的属性
                rows: 3 //输入框行数,类型：number
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('备注')),
            prop: 'notes',
            props: {
                disabled:true,
                //文本域类型的属性
                rows: 3 //输入框行数,类型：number
            }
        },
        {
            type: 'slot',
            label: computed(() => t('申请停用时间')),
            prop: 'applyStopTime',
            props:{
                slotName:"stopdate"
            }
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});

// 审批表单
let approveFormConfig = ref({
    //表单配置
    model: {
        approveStatus:"0"
    },
    rules: {
        //	表单验证规则。类型：FormRules
        applyReason: [{ required: true, message: computed(() => t('申请事由不能为空')), trigger: 'blur' }],
        approveStatus: [{ required: true, message: computed(() => t('审批意见不能为空')), trigger: 'blur' }],
        illustrate:[{required: true, message: computed(() => t('审批说明不能为空')), trigger: 'blur'}]
    },
    itemList: [
    {
            type: 'select',
            label: computed(() => t('审批意见')),
            prop: 'approveStatus',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('通过')), value: '0' },
                    { label: computed(() => t('不通过')), value: '1' }
                ]
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('审批说明')),
            prop: 'illustrate',
            props: {
                //文本域类型的属性
                rows: 3, //输入框行数,类型：number
                maxlength:1000
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('备注')),
            prop: 'notes',
            props: {
                //文本域类型的属性
                rows: 3, //输入框行数,类型：number
                maxlength:500
            }
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});

function openApproveDialog(id,applyType,interfaceStatus,limitData){
    isLimitData.value = limitData
    getData(id)
    if(applyType=="1"){
        addDialogConfig.value.title = computed(() => t('审批接口停用信息'))
    }else if(applyType=="2"){
        addDialogConfig.value.title = computed(() => t('审批接口发布信息'))
    }else{
        addDialogConfig.value.title = computed(() => t('审批接口调用信息'))
    }
    if(interfaceStatus=="停用"){
        let newData = []
        let itData = {
            type: 'slot',
            label: computed(() => t('申请停用时间')),
            prop: 'applyStopTime',
            props:{
                slotName:"stopdate"
            }
        }
        newData.push(itData)
        if(ruleFormConfig.value.itemList.length==2){
            for (let it of ruleFormConfig.value.itemList) {
                newData.push(it)
            }
            ruleFormConfig.value.itemList = newData;
        }
    }else if(interfaceStatus=="发布"){
        let newData = []
        for(let it of ruleFormConfig.value.itemList){
            if(it.prop!='applyStopTime'){
                newData.push(it)
            }
        }
        ruleFormConfig.value.itemList = newData;
    }
    addDialogConfig.value.show = true
}
function getData(id){
    initData()
    let param = {
        id:id
    }
    getApproveById(param).then((res) => {
        let para = {
            id: res.data.applyId
        }
        approveFormConfig.value.model = res.data
        approveFormConfig.value.model.approveStatus = "0"
        approveFormConfig.value.model.illustrate = ""
        approveFormConfig.value.model.notes = ""

        getApplyInfoById(para).then((response) => {
            ruleFormConfig.value.model = response.data
            nextTick(()=>{
                if(response.data.applyType == "0")
                applyInfoRef.value.initFormData(response.data)
            })
        })
        interfaceId.value = res.data.interfaceId
    })
}
function initData(){
    ruleFormConfig.value.model = {}
    approveFormConfig.value.model = {}
}
defineExpose({openApproveDialog})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
</style>
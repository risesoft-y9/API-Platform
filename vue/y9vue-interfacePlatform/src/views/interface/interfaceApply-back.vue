<template>
    <!--发布停用填写信息页面 -->
    <y9Dialog v-model:config="addDialogConfig">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #stopdate>
                    <el-date-picker v-model="applyTime" type="datetime" placeholder="选择时间"/>
                </template>
                <template #openAuthDialog>
                    <el-button @click="openAuthDialog()">点击选择要申请的权限范围信息</el-button>
                </template>
            </y9Form>
        </template>
    </y9Dialog>
    <authDialog ref="authDialogRef" v-model:select-data="selectData"v-model:open-dialog="isOpen" v-model:interfaceId="interfaceId"></authDialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useI18n } from 'vue-i18n';
import {useInterfaceApply} from '@/api/approve/approve';
import authDialog from './authDialog.vue';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const authRef = ref()
const ruleFormRef = ref()
const btnType = ref("")
const isOpen = ref(false)
const interfaceId = ref()
const authDialogRef = ref()
const applyTime = ref("")
const selectData = ref()

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
const limitInfo = ref(false)
const limitTitle = ref("选择权限信息")

//关闭配置信息
function closeDialog(type){
    emit("update:openDialog",false)
}
//确定配置信息
function confirDialog(type){
    emit("update:openDialog",false)
    emit("update:selectData",authRef.value.getCheckData())
}
// function init(){
//     authRef.value.initTableData()
// }

// 增加 修改应用 弹框的变量配置 控制
let addDialogConfig = ref({
    show: props.openDialog,
    title: computed(() => t('接口调用申请')),
    showFooter:true,
    onOkLoading: true,
    margin:'10vh auto',
    okText:'保存并提交',
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            const y9RuleFormInstance = ruleFormRef.value?.elFormRef;
            console.log(selectData.value)
            await y9RuleFormInstance.validate(async (valid) => {
                if (valid) {
                    try{
                        authDialogRef.value.getRuleForm().validate((valid) => {
                            if (!valid) {
                                ElMessageBox.alert("权限信息页面有必填项未填写，请打开页面确认！", "权限信息必填确认", {
                                    confirmButtonText: '确认'
                                })
                                reject()
                                return;
                            }
                        })
                    }catch(e){
                        ElMessageBox.alert("权限信息页面未选择，请打开页面确认！", "权限信息必填确认", {
                            confirmButtonText: '确认'
                        })
                        reject()
                        return;
                    }

                    let data = ruleFormRef.value.model;
                    let formData = new FormData();

                    for (let key in data) {
                        if(data[key]!=null && key != "createTime" && key !="updateTime")
                        formData.append(key,data[key])
                    }
                    //获取权限信息
                    for(let key in selectData.value){
                        let valStr = ""
                        for(let it of selectData.value[key]){
                            valStr+=it+","
                        }
                        formData.append(key,valStr.substring(0,valStr.length-1))
                    }
                    if(btnType.value=="停用"){

                    }else{
                        let res = await useInterfaceApply(
                            formData
                        )
                        if (res.code == 0) {
                            if (res.status == "success") {
                                ElMessage({
                                    message: '数据提交成功，等待审核，详细信息请移步到 个人中心>已申请接口 查看',
                                    type: 'success',
                                    duration:6000
                                })
                            } else {
                                ElMessage({
                                    message: '' + res.msg,
                                    type: 'success'
                                })
                            }
                            emit('getDataListParent')
                            resolve();
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
        if(!visible){
            authDialogRef.value.restData()
        }
    }
});

// 发布表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        interfaceId:"",
        applyStopTime: "",
        applyTime:""
    },
    rules: {
        //	表单验证规则。类型：FormRules
        applyReason: [{ required: true, message: computed(() => t('申请事由不能为空')), trigger: 'blur' }],
        systemIdentifier: [{ required: true, message: computed(() => t('申请事由不能为空')), trigger: 'blur' }],
        ipWhitelist: [{ required: true, message: computed(() => t('IP名单不能为空')), trigger: 'blur' }],
        applyTime: [{ required: true, message: computed(() => t('申请日期不能为空')), trigger: 'blur' }],
        applyPersonDeptName: [{ required: true, message: computed(() => t('接口调用单位名称不能为空')), trigger: 'blur' }],
        usePersonResponsible: [{ required: true, message: computed(() => t('接口调用责任人不能为空')), trigger: 'blur' }],
        usePersonResponsiblePhone: [{ required: true, message: computed(() => t('责任人联系方式不能为空')), trigger: 'blur' }],
    },
    itemList: [
        {
            type: 'textarea',
            label: computed(() => t('申请事由')),
            prop: 'applyReason',
            props: {
                //文本域类型的属性
                rows: 3 //输入框行数,类型：number
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('备注')),
            prop: 'notes',
            props: {
                //文本域类型的属性
                rows: 3 //输入框行数,类型：number
            }
        },
        // {
        //     type: 'date',
        //     label: computed(() => t('申请日期')),
        //     prop: 'applyTime',
        // },
        // {
        //     type: 'time',
        //     label: computed(() => t('申请时间')),
        //     prop: 'applyTime1',
        // },
        {
            type: 'input',
            label: computed(() => t('申请系统标识')),
            prop: 'systemIdentifier',
        },
        {
            type: 'slot',
            label: computed(() => t('权限信息申请')),
            props:{
                slotName:"openAuthDialog"
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('IP名单')),
            prop: 'ipWhitelist',
            props: {
                placeholder:"多个IP使用英文逗号隔开",
                //文本域类型的属性
                rows: 3 //输入框行数,类型：number
            }
        },
        {
            type: 'input',
            label: computed(() => t('接口调用单位名称')),
            prop: 'applyPersonDeptName',
        },
        {
            type: 'input',
            label: computed(() => t('接口调用责任人')),
            prop: 'usePersonResponsible',
        },
        {
            type: 'input',
            label: computed(() => t('责任人联系方式')),
            prop: 'usePersonResponsiblePhone',
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
function openPubDialog(id,type){
    ruleFormConfig.value.model.interfaceId = id
    interfaceId.value = id
    addDialogConfig.value.show = true
}
const openAuthDialog = ()=>{
    isOpen.value = true
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
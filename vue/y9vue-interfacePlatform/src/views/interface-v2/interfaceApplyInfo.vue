<template>
    <!--发布停用填写信息页面 -->
    <el-dialog v-model="openDialog" :title="Title"  append-to-body="true" @closed="closed">
        <template v-slot>
            <applyInfo ref="applyInfoRef" v-model:isView="isView" v-model:select-data="selectData"v-model:open-dialog="isOpen" v-model:isAuth="isAuthInfo" v-model:interfaceId="interfaceId"></applyInfo>
        </template>
        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
            @click="closeDialog"
            >确定</el-button>
            <el-button @click="closeDialog">取消</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, ref ,nextTick} from 'vue';
import { useI18n } from 'vue-i18n';
import { ElMessage, ElMessageBox } from 'element-plus';
import applyInfo from './applyInfo.vue';
import {getApplyInfoByInterfaceId} from '@/api/interface/interface'

// 注入 字体对象
const { t } = useI18n();
const isOpen = ref(false)
const interfaceId = ref()
const selectData = ref()
const applyInfoRef = ref()
const approveStatus = ref()
const isView = ref(true)
const isAuthInfo = ref()

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
const openDialog = ref(false)
const Title = ref("查看申请信息")
const emit = defineEmits(['update:openDialog','update:selectData','getDataListParent'])




function openPubDialog(id,type,status){
    interfaceId.value = id
    if(type=="申请"){
        let para = {
            id: id
        }
        getApplyInfoByInterfaceId(para).then((response) => {
            nextTick(()=>{
                applyInfoRef.value.initFormData(response.data)
            })
        })
    }
    approveStatus.value = status
    console.log(approveStatus.value)
}
//赋值
const initFormData = (data,isAuth)=>{
    openDialog.value = true
    interfaceId.value = data.interfaceId
    isAuthInfo.value = isAuth
    nextTick(()=>{
        applyInfoRef.value.initFormData(data)
    })
}
//关闭配置信息
function closeDialog(){
    openDialog.value = false
}
//关闭后调用
function closed(){
    applyInfoRef.value.resetFormData()
}
defineExpose({openPubDialog,initFormData})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
</style>
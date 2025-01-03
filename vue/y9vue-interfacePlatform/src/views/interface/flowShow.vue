<template>
    <!--流程图展示 -->
    <el-drawer v-model="props.openDialog" :title="limitTitle" @closed="closeDialog" @opened="init"
        v-if="props.openDialog">

        <el-card class="cardFont" shadow="always"  v-loading="loading">
            <el-row>
                <el-col :span="labelSpan">审批人员：</el-col>
                <el-col :span="contentSpan">
                    <div >{{ entity.personName }}</div>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="labelSpan">审批时间：</el-col>
                <el-col :span="contentSpan">
                    <div >{{ entity.updateTime }}</div>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="labelSpan">审批意见：</el-col>
                <el-col :span="contentSpan">
                    <div :class="entity.buttonDiv">{{ entity.approveStatus }}</div>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="labelSpan">审批说明：</el-col>
                <el-col :span="contentSpan">{{ entity.illustrate }}</el-col>
            </el-row>
            <el-row>
                <el-col :span="labelSpan">备注：</el-col>
                <el-col :span="contentSpan">{{ entity.notes }}</el-col>
            </el-row>
        </el-card>

        <template #footer>
            <el-button class="el-button el-button--default global-btn-third"
                @click="confirDialog('1')">关闭</el-button>
        </template>
    </el-drawer>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import {getApproveByInterfaceId,getApproveById} from '@/api/approve/approve';
import { useI18n } from 'vue-i18n';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const authRef = ref()
const entity = ref([]);
const loading = ref(true)

const props = defineProps({
    openDialog:{
        type:Boolean,
        default:()=>false
    },
    isView:{
        type:Boolean,
        default:()=>false
    },
    type:{
        type:Boolean,
        default:()=>false
    },
    interfaceId:{
        type:String,
    },
    selectData:{
        type:String
    },
    applyType:{
        type:Boolean,
        default:()=>false
    }
})
const emit = defineEmits(['update:openDialog','update:selectData','update:type'])
const limitTitle = ref("审批详情")
const active = ref(1)
const activeNodes = ref(0)
const applyInfo = ref({})
const buttonDiv1 = ref("buttonDiv")
const buttonDiv2 = ref("buttonDiv")
const labelSpan = ref(5)
const contentSpan = ref(19)
const currentNodeId = ref("")
const isOver = ref("N")

//关闭配置信息
function closeDialog(type){
    active.value = 1;
    entity.value = []
    applyInfo.value = {}
    buttonDiv1.value = "buttonDiv"
    buttonDiv2.value = "buttonDiv"
    emit("update:openDialog",false)
    emit("update:type",false)
}
//确定配置信息
function confirDialog(type){
    emit("update:openDialog",false)
    emit("update:type",false)
}
function init(){
    loading.value = true
    activeNodes.value = 0;
    let param = {
        id:props.interfaceId
    }
    if(props.type){
        param.applyType = props.applyType
        getApproveByInterfaceId(param).then((res) => {
            if (res.applyInfo != undefined) {
                applyInfo.value = res.applyInfo
            }
            if (res.data != null) {
                entity.value = res.data
                isOver.value = res.data.isOver
                console.log(entity.value)
            }
            initApproveStatus()
        })
    }else{
        getApproveById(param).then((res) => {
            if (res.applyInfo != undefined) {
                applyInfo.value = res.applyInfo
            }
            entity.value = res.data
            isOver.value = res.data.isOver
            initApproveStatus()
        })
    }
}
//初始化审批意见
function initApproveStatus(){
    let it = entity.value
    if (it.approveStatus == "不通过") {
        it.buttonDiv = "buttonDiv refuseBtn"
    } else if (it.approveStatus == "通过") {
        it.buttonDiv = "buttonDiv successBtn"
    } else {
        it.buttonDiv = "buttonDiv borderNone"
    }
    loading.value = false
}
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.timestep{
    color: black;
    font-size: 14px;
}
.buttonDiv{
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    display: inline-block;
    padding: 0 5px;
    color: white;
}
.refuseBtn{
    background-color: #f56c6c;
    border-color: #f56c6c;
}
.successBtn{
    background-color: #67c23a;
    border-color: #67c23a;
}
.borderNone{
    border-color: white;
}
.cardFont{
    font-size: 14px;
}
.el-drawer__body{
    border-top:1px #dcdfe6 solid;
}
.flowNodeTitle{
    font-size: 16px;
}
</style>
<style scoped>
.el-row{
    margin-bottom: 10px;
}
</style>
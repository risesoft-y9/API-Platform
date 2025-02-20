<template>
    <!--流程图展示 -->
    <el-drawer v-model="props.openDialog" :title="limitTitle" @closed="closeDialog" @opened="init" v-if="props.openDialog">
        <el-steps direction="vertical" :active="active" finish-status="success" :space="80">
            <el-step title="开始">
                <template #title>
                    <el-row>
                        <el-col :span="4"><span class="flowNodeTitle">发起人</span></el-col>
                        <el-col :span="3"><span class="timestep">{{applyInfo.applyPersonName!=undefined?applyInfo.applyPersonName:"开始"}}</span></el-col>
                        <el-col v-if="applyInfo.applyPersonName!=undefined" :span="17"><span class="timestep">提交时间：</span><span class="timestep">{{
                                applyInfo.createTime }}</span></el-col>
                    </el-row>
                </template>
            </el-step>
            <el-step :title="item.nodeName" v-for="(item,index) in entity">
                <template #title  v-if="item.nodeName!='结束'">
                    <el-row>
                        <el-col :span="4"><span class="flowNodeTitle">{{item.nodeName}}</span></el-col>
                        <el-col :span="3"><span class="timestep">{{item.approveUserName}}</span></el-col>
                        <el-col :span="17"><span class="timestep">审批时间：</span><span
                                class="timestep">{{ item.endTime }}</span></el-col>
                    </el-row>
                </template>
                <template #description v-if="item.nodeName!='结束'">
                    <el-card class="cardFont" v-if="active > (index+1)" shadow="always">
                        <el-row>
                            <el-col :span="labelSpan">审批意见：</el-col>
                            <el-col :span="contentSpan">
                                <div :class="item.buttonDiv">{{ item.approveStatus}}</div>
                                </el-col>
                        </el-row>
                        <el-row>
                            <el-col :span="labelSpan">审批说明：</el-col>
                            <el-col :span="contentSpan">{{ item.approveIllustrate }}</el-col>
                        </el-row>
                        <el-row>
                            <el-col :span="labelSpan">备注：</el-col>
                            <el-col :span="contentSpan">{{ item.approveNotes }}</el-col>
                        </el-row>
                    </el-card>
                </template>
            </el-step>
        </el-steps>
        <template #footer>
            <el-button class="el-button el-button--default global-btn-second"
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
const limitTitle = ref("审批进度")
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
    activeNodes.value = 0;
    let param = {
        id:props.interfaceId
    }
    debugger
    if(props.type){
        param.applyType = props.applyType
        getApproveByInterfaceId(param).then((res)=>{
        if(res.applyInfo!=undefined){
            applyInfo.value = res.applyInfo
        }
        if(res.data!=null){
            if(res.data.flowNode!=null && res.data.flowNode!=""){
                entity.value = JSON.parse(res.data.flowNode)
            }
            if(res.data.currentNode!=null && res.data.currentNode!=""){
                currentNodeId.value = JSON.parse(res.data.currentNode).id
            }
            isOver.value = res.data.isOver
            console.log(entity.value)
        }
        initApproveStatus()
    })
    }else{
        getApproveById(param).then((res)=>{
        if(res.applyInfo!=undefined){
            applyInfo.value = res.applyInfo
        }
        if(res.data.flowNode!=null && res.data.flowNode!=""){
                entity.value = JSON.parse(res.data.flowNode)
            }
        if (res.data.currentNode!=null && res.data.currentNode != "") {
            currentNodeId.value = JSON.parse(res.data.currentNode).id
        }
        isOver.value = res.data.isOver
        initApproveStatus()
    })
    }
}
//初始化审批意见
function initApproveStatus(){
    let index = 1;
    let newData = []
    for(let it of entity.value){
        if(it.approveStatus=="不通过"){
            it.buttonDiv = "buttonDiv refuseBtn"
        }else if(it.approveStatus=="通过"){
            it.buttonDiv = "buttonDiv successBtn"
        }else{
            it.buttonDiv = "buttonDiv borderNone"
        }
        if(currentNodeId.value==it.id){
            active.value = index
        }
        index++;
        newData.push(it)
        if(it.approveStatus=="不通过"){
            break
        }
    }
    if(isOver.value=="Y"){
        entity.value = newData
        entity.value.push({nodeName:"结束"})
        active.value = index+1
    }else{
        entity.value.push({nodeName:"结束"})
    }
}
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.el-row{
    margin-bottom: 10px;
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
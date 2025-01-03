<template>
    <!--节点信息 -->
    <el-dialog v-model="openAdd" :title="userTitle" v-if="openAdd" :close-on-click-modal="false" :close-on-press-escape="false" width="60%">
        <el-divider />
        <el-row :gutter="10">
            <el-col :span="20">
                <selectUser ref="selectUserRef" @updateSelectData="updateSelectData"/>
            </el-col>
            <el-col :span="4">
                <el-table :data="nodeData" border stripe header-row-class-name="tableheader">
                    <el-table-column prop="nodeName" label="节点名称" align="center" />
                    <el-table-column prop="approveUserName" label="审批人" align="center"/>
                </el-table>
            </el-col>
        </el-row>
        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
                @click="submitData()">确定</el-button>
            <el-button @click="closeDialog()">取消</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject,defineProps, nextTick } from 'vue';
import { ElMessage,ElMessageBox } from 'element-plus';
import selectUser from '@/layouts/components/selectuser/index.vue'

// 注入 字体对象

const openAdd = ref(false)
const userTitle = ref("人员选择")
const userData = ref("")

const selectUserRef = ref()
const nodeData = ref([{nodeName:"发布"}])
const len = ref(1)
const emit = defineEmits(["submitData"])

//确定按钮
const submitData = ()=>{
    userData.value = ""
    let data = selectUserRef.value.getCheckedUser()
    if(data.length>nodeData.value.length){
        openPromptDialog("选择人员确认","超过需要选中人数，当前需要"+nodeData.value.length+"位")
        return
    }
    if(data.length<nodeData.value.length){
        openPromptDialog("选择人员确认","低于需要选中人数，当前需要"+nodeData.value.length+"位")
        return
    }
    let userId = ""
    let rtUserDatas = []
    for(let it of data){
        userData.value+=it.label+"，"
        userId+=it.id+"，"
        let user = {
            id:it.id,
            name:it.label
        }
        rtUserDatas.push(user)
    }
    userData.value = userData.value.substring(0,userData.value.length-1)
    userId = userId.substring(0,userId.length-1)
    openAdd.value = false
    emit('submitData',JSON.stringify(rtUserDatas))
}
//取消按钮
const closeDialog = ()=>{
    openAdd.value = false
}
const openSelectUser = (approveUserId,nodes,userData)=>{
    nodeData.value = nodes
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
    let checkedIds=[]
    if(approveUserId!=undefined && approveUserId!=""&&approveUserId!=null){
        if(approveUserId instanceof Array){
            checkedIds = approveUserId
        }else{
            checkedIds = approveUserId.split("，")
        }
    }
    if(userData!=null && userData!=undefined && userData!=""){
        if(userData instanceof Array){
            data = userData
        }else{
            data = JSON.parse(userData)
        }
    }
    nextTick(()=>{
        selectUserRef.value.initData(data,checkedIds)
    })
}
//更新选人数据
const updateSelectData = ()=>{
    let data = selectUserRef.value.getCheckedUser()
    if(data.length>nodeData.value.length){
        openPromptDialog("选择人员确认","当前选择人员过多超过所需人员请确认")
    }
    let index = 0
    for(let node of nodeData.value){
        if(index<data.length){
            node.approveUserName = data[index].label
        }else{
            node.approveUserName = ""
        }
        index++;
    }
}
//弹出提示信息
function openPromptDialog(title, msg) {
    ElMessageBox.alert(msg, title, {
        confirmButtonText: '确认'
    })
}
defineExpose({openSelectUser})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.tableheader{
    height: 55px;
}
</style>
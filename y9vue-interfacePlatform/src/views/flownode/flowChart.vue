<template>
    <!--节点信息 -->
    <el-dialog v-model="openAdd" :title="title" v-if="openAdd" :close-on-click-modal="false"
        :close-on-press-escape="false">
        <el-scrollbar>
            <div class="box" ref="boxRef">
                <div class="card">
                    <div class="flowStart"></div>
                </div>
                <div class="lineClass">
                    <div class="line" :style="{width:lineWidth + 'px'}"></div>
                    <div class="lineEnd"></div>
                </div>
                <div class="forBox" v-for="item in array">
                    <div class="card">
                        <div class="node">
                            <el-icon>
                                <Avatar></Avatar>
                            </el-icon>
                            <el-scrollbar max-height="72px">
                                <el-tooltip placement="top">
                                    <template #content> {{ "审批人："+(item.approveUserName==""?"-":item.approveUserName) }} </template>
                                    <div class="nodeName"><span>{{ item.nodeName }}</span></div>
                                </el-tooltip>
                            </el-scrollbar>
                        </div>
                    </div>
                    <div class="lineClass">
                        <div class="line" :style="{width:lineWidth + 'px'}"></div>
                        <div class="lineEnd"></div>
                    </div>
                </div>

                <div class="card">
                    <div class="flowEnd"></div>
                </div>
            </div>
        </el-scrollbar>

        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
                @click="submitData()">确定</el-button>
            <el-button @click="closeDialog()">取消</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject, defineProps, nextTick } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { ElMessage, ElMessageBox } from 'element-plus';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const array = ref([]);
const lineWidth = ref(30)
const boxRef = ref()

const openAdd = ref(false)
const title = ref("流程图")
const query: any = ref({});

const userData = ref("")
const selectUserRef = ref()

//确定按钮
const submitData = () => {
    openAdd.value = false
}
//取消按钮
const closeDialog = () => {
    openAdd.value = false
}
//弹出提示信息
function openPromptDialog(title, msg) {
    ElMessageBox.alert(msg, title, {
        confirmButtonText: '确认'
    })
}
//初始化流程图
const initFlowChart = (data) => {
    array.value = data
    openAdd.value = true
    nextTick(()=>{
    console.log(boxRef.value.offsetWidth)
        let zline = boxRef.value.offsetWidth - 104*array.value.length - 29 - 33 - 20*(array.value.length+1)
        console.log(zline)
        let widthLine = zline/(array.value.length+1)
        console.log(widthLine)
        if(widthLine<30){
            lineWidth.value = 30 
        }else{
            lineWidth.value = widthLine
        }
    })
}

defineExpose({
    initFlowChart
})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}

.flowStart {
    border: 2px solid black;
    border-radius: 50%;
    height: 25px;
    width: 25px;
}

.flowEnd {
    border: 4px solid black;
    border-radius: 50%;
    height: 25px;
    width: 25px;
}

.box {
    display: flex;
}

.line {
    position: relative;
    top: 0;
    border-bottom: 2px solid black;
    height: 0px;
    margin-left: 5px;
}

.lineEnd {
    height: 1px;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-left: 10px solid black;
    margin-right: 5px;
}

.lineClass {
    height: 100px;
    display: flex;
    justify-items: center;
    align-items: center;
}

.card {
    display: flex;
    height: 100px;
    justify-content: center;
    align-items: center;
}

.node {
    display: flex;
    height: 60px;
    width: 100px;
    border: 2px solid black;
    border-radius: 5px;
}

.nodeName {
    width: 72px;
    height: 60px;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    word-wrap: break-word;
    overflow-wrap: break-word;
}

.forBox {
    display: flex;
    justify-content: center;
    align-items: center;
}
.nodeName span{
    line-height: 14px;
    height: 28px;
    overflow: scroll;
}
</style>
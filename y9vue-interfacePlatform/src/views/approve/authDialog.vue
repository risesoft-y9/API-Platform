<template>
    <!--权限信息 -->
    <el-dialog v-model="props.openDialog" :title="limitTitle" @closed="closeDialog" @opened="init" v-if="props.openDialog" append-to-body="true">
        <el-divider />
        <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules">
            <el-form-item v-for="item in data" :key="item.id" :label="item.label" :prop="item.prop">
                <el-tree-select 
                v-if="item.type == 'slot'" 
                v-model="ruleForm[item.prop]" 
                :data="item.options" multiple
                :render-after-expand="false" 
                show-checkbox
                check-strictly
                >

                </el-tree-select>
                <el-select v-if="item.type=='select'" v-model="ruleForm[item.prop]" multiple>
                    <el-option v-for="childIt in item.options" :key="childIt.id" :label="childIt.label"
                        :value="childIt.value" :disabled="props.isView"/>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
                @click="confirDialog('1')">确定</el-button>
            <el-button @click="closeDialog('1')">取消</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import {getAuthListByInterfaceId} from '@/api/interface/interface'
import { ElMessage, ElMessageBox } from 'element-plus';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const authRef = ref()
const ruleForm = ref({})
const ruleFormRef = ref()
const rules = ref({
        //	表单验证规则。类型：FormRules
        thresholdType: [{ required: true, message: computed(() => t('阈值类型不能为空')), trigger: 'blur' }],
    })
const data = ref([])
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
        type:Object
    }
})
const emit = defineEmits(['update:openDialog','update:selectData'])
const limitInfo = ref(false)
const limitTitle = ref("权限信息")

//关闭配置信息
function closeDialog(type){
    emit("update:openDialog",false)
}
//确定配置信息
function confirDialog(type){
    ruleFormRef.value.validate((valid)=>{
        if(valid){
            emit("update:openDialog",false)
            emit("update:selectData",ruleForm.value)
        }
    })
}
function init(){
    let para = {
        id: props.interfaceId,
        isView:props.isView
    }
    getAuthListByInterfaceId(para).then((res) => {
        data.value = res.data
        console.log(props.selectData)
        if(props.isView){

        }
        let requiredData = res.requiredData
        for(let it of requiredData){
            rules.value[it.parameterKey] = [{ required: true, message: computed(() => t('参数不能为空')), trigger: 'blur' }];
        }
        let delKeys = []
        for(let key in props.selectData){
            if(props.selectData[key].length==1&&props.selectData[key][0]==''){
                delKeys.push(key)
            }
        }
        for(let key of delKeys){
            delete props.selectData[key];
        }
        ruleForm.value = props.selectData
    }).catch(() => {
        ElMessage({ message: '获取权限信息失败', type: 'warning' })
    })
}
const restData = ()=>{
    ruleForm.value = {}
    data.value = []
}
//确定配置信息
const getRuleForm = ()=>{
    emit("update:selectData",ruleForm.value)
    return ruleFormRef.value;
}
defineExpose({restData,getRuleForm})
</script>
<style scoped>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.el-divider--horizontal{
    margin:0 0 24px 0;
}
</style>
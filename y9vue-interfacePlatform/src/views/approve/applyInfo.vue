 <template>
    <!--审批查看申请信息-->
    <el-row :gutter="10">
        <el-col :span="(oldId==null||oldId==''||oldId==undefined)?24:12">
            <span v-if="!(oldId==null||oldId==''||oldId==undefined)">当前信息</span>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #stopdate>
                    <el-date-picker v-model="applyTime" type="datetime" placeholder="选择时间" />
                </template>
                <template #openAuthDialog>
                    <el-button @click="openAuthDialog('1')">{{ isView ? "点击查看申请的权限范围信息" : "点击选择要申请的权限范围信息" }}</el-button>
                </template>
            </y9Form>
        </el-col>
        <el-col :span="(oldId==null||oldId==''||oldId==undefined)?24:12" v-if="!(oldId==null||oldId==''||oldId==undefined)">
            <span v-if="!(oldId==null||oldId==''||oldId==undefined)">变更前信息</span>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig2">
                <template #stopdate>
                    <el-date-picker v-model="applyTime" type="datetime" placeholder="选择时间" />
                </template>
                <template #openAuthDialog>
                    <el-button @click="openAuthDialog('2')">{{ isView ? "点击查看申请的权限范围信息" : "点击选择要申请的权限范围信息" }}</el-button>
                </template>
            </y9Form>
        </el-col>
    </el-row>

    <authDialog ref="authDialogRef" v-model:select-data="selectData" v-model:open-dialog="isOpen"
        v-model:interfaceId="props.interfaceId" v-model:isView="props.isView"></authDialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import {useInterfaceApply} from '@/api/interface/interface'
import authDialog from '@/views/approve/authDialog.vue';
import {getApplyInfoById} from '@/api/apply/apply';
import {getListByType,getListByPid} from '@/api/systemidentifier/systemidentifier'

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
const selectData = ref({})
const selectDataOld = ref({})
const selectDataNew = ref({})
const oldId = ref("")
const deptList = ref([])
const systemList = ref([])

const props = defineProps({
    openDialog:{
        type:Boolean,
        default:()=>false
    },
    isOpen:{
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
    },
    isLimitData:{
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
//instance
const rtDialogRef = ()=>{
    let obj = {
        y9RuleFormInstance:ruleFormRef.value?.elFormRef,
        authDialogRef:authDialogRef.value,
        ruleFormRef:ruleFormRef.value,
        selectData:selectData.value
    }
    return obj;
}

const submitData = ()=>{
    const y9RuleFormInstance = ruleFormRef.value?.elFormRef;
    y9RuleFormInstance.validate(async (valid) => {
        if (valid) {
            try {
                authDialogRef.value.getRuleForm().validate((valid) => {
                    if (!valid) {
                        ElMessageBox.alert("权限信息页面有必填项未填写，请打开页面确认！", "权限信息必填确认", {
                            confirmButtonText: '确认'
                        })
                        return false;
                    }
                })
            } catch (e) {
                ElMessageBox.alert("权限信息页面未选择，请打开页面确认！", "权限信息必填确认", {
                    confirmButtonText: '确认'
                })
                return false;
            }

            let data = ruleFormRef.value.model;
            let formData = new FormData();

            for (let key in data) {
                if (data[key] != null && key != "createTime" && key != "updateTime")
                    formData.append(key, data[key])
            }
            let auth={}
            //获取权限信息
            for (let key in selectData.value) {
                let valStr = ""
                for (let it of selectData.value[key]) {
                    valStr += it + ","
                }
                auth[key] = valStr.substring(0, valStr.length - 1)
            }
            formData.append("auth", JSON.stringify(auth))
            data.auth = JSON.stringify(auth)
            if (btnType.value == "停用") {

            } else {
                let res = await useInterfaceApply(
                    data
                )
                if (res.code == 0) {
                    if (res.status == "success") {
                        ElMessage({
                            message: '数据提交成功，等待审核，详细信息请移步到 个人中心>已申请接口 查看',
                            type: 'success',
                            duration: 6000
                        })
                    } else {
                        ElMessage({
                            message: '' + res.msg,
                            type: 'success'
                        })
                    }
                    emit('getDataListParent')
                    return true;
                }else{
                    return false
                }
            }
        } else {
            return false;
        }
    });
}
//校验手机号
const validatePhone = (rule: any, value: any, callback: any) => {
    let result = $validCheck('phone', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};
//动态写入表单项
const itemLists = []
let applyReasonItem = {
    type: 'textarea',
    label: computed(() => t('申请事由')),
    prop: 'applyReason',
    props: {
        disabled: props.isView,
        //文本域类型的属性
        rows: 3, //输入框行数,类型：number
        maxlength: 500
    }
}
let notesItem = {
    type: 'textarea',
    label: computed(() => t('备注')),
    prop: 'notes',
    props: {
        disabled: props.isView,
        //文本域类型的属性
        rows: 3,//输入框行数,类型：number
        maxlength: 254
    }
}
// let systemIdentifierItem = {
//     type: 'input',
//     label: computed(() => t('申请系统名称')),
//     prop: 'systemIdentifier',
//     props: {
//         disabled: props.isView,
//         maxlength: 254
//     }
// }
let systemIdentifierItem = {
    type: 'select',
    label: computed(() => t('申请系统名称')),
    prop: 'systemIdentifier',
    props: {
        disabled: props.isView,
        maxlength: 254,
        options: systemList.value,
    }
}
let openSlotItem = {
    type: 'slot',
    label: computed(() => t('权限信息申请')),
    props: {
        slotName: "openAuthDialog"
    }
}
let ipWhitelistItem = {
    type: 'textarea',
    label: computed(() => t('IP名单')),
    prop: 'ipWhitelist',
    props: {
        disabled: props.isView,
        placeholder: "多个IP使用英文逗号隔开，例如：132.1.168.11,132.1.168.12",
        //文本域类型的属性
        rows: 3, //输入框行数,类型：number
        maxlength: 100
    }
}
// let applyPersonDeptNameItem = {
//     type: 'input',
//     label: computed(() => t('接口调用单位名称')),
//     prop: 'applyPersonDeptName',
//     props: {
//         disabled: props.isView,
//         maxlength: 254
//     }
// }
let applyPersonDeptNameItem = {
    type: 'select',
    label: computed(() => t('接口调用单位名称')),
    prop: 'applyPersonDeptId',
    props: {
        disabled: props.isView,
        maxlength: 254,
        options: deptList.value,
    }
}
let usePersonResponsibleItem = {
    type: 'input',
    label: computed(() => t('接口调用责任人')),
    prop: 'usePersonResponsible',
    props: {
        disabled: props.isView,
        maxlength: 50
    }
}
let usePersonResponsiblePhoneItem = {
    type: 'input',
    label: computed(() => t('责任人联系方式')),
    prop: 'usePersonResponsiblePhone',
    props: {
        placeholder: "请输入11位手机号码",
        disabled: props.isView,
        maxlength: 11
    }
}
itemLists.push(applyReasonItem)
itemLists.push(notesItem)
itemLists.push(applyPersonDeptNameItem)
itemLists.push(systemIdentifierItem)
if (props.isLimitData == "是") {
    itemLists.push(openSlotItem)
}
itemLists.push(ipWhitelistItem)
itemLists.push(usePersonResponsibleItem)
itemLists.push(usePersonResponsiblePhoneItem)

// 申请表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        interfaceId:props.interfaceId,
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
        usePersonResponsiblePhone: [{ required: true, message: computed(() => t('责任人联系方式不能为空')), trigger: 'blur' }
        ,{ validator: validatePhone, trigger: 'blur' }
        ],
    },
    itemList: itemLists,
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
//第二个表单
let ruleFormConfig2 = ref({
    //表单配置
    model: {
        interfaceId:props.interfaceId,
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
        usePersonResponsiblePhone: [{ required: true, message: computed(() => t('责任人联系方式不能为空')), trigger: 'blur' }
        ,{ validator: validatePhone, trigger: 'blur' }
        ],
    },
    itemList: itemLists,
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
function openPubDialog(id,type){
    ruleFormConfig.value.model.interfaceId = id
    interfaceId.value = id
}
const openAuthDialog = (type)=>{
    if(type=="1"){
        selectData.value = selectDataNew.value
    }else{
        selectData.value = selectDataOld.value
    }
    isOpen.value = true
}
//赋值
const initFormData = (data)=>{
    let selectParameter = {
        parameterType: 0
    }
    getListByType(selectParameter).then((res) => {
        for (let it of res.data) {
            let item = {
                label: it.name,
                value: it.id
            }
            deptList.value.push(item)
        }
    })
    let para = {
        pid: data.applyPersonDeptId
    }
    getListByPid(para).then((res) => {
        systemList.value = []
        for (let it of res.data) {
            let item = {
                label: it.name,
                value: it.id
            }
            systemList.value.push(item)
        }
        for (let it of ruleFormConfig.value.itemList) {
            if (it.prop == 'systemIdentifier') {
                it.props.options = systemList.value
            }
        }
    })
    ruleFormConfig.value.model = data
    oldId.value=data.oldId
    let jsonData = JSON.parse(data.auth)
    for(let key in jsonData){
        selectDataNew.value[key] = jsonData[key].split(",")
    }
    if(oldId.value!=null&&oldId.value!=undefined&&oldId.value!=""){
        let para = {
            id:oldId.value
        }
        getApplyInfoById(para).then((response) => {
            let newItemLists = JSON.parse(JSON.stringify(ruleFormConfig.value.itemList))
            let para = {
                pid: data.applyPersonDeptId
            }
            getListByPid(para).then((res) => {
                let systemLists = []
                for (let it of res.data) {
                    let item = {
                        label: it.name,
                        value: it.id
                    }
                    systemLists.push(item)
                }
                for (let it of newItemLists) {
                    if (it.prop == 'systemIdentifier') {
                        it.props.options = systemLists
                    }
                }
                ruleFormConfig2.value.itemList = newItemLists
            })
            ruleFormConfig2.value.model = response.data
            let jsonData1 = JSON.parse(response.data.auth)
            for (let key in jsonData1) {
                selectDataOld.value[key] = jsonData1[key].split(",")
            }
        })
    }
}
//重置值
const resetFormData = ()=>{
    ruleFormConfig.value.model = {}
    selectData.value = {}
}
let selectParameter = {
    parameterType: 0
}
getListByType(selectParameter).then((res) => {
    for (let it of res.data) {
        let item = {
            label: it.name,
            value: it.id
        }
        deptList.value.push(item)
    }
})
defineExpose({openPubDialog,submitData,rtDialogRef,initFormData,resetFormData})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
</style>
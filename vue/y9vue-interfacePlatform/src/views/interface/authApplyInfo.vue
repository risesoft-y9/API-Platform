<template>
    <!--发布停用填写信息页面 -->
    <el-dialog v-model="openDialog" :title="Title" v-if="openDialog" @closed="closed">
        <el-scrollbar height="600px">
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #openAuthDialog>
                    <span>{{ ruleFormConfig.model.userSecret }}</span>
                    <el-button style="margin-left: 10px;" @click="downLoadSecretBtn">点击下载密钥</el-button>
                    <a style="display: none;" ref="secretRef"></a>
                </template>
            </y9Form>
            <el-divider content-position="left"><span style="font-size: 16px">申请信息</span></el-divider>
            <applyInfo ref="applyInfoRef" v-model:isView="isView" v-model:select-data="selectData"
                v-model:open-dialog="isOpen" v-model:interfaceId="interfaceId" v-model:isAuth="isAuthInfo"></applyInfo>
        </el-scrollbar>
        <template #footer>
            <el-button class="el-button el-button--primary el-button--default global-btn-main"
                @click="closeDialog">确定</el-button>
            <el-button @click="closeDialog">取消</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, ref, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import applyInfo from './applyInfo.vue';
import { getApplyInfoByInterfaceId } from '@/api/interface/interface'
import { downLoadSecret, getApplyInfoById } from '@/api/apply/apply'

// 注入 字体对象
const { t } = useI18n();
const isOpen = ref(false)
const interfaceId = ref()
const selectData = ref()
const applyInfoRef = ref()
const isView = ref(true)
const secretRef = ref()
const interfaceName = ref("")
const isAuthInfo = ref("是")

const props = defineProps({
    openDialog: {
        type: Boolean,
        default: () => false
    },
    isView: {
        type: Boolean,
        default: () => false
    },
    interfaceId: {
        type: String,
    },
    selectData: {
        type: String
    }
})
const openDialog = ref(false)
const Title = ref("查看申请信息")
const emit = defineEmits(['update:openDialog', 'update:selectData', 'getDataListParent'])

// 发布表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        interfaceId: props.interfaceId,
        applyStopTime: "",
        applyTime: ""
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
            type: 'input',
            label: computed(() => t('用户令牌')),
            prop: 'userKey',
            props: {
                disabled: true,
            }
        },
        {
            type: 'slot',
            label: computed(() => t('用户密钥')),
            props: {
                slotName: "openAuthDialog"
            }
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});


function openPubDialog(id, type, name, isAuth) {
    interfaceId.value = id
    openDialog.value = true
    debugger
    if (type == "申请") {
        let para = {
            id: id
        }
        getApplyInfoByInterfaceId(para).then((response) => {
            ruleFormConfig.value.model = response.data
            nextTick(() => {
                applyInfoRef.value.initFormData(response.data)
            })
        })
    } else {
        let para = {
            id: id
        }
        getApplyInfoById(para).then((response) => {
            ruleFormConfig.value.model = response.data
            interfaceId.value = response.data.interfaceId
            nextTick(() => {
                applyInfoRef.value.initFormData(response.data)
            })
        })
    }
    isAuthInfo.value = isAuth
    interfaceName.value = (name != undefined ? name : "")
}
//赋值
const initFormData = (data) => {
    openDialog.value = true
    interfaceId.value = data.interfaceId
    nextTick(() => {
        applyInfoRef.value.initFormData(data)
    })
}
//关闭配置信息
function closeDialog() {
    openDialog.value = false
}
//关闭后调用
function closed() {
    applyInfoRef.value.resetFormData()
}
//下载密钥
const downLoadSecretBtn = () => {
    let param = {
        id: ruleFormConfig.value.model.id
    }
    downLoadSecret(param).then((res) => {
        console.log(res)
        const a = document.createElement('a')
        a.href = URL.createObjectURL(res)
        a.download = interfaceName.value + '_密钥.txt'
        a.click()
    })
}
defineExpose({ openPubDialog, initFormData })
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
</style>
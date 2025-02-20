<template>
    <!--发布停用填写信息页面 -->
    <y9Dialog v-model:config="addDialogConfig" v-loading="loading">
        <template v-slot>
            <applyInfo ref="applyInfoRef" v-model:isView="isView" v-model:select-data="selectData"
                v-model:open-dialog="isOpen" v-model:interfaceId="interfaceId" v-model:isAuth="isAuthInfo"></applyInfo>
        </template>
    </y9Dialog>
</template>
<script lang="ts" setup>
import { computed, ref, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { ElMessage, ElMessageBox } from 'element-plus';
import applyInfo from './applyInfo.vue';
import { getApplyInfoByInterfaceId } from '@/api/interface/interface'
import {useInterfaceApply} from '@/api/approve/approve'
import { getSelectUserFlowNode } from '@/api/flownode/flownode'

// 注入 字体对象
const { t } = useI18n();
const isOpen = ref(false)
const interfaceId = ref()
const selectData = ref()
const applyInfoRef = ref()
const approveStatus = ref()
const isView = ref(false)
const isAuthInfo = ref("否")
const loading = ref(false)
const APPLY_FLOW_ID = '33b87d685087448e8bb27ec7d155ee70'

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
const emit = defineEmits(['update:openDialog', 'update:selectData', 'getDataListParent'])

// 增加 修改应用 弹框的变量配置 控制
let addDialogConfig = ref({
    show: props.openDialog,
    title: computed(() => t('接口调用申请')),
    showFooter: true,
    onOkLoading: true,
    margin: '10vh auto',
    okText: '提交',
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            if (approveStatus.value != "通过") {
                let rtData = applyInfoRef.value.rtDialogRef();
                const y9RuleFormInstance = rtData.y9RuleFormInstance;
                let data = rtData.ruleFormRef.model;
                y9RuleFormInstance.validate(async (valid) => {
                    if (valid) {
                        loading.value = true
                        try {
                            if (isAuthInfo.value == "是") {
                                rtData.authDialogRef.getRuleForm().validate((valid) => {
                                    if (!valid) {
                                        ElMessageBox.alert("权限信息页面有必填项未填写，请打开页面确认！", "权限信息必填确认", {
                                            confirmButtonText: '确认'
                                        })
                                        return
                                    }
                                })
                            }
                        } catch (e) {
                            ElMessageBox.alert("权限信息页面未选择，请打开页面确认！", "权限信息必填确认", {
                                confirmButtonText: '确认'
                            })
                            return
                        }

                        let data = rtData.ruleFormRef.model;
                        let formData = new FormData();

                        for (let key in data) {
                            if (data[key] != null && key != "createTime" && key != "updateTime")
                                formData.append(key, data[key])
                        }
                        formData.delete("auth")
                        let auth = {}
                        //获取权限信息
                        for (let key in rtData.selectData) {
                            let valStr = ""
                            for (let it of rtData.selectData[key]) {
                                valStr += it + ","
                            }
                            auth[key] = valStr.substring(0, valStr.length - 1)
                        }
                        formData.append("auth", JSON.stringify(auth))
                        data.auth = JSON.stringify(auth)

                        useInterfaceApply(data).then((res) => {
                            if (res.code == 0) {
                                if (res.status == "success") {
                                    ElMessage({
                                        message: '流程提交成功，等待审核，详细信息请移步到 个人中心>已申请接口 查看',
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
                                addDialogConfig.value.show = false
                                loading.value = false
                            } else {
                                loading.value = false
                            }
                        })

                    } else {
                        reject()
                        loading.value = false
                    }
                });
            } else {
                ElMessageBox.confirm(
                    '是否想要变更申请信息，当前接口调用申请已经审批通过，点击“保存并提交”后需要重新审批',
                    '重新申请确认',
                    {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'info',
                        draggable: true
                    }
                ).then(() => {
                    let rtData = applyInfoRef.value.rtDialogRef();
                    const y9RuleFormInstance = rtData.y9RuleFormInstance;
                    let data = rtData.ruleFormRef.model;
                    y9RuleFormInstance.validate(async (valid) => {
                        if (valid) {
                            loading.value = true

                            try {
                                if (isAuthInfo.value == "是") {
                                    rtData.authDialogRef.getRuleForm().validate((valid) => {
                                        if (!valid) {
                                            ElMessageBox.alert("权限信息页面有必填项未填写，请打开页面确认！", "权限信息必填确认", {
                                                confirmButtonText: '确认'
                                            })
                                            reject()
                                            loading.value = false
                                            return
                                        }
                                    })
                                }
                            } catch (e) {
                                ElMessageBox.alert("权限信息页面未选择，请打开页面确认！", "权限信息必填确认", {
                                    confirmButtonText: '确认'
                                })
                                reject()
                                loading.value = false
                                return
                            }

                            let formData = new FormData();

                            for (let key in data) {
                                if (data[key] != null && key != "createTime" && key != "updateTime")
                                    formData.append(key, data[key])
                            }
                            formData.delete("auth")
                            let auth = {}
                            //获取权限信息
                            for (let key in rtData.selectData) {
                                let valStr = ""
                                for (let it of rtData.selectData[key]) {
                                    valStr += it + ","
                                }
                                auth[key] = valStr.substring(0, valStr.length - 1)
                            }
                            formData.append("auth", JSON.stringify(auth))
                            data.auth = JSON.stringify(auth)

                            useInterfaceApply(data).then((res) => {
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
                                    resolve()
                                    loading.value = false
                                } else {
                                    reject()
                                    loading.value = false
                                }
                            })
                        } else {
                            reject()
                            loading.value = false
                        }
                    });
                }).catch(() => {
                    reject()
                    loading.value = false
                })
            }

        });
    },
    visibleChange: (visible) => {
        emit("update:openDialog", visible)
        if (!visible) {
            try {
                applyInfoRef.value.restData()
            } catch (e) {

            }
        }
    }
});


function openPubDialog(id, type, status, isAuth) {
    interfaceId.value = id
    addDialogConfig.value.show = true
    if (type == "申请") {
        let para = {
            id: id
        }
        getApplyInfoByInterfaceId(para).then((response) => {
            nextTick(() => {
                response.data.oldId = response.data.id
                applyInfoRef.value.initFormData(response.data)
            })
        })
    }
    isAuthInfo.value = isAuth
    approveStatus.value = status
    console.log(approveStatus.value)
}

//赋值
const initFormData = (data) => {
    addDialogConfig.value.show = true
    nextTick(() => {
        isView.value = true
        applyInfoRef.value.initFormData(data)
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
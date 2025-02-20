<template>
    <!--发布停用填写信息页面 -->
    <y9Dialog v-model:config="addDialogConfig" v-loading="loading">
        <template v-slot>
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #stopdate>
                    <el-date-picker v-model="applyStopTime" value-format="YYYY-MM-DD HH:mm:ss" type="datetime"
                        placeholder="选择时间" :disabled-date="pickerOptions" :show-now="false"/>
                </template>
            </y9Form>
        </template>
    </y9Dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { pubInterface, stopInterface } from '@/api/approve/approve';


// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const authRef = ref()
const ruleFormRef = ref()
const btnType = ref("")
const applyStopTime = ref("")
const loading = ref(false)
const flowOptions = ref([])
const PUB_FLOW_ID = '791e592e161f4532be630672985e8557'
// const PUB_FLOW_ID = '379412392b424fd3b4c413f22b2b8cca'

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
    title: computed(() => t('发布接口信息')),
    showFooter: true,
    onOkLoading: true,
    okText: "送审",
    onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
            const y9RuleFormInstance = ruleFormRef.value?.elFormRef;
            await y9RuleFormInstance.validate(async (valid) => {
                if (valid) {
                    loading.value = true
                    let data = ruleFormRef.value.model;
                    data.applyStopTime = applyStopTime.value;
                    let formData = new FormData();
                    for (let key in data) {
                        if (data[key] != null && key != "createTime" && key != "updateTime")
                            formData.append(key, data[key])
                    }
                    if (btnType.value == "停用") {
                        stopInterface(data).then((res) => {
                            if (res.code == 0) {
                                if (res.status == "success") {
                                    ElMessage({
                                        message: '数据提交成功，等待审核',
                                        type: 'success'
                                    })
                                } else {
                                    ElMessage({
                                        message: '' + res.msg,
                                        type: 'success'
                                    })
                                }
                                addDialogConfig.value.show = false
                                emit('getDataListParent')
                            }
                        })

                    } else {
                        pubInterface(data).then((res) => {
                            if (res.code == 0) {
                                if (res.status == "success") {
                                    ElMessage({
                                        message: '流程提交成功',
                                        type: 'success'
                                    })
                                } else {
                                    ElMessage({
                                        message: '' + res.msg,
                                        type: 'success'
                                    })
                                }
                                addDialogConfig.value.show = false
                                emit('getDataListParent')
                            }
                        })
                    }
                    reject();
                    loading.value = false


                } else {
                    reject();
                    loading.value = false
                }
            });
        });
    },
    visibleChange: (visible) => {
        emit("update:openDialog", visible)
    }
});

// 发布表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        interfaceId: "",
        applyStopTime: ""
    },
    rules: {
        //	表单验证规则。类型：FormRules
        applyReason: [{ required: true, message: computed(() => t('事由不能为空')), trigger: 'blur' }],
    },
    itemList: [
        {
            type: 'textarea',
            label: computed(() => t('事由')),
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
        {
            type: 'slot',
            label: computed(() => t('申请停用时间')),
            prop: 'applyStopTime',
            props: {
                slotName: "stopdate"
            }
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});
function openPubDialog(id, type) {
    let isAdd = true
    ruleFormConfig.value.model.interfaceId = id
    if (type == "停用") {
        addDialogConfig.value.title = computed(() => t('停用接口信息'))
        let newData = []
        let itData = {
            type: 'slot',
            label: computed(() => t('申请停用时间')),
            prop: 'applyStopTime',
            props: {
                slotName: "stopdate"
            }
        }
        let isAddTime = true;
        for (let it of ruleFormConfig.value.itemList) {
            if (it.prop == 'applyStopTime') {
                isAddTime = false
            }
            newData.push(it)
        }
        if (isAddTime) {
            newData.push(itData)
        }
        ruleFormConfig.value.itemList = newData;
    } else if (type == "发布") {
        addDialogConfig.value.title = computed(() => t('发布接口信息'))
        let newData = []
        for (let it of ruleFormConfig.value.itemList) {
            if (it.prop != 'applyStopTime') {
                newData.push(it)
            }
        }
        ruleFormConfig.value.itemList = newData;
    }

    btnType.value = type
    addDialogConfig.value.show = true
}
const pickerOptions = (time)=>{
    return time.getTime() < Date.now()
}
defineExpose({ openPubDialog })
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
</style>
<template>
    <!--更改节点顺序 -->
    <el-dialog v-model="openAdd" :title="title" v-if="openAdd" :close-on-click-modal="false"
        :close-on-press-escape="false">
        <el-divider />
        <y9VxeTable ref="editTableSortRef" :config="flowNodeTable">
 
        </y9VxeTable>
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
import { $validCheck } from '@/utils/validate'
import { getFlowNode, updateFlowNodeSortBatch } from '@/api/flownode/flownode'
import { ElMessage, ElMessageBox } from 'element-plus';

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const query: any = ref({});
const sameId = ref()
const openAdd = ref(false)
const title = ref("节点顺序更改")
const editTableSortRef = ref()
const emit = defineEmits(["getDataListParent"])

const validateNumber = (data) => {
    let result = $validCheck('number', data.cellValue, true);
    if (!result.valid) {
        return result.msg;
    } else {
        return true;
    }
};
//请求参数table配置
const flowNodeTable = ref({
    headerBackground: true,
    pageConfig: false,
    editConfig:{
        trigger: 'click',
        enable:true,
        mode:"cell"
    },
    height: 500,
    keepSource: true,
    columns: [
        {
            title: computed(() => t('节点名称')),
            key: 'nodeName'
        },
        {
            title: computed(() => t('排序')),
            key: 'sort',
            editRender:{
                name:"input",
                useElement: true,
                immediate:true,
                rules:[{ required: true, message: computed(() => t('是否必填不能为空')), trigger: 'blur' },
                { validator: validateNumber, trigger: 'blur' }]
            }
        },
    ],
    tableData: [],
})

//获取已接入接口列表
async function getDataList() {
    flowNodeTable.value.loading = true;
    query.value.sameId = sameId.value;
    let res = await getFlowNode(
        query.value
    )
    flowNodeTable.value.tableData = res.data || []
    flowNodeTable.value.loading = false;
}

//初始化table数据
function initTableData(id) {
    sameId.value = id
    openAdd.value = true
    getDataList()
}
//弹出提示信息
function openPromptDialog(title, msg) {
    ElMessageBox.alert(msg, title, {
        confirmButtonText: '确认'
    })
}
const closeDialog = ()=>{
    openAdd.value = false
}
const submitData = ()=>{
    console.log(editTableSortRef.value.vxeTableRef.getRecordset().updateRecords)
    let data = []
    for(let it of editTableSortRef.value.vxeTableRef.getRecordset().updateRecords){
        let result = $validCheck('number', it.sort, true);
        if (!result.valid){
            openPromptDialog("数据格式确认","排序只可以输入正整数且不能为空")
            return
        }
        let item = {
            id:it.id,
            sort:it.sort
        }
        data.push(item)
    }
    if(data.length!=0){
        let formData = new FormData();
        formData.append("flowNodes", JSON.stringify(data))
        updateFlowNodeSortBatch(formData).then((res) => {
            if (res.status == "success") {
                ElMessage({
                    message: '修改成功',
                    type: 'success',
                    duration: 3000
                })
                openAdd.value = false
            } else {
                ElMessage({
                    message: '' + res.msg,
                    type: 'success'
                })
                return
            }
            emit('getDataListParent')
        })
    }
}
defineExpose({
    initTableData
})
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
</style>
<template>
    <el-scrollbar :height="props.maxHeight">
        <span>根节点</span>
        <el-tree 
        :data="dataSource"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        >
        <template #default="{node,data}">
            <el-row style="width: 80vh;" :gutter="10">
                <el-col :span="3" v-if="node.level==1||(node.parent.data.parameterType!=undefined && node.parent.data.parameterType!='array')">
                    <el-input v-model="node.data.parameterKey" placeholder="字段名">
                        <template #prefix><i class="ri-asterisk" style="color: #f56c6c;font-size: 1px;"></i></template>
                    </el-input>
                </el-col>
                <el-col :span="3" v-if="node.parent.data.parameterType!=undefined && node.parent.data.parameterType=='array'">
                    <span style="color: #409eff;">items</span>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="node.data.parameterType" placeholder="参数类型" @change="(e) => selectChange(e,node,data)">
                        <el-option label="String" value="String"></el-option>
                        <el-option label="integer" value="integer"></el-option>
                        <el-option label="boolean" value="boolean"></el-option>
                        <el-option label="array" value="array"></el-option>
                        <el-option label="map" value="map"></el-option>
                        <el-option label="number" value="number"></el-option>
                        <template #prefix><i class="ri-asterisk" style="color: #f56c6c;font-size: 1px;"></i></template>
                    </el-select>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="node.data.required" placeholder="是否必填">
                        <el-option label="是" value="是"></el-option>
                        <el-option label="否" value="否"></el-option>
                        <template #prefix><i class="ri-asterisk" style="color: #f56c6c;font-size: 1px;"></i></template>
                    </el-select>
                </el-col>
                <el-col :span="3">
                    <el-input v-model="node.data.defaultVal" placeholder="默认值">
                    </el-input>
                </el-col>
                <el-col :span="3">
                    <el-input v-model="node.data.notes" placeholder="参数描述">
                    </el-input>
                </el-col>
                <el-col :span="7"><a @click="append(node,data)"><i class="ri-add-line" style="color: #67c23a;"></i></a><a @click="remove(node,data)"><i class="ri-delete-bin-5-line" style="color: #f56c6c;"></i></a></el-col>
            </el-row>
        </template>
        </el-tree>
    </el-scrollbar>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useI18n } from 'vue-i18n';
import type Node from 'element-plus/es/components/tree/src/model/node'

interface Tree{
    id:number,
    label:string,
    children?:Tree[]
}
// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const settingStore = useSettingStore();
const { t } = useI18n();
const authRef = ref()
const ruleFormRef = ref()
const btnType = ref("")

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
        type:String
    },
    maxHeight:{
        type:String,
        default:()=>"200px"
    }
})
const value = ref()
const emit = defineEmits(['update:openDialog','update:selectData'])

const dataSource = ref<Tree[]>([
    {
        id: 1,
        label: "测试",
        parameterType:"String",
        children: [{
            id: 2,
            label: "测试2",
            parameterType:"String",
        },
        {
            id: 3,
            label: "测试3",
            parameterType:"String",
        }
        ]
    }
])
let id = 1000
const append = (node,data)=>{
    let newChild = {id:id++,label:"",parameterType:"String",required:"否"}
    if(!data.children){
        data.children = []
    }
    data.children.push(newChild)
    dataSource.value = [...dataSource.value]
}
const remove = (node,data)=>{
    const parent = node.parent;
    const children:Tree[] = parent.data.children || parent.data;
    const index = children.findIndex((d)=> d.id === data.id)
    children.splice(index,1)
    dataSource.value = [...dataSource.value]
}
const selectChange = (e,node,data)=>{
    console.log(e)
    console.log(node)
    console.log(data)
}
defineExpose({})
</script>
<style scoped>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.el-input__wrapper{
    border:none !important;
    box-shadow: none !important;
    /* border-bottom: 1px #dcdfe6 solid !important; */
}
.el-select__wrapper{
    border:none !important;
    box-shadow: none !important;
    /* border-bottom: 1px #dcdfe6 solid !important; */
}
.el-tree {
    --el-tree-node-content-height: 35px;}
</style>
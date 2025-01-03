<!--
 * @Author: duanzhixin
 * @Date: 2024-09-06 17:31:19
 * @Description: 选人组件
-->
<template>
    <div>
        <el-row>
            <el-col :span="10">
                <el-card>
                    <template #header>人员列表</template>
                    <el-scrollbar :height="props.maxHeight">
                        <el-tree ref="selectUserRef" :data="dataSource" show-checkbox node-key="id" default-expand-all
                            :expand-on-click-node="false"></el-tree></el-scrollbar>
                </el-card>
            </el-col>
            <el-col :span="4">
                <el-row class="rowHeigth">
                    <el-col :span="12">
                        <div class="content">
                            <el-button @click="delCheckedData()">
                                <</el-button>
                        </div>
                    </el-col>
                    <el-col :span="12">
                        <div class="content">
                            <el-button @click="checkedUser()">></el-button>
                        </div>
                    </el-col>
                </el-row>
            </el-col>
            <el-col :span="10">
                <el-card>
                    <template #header>
                        <el-row style="margin-bottom:0px!important">
                            <el-col :span="23">
                                <span>选中人员</span>
                            </el-col>
                            <el-col :span="1">
                                <el-tooltip content="清空" effect="light" placement="top">
                                    <el-icon style="cursor: pointer;" @click="clearUserData">
                                        <CircleClose></CircleClose>
                                    </el-icon>
                                </el-tooltip>
                            </el-col>
                        </el-row>
                    </template>
                    <el-scrollbar :height="props.maxHeight">
                        <el-tree ref="checkedUserRef" :data="checkedArray" show-checkbox node-key="id"
                            default-expand-all :expand-on-click-node="false"></el-tree></el-scrollbar>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import type Node from 'element-plus/es/components/tree/src/model/node'
import {getSelectUserList} from '@/api/flownode/flownode'

const emit = defineEmits(['updateSelectData'])
const selectUserRef = ref()
const checkedUserRef = ref()
interface Tree {
    id: number,
    label: string,
    children?: Tree[]
}
const props = defineProps({
    maxHeight: {
        type: String,
        default: () => '400px'
    },
    selectUserNum: {
        type: Number,
        default: () => 1
    },
})
const dataSource = ref<Tree[]>([])
const checkedArray = ref([])
const checkedUser = () => {
    let array = selectUserRef.value.getCheckedNodes()
    let map = new Map()
    for (let it of checkedArray.value) {
        map.set(it.id, "1")
    }
    for (let it of array) {
        if (it.type == "user" && map.get(it.id) != "1") {
            let copyData = JSON.parse(JSON.stringify(it))
            checkedArray.value.push(copyData)
            it.disabled = true
        }
        selectUserRef.value.setChecked(it, false, false)
    }
    emit('updateSelectData')
}
//递归遍历数据
function selectChild(child, allDataMap) {
    if (child != undefined) {
        for (let it of child) {
            if (it.children instanceof Array) {
                selectChild(it.children, allDataMap)
            }
            allDataMap.set(it.id, it)
        }
    }
}

//删除选中人员
const delCheckedData = () => {
    let allArrayDataMap = new Map()
    for (let it of dataSource.value) {
        allArrayDataMap.set(it.id, it)
        if (it.children != null && it.children != undefined && it.children.length != 0) {
            selectChild(it.children, allArrayDataMap)
        }
    }
    let array = checkedUserRef.value.getCheckedNodes()
    let map = new Map()
    for (let user of array) {
        map.set(user.id, "1")
    }
    let newArray = []
    for (let it of checkedArray.value) {
        if (map.get(it.id) != "1") {
            newArray.push(it)
        } else {
            allArrayDataMap.get(it.id).disabled = false
        }
    }
    checkedArray.value = newArray
    emit('updateSelectData')
}
//赋值选中人员data为人员列表,checkedIds为选中人员的id数组
const initData = (data, checkedIds) => {
    if (data == null||data==undefined||data.length==0) {
        let para = {}
        getSelectUserList(para).then((res) => {
            if(res.code=="0"){
                dataSource.value = res.userData
            }else{
                dataSource.value = []
            }
            let allArrayDataMap = new Map()
            for (let it of dataSource.value) {
                allArrayDataMap.set(it.id, it)
                if (it.children != null && it.children != undefined && it.children.length != 0) {
                    selectChild(it.children, allArrayDataMap)
                }
            }
            checkedArray.value = []
            for (let it of checkedIds) {
                if (allArrayDataMap.get(it) != undefined && allArrayDataMap.get(it) != null && allArrayDataMap.get(it) != "") {
                    let copyData = JSON.parse(JSON.stringify(allArrayDataMap.get(it)))
                    checkedArray.value.push(copyData)
                    allArrayDataMap.get(it).disabled = true
                }
            }
        })
    } else {
        dataSource.value = data
        let allArrayDataMap = new Map()
        for (let it of dataSource.value) {
            allArrayDataMap.set(it.id, it)
            if (it.children != null && it.children != undefined && it.children.length != 0) {
                selectChild(it.children, allArrayDataMap)
            }
        }
        checkedArray.value = []
        for (let it of checkedIds) {
            if (allArrayDataMap.get(it) != undefined && allArrayDataMap.get(it) != null && allArrayDataMap.get(it) != "") {
                let copyData = JSON.parse(JSON.stringify(allArrayDataMap.get(it)))
                checkedArray.value.push(copyData)
                allArrayDataMap.get(it).disabled = true
            }
        }
    }
}
//获取选中人员
const getCheckedUser = () => {
    return checkedArray.value
}
//清空人员数据
const clearUserData = ()=>{
    let allArrayDataMap = new Map()
    for (let it of dataSource.value) {
        allArrayDataMap.set(it.id, it)
        if (it.children != null && it.children != undefined && it.children.length != 0) {
            selectChild(it.children, allArrayDataMap)
        }
    }
    let array = checkedArray.value
    let map = new Map()
    for (let user of array) {
        map.set(user.id, "1")
    }
    let newArray = []
    for (let it of checkedArray.value) {
        if (map.get(it.id) != "1") {
            newArray.push(it)
        } else {
            allArrayDataMap.get(it.id).disabled = false
        }
    }
    checkedArray.value = newArray
    emit('updateSelectData')
}
defineExpose({ getCheckedUser, initData })
</script>
<style lang="scss" scoped>
.content {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
}

.btn {
    background-color: #409eff;
}

.rowHeigth {
    height: 100%;
}
</style>

<template>
    <!-- 注册接口信息 -->
    <div class="windowBlank" :style="{height:screenHeight +'px'}">
        <testDialog :isWindow="'true'"></testDialog>
    </div>
</template>
<script lang="ts" setup>
import testDialog from './testDialog.vue';
import { computed, h, ref, inject, nextTick } from 'vue';
const screenHeight = ref(window.innerHeight - 58)
console.log(screenHeight.value)
</script>
<style scoped>
.windowBlank{
    padding: 29px 35px 29px 35px;
}
</style>
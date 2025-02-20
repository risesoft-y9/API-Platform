<template>
    <!-- 注册接口信息 -->
    <div v-loading="loading" style="padding: 15px;background-color: #ffffff" element-loading-text="加载中...">
        <el-row :gutter="20">
            <el-col :span="20"></el-col>
            <el-col :span="4">
                <div class="btnDiv">
                    <el-button v-if="isView" style="margin-bottom: 10px;"
                        class="el-button el-button--primary el-button--default global-btn-main" @click="submitData()">
                        <i class="ri-send-plane-line"></i>
                        <span>{{ $t('提交') }}</span>
                    </el-button>
                    <el-button v-if="isView" style="margin-bottom: 10px;"
                        class="el-button el-button--default global-btn-third" @click="resetData()">
                        <i class="ri-refresh-line"></i>
                        <span>{{ $t('重置') }}</span>
                    </el-button>
                </div>
            </el-col>
        </el-row>
        <el-scrollbar height="73vh">
            <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                <template #isLimit>
                    <el-switch :disabled="!isView" v-model="value1" active-text="是"
                        inactive-text="否"></el-switch><el-tooltip v-if="value1" effect="light" content="点击配置限流信息"
                        placement="top-start"><el-icon class="operate" @click="openView('1')">
                            <Warning></Warning>
                        </el-icon></el-tooltip>
                </template>
                <template #isAuth>
                    <el-switch :disabled="!isView" v-model="value3" active-text="是" inactive-text="否"></el-switch>
                </template>
                <template #isLimitData>
                    <el-switch :disabled="!isView" v-model="value2" active-text="是" inactive-text="否"
                        @click="ctlDataAuth"></el-switch>
                </template>
                <template #interfaceType>
                    <el-radio-group v-model="interfaceType" :disabled="!isView">
                        <el-radio-button label="Rest" value="Rest" />
                        <el-radio-button label="webService" value="webService" @click="openWebServiceDialog()" />
                    </el-radio-group>
                </template>
                <template #network>
                    <el-select v-model="network" disabled>
                        <el-option :label="'http'" :value="'http'"></el-option>
                        <el-option :label="'https'" :value="'https'"></el-option>
                    </el-select>
                </template>
                <template #isOverwrite>
                    <el-radio-group v-model="isOverwrite"
                        @change="(val) => { if (val == 'Y') { openPromptDialog('覆盖更新确认', '覆盖更新将会在接口审批通过后替换原接口') } }">
                        <el-radio-button label="否" value="N" />
                        <el-radio-button label="是" value="Y" />
                    </el-radio-group>
                </template>
                <template #parameterHeader>
                    <y9VxeTable ref="editRequestHeaderRef" :config="requestHeaderParameterTable"
                        :filterConfig="requestHeaderParameterTableFilter" v-model:selectedVal="headerSelectedCheckboxVal">
                        <template v-slot:slotBtns>
                            <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                                @click="addParameter('1')">
                                <i class="ri-add-line"></i>
                                <span>{{ $t('新增') }}</span>
                            </el-button>
                            <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }" class="el-button el-button--default global-btn-third" type="primary"
                                @click="delParameter('1')">
                                <i class="ri-delete-bin-line"></i>
                                <span>{{ $t('删除') }}</span>
                            </el-button>
                        </template>
                    </y9VxeTable>
                </template>
                <template #parameter>
                    <el-divider content-position="left" v-if="props.isShow">请求参数列表</el-divider>
                    <y9VxeTable v-if="reqMethod == 'get' || interfaceType != 'Rest'" ref="editRequestRef"
                        :config="requestParameterTable" :filterConfig="requestHeaderParameterTableFilter" v-model:selectedVal="requestSelectedCheckboxVal">
                        <template v-slot:slotBtns>
                            <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary"
                                @click="addParameter('2')">
                                <i class="ri-add-line"></i>
                                <span>{{ $t('新增') }}</span>
                            </el-button>
                            <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }" class="el-button el-button--default global-btn-third"
                                @click="delParameter('2')">
                                <i class="ri-delete-bin-line"></i>
                                <span>{{ $t('删除') }}</span>
                            </el-button>
                        </template>
                    </y9VxeTable>
                    <parameter v-if="reqMethod == 'post' && interfaceType == 'Rest'" ref="requestParameterRef"
                        v-model:parameterStatus="requestStatus" v-model:data="reqData" v-model:isView="isView">
                    </parameter>
                    <el-divider content-position="left" v-if="value2">数据请求范围（<span
                            style="color:red">此处选中的参数，需要在上方请求参数列表出现。不可选择相同参数key的参数</span>）</el-divider>
                    <interfaceAuth v-if="value2" v-model:isView="isView" v-model:isShow="props.isShow"
                        v-model:isDisabled="value2" ref="authRef" :interfaceId="interfaceId"
                        v-model:selectData="selectData" />
                </template>
                <template #parameterResponse>
                    <parameter ref="responseParameterRef" v-model:parameterStatus="responseStatus"
                        v-model:data="resData" v-model:isView="isView" ></parameter>
                </template>
            </y9Form>
        </el-scrollbar>

    </div>


    <!-- 限流信息 -->
    <el-dialog v-model="limitInfo" :title="limitTitle">
        <el-divider />
        <el-form class="limitClass" ref="limitInfoRef" :model="limitInfoForm" label-width="140px"
            :rules="limitInfoRule">
            <el-form-item label="阈值类型" prop="thresholdType">
                <el-radio-group v-model="limitInfoForm.thresholdType" :disabled="!isView">
                    <el-radio value="0">自定义</el-radio>
                    <el-radio value="1">QPS</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="限定时间" v-if="limitInfoForm.thresholdType == 0" prop="limitTime">
                <el-input v-model="limitInfoForm.limitTime" :disabled="!isView" placeholder="请输入，单位：秒" maxlength="7">
                    <template #append>秒</template>
                </el-input>
            </el-form-item>
            <el-form-item label="限定时间内访问量" v-if="limitInfoForm.thresholdType == 0" prop="limitCount">
                <el-input v-model="limitInfoForm.limitCount" :disabled="!isView" placeholder="请输入次数" maxlength="7">
                    <template #append>次数</template>
                </el-input>
            </el-form-item>
            <el-form-item label="阈值" v-if="limitInfoForm.thresholdType == 1" prop="thresholdVal">
                <el-input v-model="limitInfoForm.thresholdVal" :disabled="!isView" placeholder="请输入" maxlength="7">
                    <template #append>次数/秒</template>
                </el-input>
            </el-form-item>
            <el-form-item label="流控效果" prop="effect">
                <el-radio-group v-model="limitInfoForm.effect" :disabled="!isView">
                    <el-radio value="1">快速失败</el-radio>
                    <el-radio value="3">排队等候</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="阻塞时间" v-if="limitInfoForm.effect == 3" prop="waitTime">
                <el-input v-model="limitInfoForm.waitTime" :disabled="!isView" placeholder="请输入，单位：毫秒" maxlength="9">
                    <template #append>毫秒</template>
                </el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button v-if="isView" class="el-button el-button--primary el-button--default global-btn-main"
                @click="confirDialog('1')">确定</el-button>
            <el-button @click="closeDialog('1')">取消</el-button>
        </template>
    </el-dialog>

    <!-- webService -->
    <el-dialog v-model="webService" :title="webServiceTitle">
        <el-divider />
        <el-form ref="webServiceRef" :model="webServiceForm" label-width="155px" :rules="webServiceRule">
            <el-form-item label="webService规范协议" prop="webSpecification">
                <el-select v-model="webServiceForm.webSpecification" placeholder="请选择webService规范协议"
                    :disabled="!isView">
                    <el-option label="JAX-WS" value="JAX-WS"></el-option>
                    <el-option label="JAX-RS" value="JAX-RS"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="命名空间" prop="nameSpace" v-if="webServiceForm.webSpecification=='JAX-WS'">
                <el-input v-model="webServiceForm.nameSpace" :disabled="!isView" placeholder="请输入命名空间"
                    maxlength="300" />
            </el-form-item>
            <el-form-item label="调用方法名" prop="method" v-if="webServiceForm.webSpecification=='JAX-WS'">
                <el-input v-model="webServiceForm.method" :disabled="!isView" placeholder="请输入调用方法名" maxlength="300" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button v-if="isView" class="el-button el-button--primary el-button--default global-btn-main"
                @click="confirDialog('2')">确定</el-button>
            <el-button @click="closeDialog('2')">取消</el-button>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { computed, h, ref, inject } from 'vue';
import { useSettingStore } from '@/store/modules/settingStore';
import { useI18n } from 'vue-i18n';
import { $validCheck } from '@/utils/validate'
import {saveUpdateVersionInfo,saveInterfaceInfo,getInterfaceId,getInterfaceInfoById,getRegisterNum} from '@/api/interface/interface'
import {getListByType,getListByPid} from '@/api/systemidentifier/systemidentifier'
import { ElMessage, ElMessageBox } from 'element-plus';
import interfaceAuth from '@/views/auth/interfaceAuth.vue';
import parameter from '../parameter/parameterTable.vue';
import { useRoute } from 'vue-router';
import { nextTick } from 'vue';

//
const exceuteInstanceInfo = ref([])
// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const loading = ref(false)
const settingStore = useSettingStore();
const route = useRoute();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const requestSelectedCheckboxVal = ref([])
const headerSelectedCheckboxVal = ref([])
const editRequestHeaderRef = ref();
const editRequestRef = ref();
const editResponseRef = ref();
const limitInfoRef = ref();
const webServiceRef = ref();
const webService = ref(false);
const webServiceTitle = ref("webService信息补充");
const limitInfo = ref(false)
const limitTitle = ref("配置限流信息")
const openDialog = ref(false)
const interfaceId = ref()
const value1 = ref('false')
const value2 = ref(false)
const value3 = ref('false')
const selectData = ref()
const isView = ref(true)
const discardInterfaceId = ref("0")
const saveType = ref("0")
const isDelData = ref(true)
const authRef = ref()
const responseStatus = ref("3")
const responseParameterRef = ref()
const requestStatus = ref("2")
const requestParameterRef = ref()
const interfaceType = ref("Rest")
const isOverwrite = ref("N")
const resData = ref()
const reqData = ref()
const reqMethod = ref("get")
const router = useRouter();
const network = ref("");
const deptList = ref([])
const systemList = ref([])
const emit = defineEmits(['getDataListParent'])
const limitInfoForm = ref({
    thresholdType:"0",
    limitTime:"",
    limitCount:"",
    thresholdVal:"",
    effect:"1",
    waitTime:""
})
const props = defineProps({
    status:{
        type:String,
        default:()=>"其他"
    },
    isShow:{
        type:Boolean,
        default:()=>true
    }
})
//校验数字格式
const validateNumber = (rule: any, value: any, callback: any) => {
    let result = $validCheck('number', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};
const oldLimitInfoForm = ref()
const limitInfoRule = ref({
        //	表单验证规则。类型：FormRules
        thresholdType: [{ required: true, message: computed(() => t('阈值类型不能为空')), trigger: 'blur' }],
        limitTime: [{ required: true, message: computed(() => t('限定时间不能为空')), trigger: 'blur' }
            ,{ validator: validateNumber, trigger: 'blur' }
        ],
        limitCount: [{ required: true, message: computed(() => t('限定访问量不能为空')), trigger: 'blur' }
        ,{ validator: validateNumber, trigger: 'blur' }
        ],
        thresholdVal: [{ required: true, message: computed(() => t('阈值不能为空')), trigger: 'blur' }
        ,{ validator: validateNumber, trigger: 'blur' }
        ],
        effect: [{ required: true, message: computed(() => t('流控效果不能为空')), trigger: 'blur' }],
        waitTime: [{ required: true, message: computed(() => t('超时时间不能为空')), trigger: 'blur' }
        ,{ validator: validateNumber, trigger: 'blur' }
        ],
    })
//webService补充信息规则
const oldWebServiceForm = ref({})
const webServiceForm = ref({})
const webServiceRule = ref({
            //	表单验证规则。类型：FormRules
            webSpecification: [{ required: true, message: computed(() => t('webService规范协议不能为空')), trigger: 'blur' }],
            nameSpace: [{ required: true, message: computed(() => t('命名空间不能为空')), trigger: 'blur' }],
        method: [{ required: true, message: computed(() => t('调用方法名不能为空')), trigger: 'blur' }],
})
// 应用 添加 修改表单ref
const ruleFormRef = ref();
//校验url格式
const validateUrl = (rule: any, value: any, callback: any) => {
    let result = $validCheck('nopreurl', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};
//校验特殊字符
const validateSpecial = (rule: any, value: any, callback: any) => {
    let result = $validCheck('special', value, true);
    console.log(value)
    if (!result.valid) {
        callback();
    } else {
        callback(new Error(result.msg));
    }
};
//校验版本格式
const validateVersion = (rule: any, value: any, callback: any) => {
    let result = $validCheck('version', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        let val = value.substring(1)
        let vals = val.split(".")
        let isFirst = true
        for(let it of vals){
            if(isNaN(Number(it))){
                callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
            }
            if(checkDataIsBlank(it)){
                callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
            }
            let numStr = Number(it)
            if(numStr.toString().length!=it.length){
                callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
            }
            if(isFirst){
                if(it<=0){
                    callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
                }
            }
        }
        callback();
    }
};
//校验手机号
const validatePhone = (rule: any, value: any, callback: any) => {
    let result = $validCheck('phone', value, true);
    if (!result.valid) {
        callback(new Error(result.msg));
    } else {
        callback();
    }
};

// 增加 修改应用 弹框的变量配置 控制
let addDialogConfig = ref({
    show: false,
    title: computed(() => t('新增接口')),
    margin:"5vh auto",
    showFooter:true,
    onOkLoading: true,
    visibleChange:(visible)=>{
        // if(!visible && isDelData.value){
        //     if(discardInterfaceId.value != "0"){
        //         let para = {
        //             id: discardInterfaceId.value
        //         }
        //         delAuthInfoById(para)
        //     }
        //     discardInterfaceId.value="0"
        // }
        if(visible){
            nextTick(()=>{
                if(value2.value){
                    authRef.value.initTableData();
                }
            })
        }else{
            if(value2.value){
                authRef.value.restTable();
            }
        }
    }
});

//请求头参数table配置
const requestHeaderParameterTable = ref({
    headerBackground: true,
    pageConfig: false,
    editConfig:{
        trigger: 'click',
        enable:true,
        mode:"cell"
    },
    editRules:{
        //	表单验证规则。类型：FormRules
        parameterKey: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        parameterType: [{ required: true, message: computed(() => t('参数类型不能为空')), trigger: 'blur' }],
        required: [{ required: true, message: computed(() => t('是否必填不能为空')), trigger: 'blur' }],
    },
    height: 200,
    keepSource: true,
    columns: [
        {
            type: "checkbox",
            width: 60,
            fixed: 'left'
        },
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
        },
        {
            title: computed(() => t('参数key')),
            key: 'parameterKey',
            editRender:{
                name:"input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType',
            editRender:{
                name:"select",
                useElement: true,
                options:[
                    { label: computed(() => t('String')), value: 'String' },
                    { label: computed(() => t('integer')), value: 'integer' },
                    { label: computed(() => t('double')), value: 'double' },
                    { label: computed(() => t('boolean')), value: 'boolean' },
                    { label: computed(() => t('number')), value: 'number' },
                ]
            }
        },
        {
            title: computed(() => t('是否必填')),
            key: 'required',
            editRender:{
                name:"select",
                useElement: true,
                options:[
                    {
                        value:"是",
                        label:"是"
                    },
                    {
                        value:"否",
                        label:"否"
                    },
                    
                ]
            }
        },
        // {
        //     title: computed(() => t('默认值')),
        //     key: 'defaultVal',
        //     editRender:{
        //         name:"input",
        //         useElement: true
        //     }
        // },
        {
            title: computed(() => t('参数描述')),
            key: 'notes',
            editRender:{
                name:"input",
                useElement: true
            }
        },
        {
            title: computed(() => t('操作')),
            width: 100,
            fixed: 'right',
            render: (row) => {
                if(isView.value)
                return h('div', [h('span', { onClick: () => { delById(row.parameterStatus,row.id) } ,
                class: 'operate'
            }, t("删除"))]);
            }
        }
    ],
    tableData: [],
}) 

//请求参数table配置
const requestParameterTable = ref({
    headerBackground: true,
    pageConfig: false,
    editConfig:{
        trigger: 'click',
        enable:true,
        mode:"cell"
    },
    height: 200,
    editRules:{
        //	表单验证规则。类型：FormRules
        parameterKey: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        parameterType: [{ required: true, message: computed(() => t('参数类型不能为空')), trigger: 'blur' }],
        required: [{ required: true, message: computed(() => t('是否必填不能为空')), trigger: 'blur' }],
    },
    keepSource: true,
    columns: [
        {
            type: "checkbox",
            width: 60,
            fixed: 'left'
        },
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
        },
        {
            title: computed(() => t('参数key')),
            key: 'parameterKey',
            editRender:{
                name:"input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType',
            editRender:{
                name:"select",
                useElement: true,
                options:[
                    { label: computed(() => t('String')), value: 'String'},
                    { label: computed(() => t('integer')), value: 'integer'},
                    { label: computed(() => t('double')), value: 'double'},
                    { label: computed(() => t('boolean')), value: 'boolean'},
                    { label: computed(() => t('number')), value: 'number'},
                ]
            }
        },
        {
            title: computed(() => t('是否必填')),
            key: 'required',
            editRender:{
                name:"select",
                useElement: true,
                options:[
                    {
                        value:"是",
                        label:"是"
                    },
                    {
                        value:"否",
                        label:"否"
                    },
                    
                ]
            }
        },
        // {
        //     title: computed(() => t('默认值')),
        //     key: 'defaultVal',
        //     editRender:{
        //         name:"input",
        //         useElement: true
        //     }
        // },
        {
            title: computed(() => t('参数描述')),
            key: 'notes',
            editRender:{
                name:"input",
                useElement: true
            }
        },
        {
            title: computed(() => t('操作')),
            width: 100,
            fixed: 'right',
            render: (row) => {
                if(isView.value)
                return h('div', [h('span', { onClick: () => { delById(row.parameterStatus,row.id) } ,
                class: 'operate'
            }, t("删除"))]);
            }
        }
    ],
    tableData: [],
}) 
//请求返回参数table配置
const responseParameterTable = ref({
    headerBackground: true,
    pageConfig: false,
    editConfig:{
        trigger: 'click',
        enable:true,
        mode:"cell",
        autoClear:false
    },
    height: 200,
    keepSource: true,
    editRules:{
        //	表单验证规则。类型：FormRules
        parameterKey: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        parameterType: [{ required: true, message: computed(() => t('参数类型不能为空')), trigger: 'blur' }],
        required: [{ required: true, message: computed(() => t('是否必填不能为空')), trigger: 'blur' }],
    },
    columns: [
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 80,
            fixed: 'left'
        },
        {
            title: computed(() => t('参数key')),
            key: 'parameterKey',
            editRender:{
                name:"input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType',
            editRender:{
                name:"select",
                useElement: true,
                options:[
                    {
                        value:"String",
                        label:"String"
                    },
                    {
                        value:"int",
                        label:"int"
                    },
                    {
                        value:"boolean",
                        label:"boolean"
                    },
                    {
                        value:"double",
                        label:"double"
                    },
                    
                ]
            }
        },
        {
            title: computed(() => t('是否必填')),
            key: 'required',
            editRender:{
                name:"select",
                useElement: true,
                options:[
                    {
                        value:"是",
                        label:"是"
                    },
                    {
                        value:"否",
                        label:"否"
                    },
                    
                ]
            }
        },
        // {
        //     title: computed(() => t('默认值')),
        //     key: 'defaultVal',
        //     editRender:{
        //         name:"input",
        //         useElement: true
        //     }
        // },
        {
            title: computed(() => t('参数描述')),
            key: 'notes',
            editRender:{
                name:"input",
                useElement: true
            }
        },
        {
            title: computed(() => t('操作')),
            width: 100,
            fixed: 'right',
            render: (row) => {
                if(isView.value)
                return h('div', [h('span', { onClick: () => { delById(row.parameterStatus,row.id) } ,
                class: 'operate'
            }, t("删除"))]);
            }
        }
    ],
    tableData: [],
}) 
//参数监听配置
const requestHeaderParameterTableFilter = ref({
    filtersValueCallBack: (filter) => {
        query.value = filter;
    },
    itemList: [
        {
            type: 'slot',
            slotName: 'slotBtns',
            span: 24,
            justify: 'flex-end'
        }

    ],
    showBorder: true
})
// 接口表单
let ruleFormConfig = ref({
    //表单配置
    model: {

    },
    rules: {
        //	表单验证规则。类型：FormRules
        interfaceName: [{ required: true, message: computed(() => t('接口名称不能为空')), trigger: 'blur' },
            {validator:validateSpecial,trigger:'blur'}
        ],
        interfaceUrl:[{required: true, message: computed(() => t('接口调用地址不能为空')), trigger: 'blur'}
            ,{ validator: validateUrl, trigger: 'blur' }
        ],
        interfaceType:[{required: true, message: computed(() => t('接口类型不能为空')), trigger: 'blur'}],
        interfaceMethod:[{required: true, message: computed(() => t('请求方式不能为空')), trigger: 'blur'}],
        isResponseFile: [{ required: true, message: computed(() => t('是否返回文件不能为空')), trigger: 'blur' }],
        networkAgreement:[{required: true, message: computed(() => t('网络协议不能为空')), trigger: 'blur'}],
        head:[{required: true, message: computed(() => t('接口负责人不能为空')), trigger: 'blur'}],
        headPhone:[{required: true, message: computed(() => t('负责人联系方式不能为空')), trigger: 'blur'}
            ,{ validator: validatePhone, trigger: 'blur' }
        ],
        deptInfo:[{required: true, message: computed(() => t('接口负责单位不能为空')), trigger: 'blur'}],
        isOverwrite:[{required: true, message: computed(() => t('是否覆盖更新不能为空')), trigger: 'blur'}],
        version:[{required: true, message: computed(() => t('版本不能为空')), trigger: 'blur'}
            ,{ validator: validateVersion, trigger: 'blur' }
        ],
        url: [
            { required: true, message: computed(() => t('请输入链接地址')), trigger: 'blur' },
            { validator: validateUrl, trigger: 'blur' }
        ],
        illustrate:[{required:true,message:computed(()=> t('接口描述不能为空')),trigger:'blur'}],
        systemId:[{required:true,message:computed(()=> t('接口归属系统不能为空')),trigger:'blur'}],
        deptId:[{required:true,message:computed(()=> t('接口负责人单位不能为空')),trigger:'blur'}],
        executeInstanceId:[{required:true,message:computed(()=> t('部署实例不能为空')),trigger:'blur'}]
    },
    itemList: [
        {
            type: 'input',
            label: computed(() => t('接口名称')),
            prop: 'interfaceName',
            props:{
                maxlength:100,
                placeholder:"请输入接口名称",
            }
        },
        {
            type: 'slot',
            label: computed(() => t('接口类型')),
            prop: 'interfaceType',
            props: {
                slotName: "interfaceType"
            }
        },
        {
            type: 'select',
            label: computed(() => t('网络协议')),
            prop: 'networkAgreement',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('http')), value: 'http' },
                    { label: computed(() => t('https')), value: 'https' }
                ],
                events:{
                    change:(val)=>{
                        ruleFormConfig.value.model = ruleFormRef.value.model

                        for(let it of ruleFormConfig.value.itemList){
                            if(it.prop=="interfaceUrl"){
                                it.props.prependText = val+"://"
                            }
                        }
                    }
                }
            }
        },
        {
            type: 'input',
            label: computed(() => t('接口地址')),
            prop: 'interfaceUrl',
            required: true,
            props:{
                placeholder:"请输入接口地址，例如www.baidu.com",
                maxlength: 500,
                prependText: "http://",
            }
        },
        {
            type: 'select',
            label: computed(() => t('请求方式')),
            prop: 'interfaceMethod',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('get')), value: 'get' },
                    { label: computed(() => t('post')), value: 'post' }
                ],
                events:{
                    change:(val)=>{reqMethod.value = val}
                }
            }
        },
        {
            type: 'select',
            label: computed(() => t('是否返回文件')),
            prop: 'isResponseFile',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('是')), value: true },
                    { label: computed(() => t('否')), value: false }
                ]
            }
        },
        {
            type: 'slot',
            label: computed(() => t('是否限流')),
            prop: 'isLimit',
            props:{
                slotName: "isLimit"
            }
        },
        {
            type: 'slot',
            label: computed(() => t('是否鉴权')),
            prop: 'isLimit',
            props:{
                slotName: "isAuth"
            }
        },
        {
            type: 'select',
            label: computed(() => t('部署实例')),
            prop: 'executeInstanceId',
            props: {
                options: []
            }
        },
        // {
        //     type: 'slot',
        //     label: computed(() => t('是否控制数据请求范围')),
        //     prop: 'isLimitData',
        //     props:{
        //         slotName: "isLimitData"
        //     }
        // },
        {
            type: 'input',
            label: computed(() => t('接口负责人')),
            prop: 'head',
            props:{
                maxlength:50
            }
        },
        {
            type: 'input',
            label: computed(() => t('负责人联系方式')),
            prop: 'headPhone',
            props:{
                placeholder:"请输入11位手机号",
                maxlength:11
            }
        },
        {
            type: 'select',
            label: computed(() => t('接口负责人单位')),
            prop: 'deptId',
            props:{
                maxlength:254,
                options: deptList.value,
                events:{
                    change:(val)=>{
                        ruleFormConfig.value.model = ruleFormRef.value.model
                        let para = {
                            pid:val
                        }
                        ruleFormConfig.value.model.systemName = ""
                        ruleFormConfig.value.model.systemId = ""
                        getListByPid(para).then((res)=>{
                            systemList.value = []
                            for (let it of res.data) {
                                let item = {
                                    label: it.name,
                                    value: it.id
                                }
                                systemList.value.push(item)
                            }
                            for(let it of ruleFormConfig.value.itemList){
                                if(it.prop == "systemId"){
                                    it.props.options = systemList.value;
                                    break;
                                }
                            }
                        })
                    }
                }
            }
        },
        {
            type: 'select',
            label: computed(() => t('接口归属系统')),
            prop: 'systemId',
            props:{
                maxlength:254,
                options: systemList.value,
            }
        },
        {
            type: 'input',
            label: computed(() => t('版本')),
            prop: 'version',
            props:{
                placeholder:"请输入版本号。例如V1.1.1",
                maxlength:50
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('接口描述')),
            prop: 'illustrate',
            props: {
                //文本域类型的属性
                rows: 3, //输入框行数,类型：number,
                maxlength:500
            }
        },
        {
            type: 'slot',
            label: computed(() => t('请求头参数')),
            prop: '',
            props:{
                slotName:"parameterHeader"
            }
        },
        {
            type: 'slot',
            label: computed(() => t('请求参数')),
            prop: '',
            props:{
                slotName:"parameter"
            }
        },
        {
            type: 'slot',
            label: computed(() => t('返回参数')),
            prop: '',
            props:{
                slotName:"parameterResponse"
            }
        },
        {
            type: 'textarea',
            label: computed(() => t('备注')),
            prop: 'notes',
            props: {
                //文本域类型的属性
                rows: 3, //输入框行数,类型：number
                maxlength: 254
            }
        }
    ],
    descriptionsFormConfig: {
        labelWidth: '200px',
        labelAlign: 'center',
    }
});

//table参数新增
function addParameter(type){
    let itemData = {
        id: ""+Date.now(),
        parameterKey: "",
        parameterType: "",
        required: "",
        defaultVal: "",
        notes: "",
        parameterStatus: type,
        isItems:false,
        pid:0,
        level:1,
    }
    if(type==1){
        let newData = parameterDatas(type,false);
        itemData.sort = newData.length==0?1:newData[newData.length-1].sort+1
        newData.push(itemData);
        requestHeaderParameterTable.value.tableData = newData;
    }else if(type==2){
        let newData = parameterDatas(type,false);
        itemData.sort = newData.length==0?1:newData[newData.length-1].sort+1
        newData.push(itemData)
        requestParameterTable.value.tableData = newData;
    }else if(type == 3){
        let newData = parameterDatas(type,false);
        newData.push(itemData)
        responseParameterTable.value.tableData = newData;
    }
}
//批量删除参数信息
const delParameter = (type)=>{
    if(type==1){
        if(headerSelectedCheckboxVal.value.length!=0){
            for(let id of headerSelectedCheckboxVal.value){
                delById(type,id)
            }
        }else{
            ElMessage({ type: 'warning', message: (type==1?"请求头参数，":type==2?"请求参数，":type==3?"返回参数，":"")+'未选择数据，无法删除' })
        }
    }else if(type==2){
        if(requestSelectedCheckboxVal.value.length!=0){
            for(let id of requestSelectedCheckboxVal.value){
                delById(type,id)
            }
        }else{
            ElMessage({ type: 'warning', message: (type==1?"请求头参数，":type==2?"请求参数，":type==3?"返回参数，":"")+'未选择数据，无法删除' })
        }
    }else if(type == 3){

    }
}
//返回新改变的数据
function parameterDatas(type,isCheck){
    let data = [];
    let oldData = [];
    if(type==1){
        data = editRequestHeaderRef.value.vxeTableRef.getRecordset().updateRecords;
        oldData = requestHeaderParameterTable.value.tableData;
    }else if(type==2){
        data = editRequestRef.value.vxeTableRef.getRecordset().updateRecords;
        oldData = requestParameterTable.value.tableData;
    }else if(type == 3){
        data = editResponseRef.value.vxeTableRef.getRecordset().updateRecords;
        oldData = responseParameterTable.value.tableData;
    }
    let newData = []
    let index = 0;
    for (let it of oldData) {
        if (index<data.length && it.id == data[index].id) {
            newData.push(data[index])
            index++;
        } else {
            newData.push(it)
        }
    }
    if(isCheck){
        let index = 1
        for(let it of newData){
            if(!checkData(it.parameterKey)){
                ElMessage({ type: 'warning', message: (type==1?"请求头参数，":type==2?"请求参数，":type==3?"返回参数":"")+'第'+index+'行，参数key不能为空' })
                return false;
            }
            if(!checkData(it.parameterType)){
                ElMessage({ type: 'warning', message: (type==1?"请求头参数":type==2?"请求参数":type==3?"返回参数":"")+'第'+index+'行，参数类型不能为空' })
                return false;
            }
            if(!checkData(it.required)){
                ElMessage({ type: 'warning', message: (type==1?"请求头参数":type==2?"请求参数":type==3?"返回参数":"")+'第'+index+'行，是否必填不能为空' })
                return false;
            }
            index++;
        }
    }

    return newData;
}
//检查值是否为空
function checkData(data){
    if(data==null || data==undefined || data==""){
        return false;
    }else{
        return true;
    }
}
//删除参数table数据
function delById(type,id){
    let data = []
    let newData = []
    if(type==1){
        data = parameterDatas(type,false);
        for (let it of data) {
            if (it.id != id) {
                newData.push(it)
            }
        }
        requestHeaderParameterTable.value.tableData = newData;
    }else if(type==2){
        data = parameterDatas(type,false);
        for (let it of data) {
            if (it.id != id) {
                newData.push(it)
            }
        }
        requestParameterTable.value.tableData = newData;
    }else if(type == 3){
        data = parameterDatas(type,false);
        for (let it of data) {
            if (it.id != id) {
                newData.push(it)
            }
        }
        responseParameterTable.value.tableData = newData;
    }
}

//打开配置信息
function openView(type){
    if(type==1){
        limitInfo.value = true
        limitInfoForm.value = JSON.parse(JSON.stringify(oldLimitInfoForm.value))
    }else{
        openDialog.value = true
    }
}
//关闭配置信息
function closeDialog(type){
    if(type==1){
        limitInfo.value = false
        limitInfoForm.value = JSON.parse(JSON.stringify(oldLimitInfoForm.value))
    }else if(type==2){
        webService.value = false
        let jsData = JSON.parse(JSON.stringify(oldWebServiceForm.value))
        webServiceForm.value.method = jsData.method
        webServiceForm.value.nameSpace = jsData.nameSpace
        webServiceForm.value.webSpecification = jsData.webSpecification
    }
}
//确定配置信息
function confirDialog(type){
    if(type==1){
        limitInfoRef.value.validate((valid)=>{
            if(valid){
                limitInfo.value = false
                oldLimitInfoForm.value = JSON.parse(JSON.stringify(limitInfoForm.value))
            }else{
                return false
            }
        })
    }else if(type==2){
        webServiceRef.value.validate((valid)=>{
            if(valid){
                webService.value = false
                oldWebServiceForm.value = JSON.parse(JSON.stringify(webServiceForm.value))
            }else{
                return false
            }
        })
    }
}

//新增按钮
async function addDialog(){
    ruleFormConfig.value.model = {}
    initFormData()
    let res = await getInterfaceId()
    ruleFormConfig.value.model.id = res.data
    discardInterfaceId.value = res.data
    interfaceId.value = res.data
    let resExecute = await getRegisterNum()
    //let executeTable = analysisData(resExecute)
    for(let it of resExecute.data){
        let item = {
            label: computed(() => t("实例ID:" + it.instanceId + "    已注册接口数："+it.num)), value: it.instanceId
        }
        exceuteInstanceInfo.value.push(item)

    }
    for(let it of ruleFormConfig.value.itemList){
        if(it.props==undefined){
            it.props = {
                disabled: false
            }
        }else{
            it.props.disabled = false
        }
        if(it.prop=="executeInstanceId"){
            it.props.options = exceuteInstanceInfo.value;
        }
    }
    analysisYableData('[]',false)
    analysisTreeData(res)
    nextTick(()=>{
        if(value2.value){
            authRef.value.initTableData();
        }
    })
}
addDialog()
//编辑按钮
async function edit(id) {
    initFormData()
    let para = {
        id:id
    }
    let res = await getInterfaceInfoById(para)
    ruleFormConfig.value.model=res.data
    for(let it of ruleFormConfig.value.itemList){
        if(it.props==undefined){
            it.props = {
                disabled: false
            }
        }else{
            it.props.disabled = false
        }
    }
    initData(res.data)
    analysisYableData(res.data.parameters,false)
    analysisTreeData(res)
    addDialogConfig.value.title = computed(() => t('编辑接口信息'))
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.show = true
}
//版本升级按钮
async function updateVersion(id) {
    initFormData()
    let para = {
        id:id
    }
    let res = await getInterfaceInfoById(para)
    ruleFormConfig.value.model=res.data
    for(let it of ruleFormConfig.value.itemList){
        if(it.props==undefined){
            it.props = {
                disabled: false
            }
        }else{
            it.props.disabled = false
        }
    }
    initData(res.data)
    analysisYableData(res.data.parameters,false)
    analysisTreeData(res)
    let response = await getInterfaceId(para)
    ruleFormConfig.value.model.id = response.data
    discardInterfaceId.value = response.data
    interfaceId.value = response.data
    saveType.value = "1"
    let isOverwriteItem = {
            type: 'slot',
            label: computed(() => t('覆盖更新')),
            prop: 'isOverwrite',
            props:{
                slotName: "isOverwrite"
            }
        }
    ruleFormConfig.value.itemList.push(isOverwriteItem)
    addDialogConfig.value.title = computed(() => t('接口版本升级维护信息'))
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.show = true
}
//详情按钮
async function view(id,show) {
    initFormData()
    let para = {
        id:id
    }
    let res = await getInterfaceInfoById(para)
    ruleFormConfig.value.model=res.data
    for(let it of ruleFormConfig.value.itemList){
        if(it.props==undefined){
            it.props = {
                disabled: true
            }
        }else{
            it.props.disabled = true
        }
    }
    initData(res.data)
    analysisYableData(res.data.parameters,true)
    analysisTreeData(res)
    addDialogConfig.value.okText = false
    addDialogConfig.value.title = computed(() => t('查看接口信息'))
    addDialogConfig.value.show = true
    props.isShow = show
}

//解析请求参数json数据并赋值
function analysisYableData(jsonData,isDisabled){
    let reloadData = JSON.parse(jsonData)
    let jzData1 = []
    let jzData2 = []
    let jzData3 = []
    for (let it of reloadData) {
        if (it.parameterStatus == "1") {
            jzData1.push(it)
        }
        if (it.parameterStatus == "2") {
            jzData2.push(it)
        }
        if (it.parameterStatus == "3") {
            jzData3.push(it)
        }
    }
    isView.value = !isDisabled
    requestHeaderParameterTable.value.editConfig = isView.value?{
        trigger: 'click',
        enable:true,
        mode:"cell"
    }:false
    requestHeaderParameterTable.value.tableData = jzData1

    requestParameterTable.value.editConfig = isView.value?{
        trigger: 'click',
        enable:true,
        mode:"cell"
    }:false
    requestParameterTable.value.tableData = jzData2

    responseParameterTable.value.editConfig = isView.value?{
        trigger: 'click',
        enable:true,
        mode:"cell"
    }:false
    responseParameterTable.value.tableData = jzData3
}

//解析参数json树形数据并赋值
function analysisTreeData(res){
    if(res.data.reqParameters!=undefined){
        reqData.value = JSON.parse(res.data.reqParameters)
    }else{
        reqData.value = []
    }
    if(res.data.resParameters!=undefined){
        resData.value = JSON.parse(res.data.resParameters)
    }else{
        resData.value = []
    }
}
//初始化switch组件的值
function initData(data,isDisabled){
    if (data.isLimit == "是") {
        value1.value = true
    }else{
        value1.value = false
    }
    if (data.isAuth == "是") {
        value3.value = true
    }else{
        value3.value = false
    }
    if(data.isLimitData == "是"){
        value2.value = true
    }else{
        value2.value = false
    }
    reqMethod.value = data.interfaceMethod
    interfaceType.value = data.interfaceType
    limitInfoForm.value = JSON.parse(data.limitInfo)
    oldLimitInfoForm.value = JSON.parse(data.limitInfo)
    selectData.value = data.parameterIds
    interfaceId.value = data.id
}

//新增时初始化赋值
function initFormData(){
    interfaceType.value = "Rest"
    limitInfoForm.value = {
        thresholdType: "0",
        limitTime: "",
        limitCount: "",
        thresholdVal: "",
        effect: "1",
        waitTime: ""
    }
    oldLimitInfoForm.value = {
        thresholdType: "0",
        limitTime: "",
        limitCount: "",
        thresholdVal: "",
        effect: "1",
        waitTime: ""
    }
    selectData.value = ""
    isDelData.value = true
    //保存类型，0普通保存，1版本迭代保存
    saveType.value = "0"
    //去除表单的是否覆盖更新
    let newData = []
    for(let it of ruleFormConfig.value.itemList){
        if(it.prop!="isOverwrite"){
            newData.push(it)
        }
    }
    ruleFormConfig.value.itemList = newData
}
//弹出提示信息
function openPromptDialog(title,msg){
    ElMessageBox.alert(msg,title,{
        confirmButtonText:'确认'
    })
}
//递归遍历数据
function selectChild(child,arrayData){
    if(child!=undefined){
        for (let it of child) {
            if (it.children instanceof Array) {
                selectChild(it.children, arrayData)
            }
            arrayData.push(it)
        }
    }
}
//打开webService补充页面
const openWebServiceDialog = ()=>{
    webService.value = true
    console.log(ruleFormRef.value.model)
}
const resetData = ()=>{
    addDialog()
    let instance = ruleFormRef.value?.elFormRef;
    instance.resetFields()
}
//提交接口注册数据
const submitData = () => {
    loading.value = true
    const y9RuleFormInstance = ruleFormRef.value?.elFormRef;
    ruleFormRef.value.model.interfaceType = interfaceType.value;
    console.log(ruleFormRef.value.model)
    y9RuleFormInstance.validate(async (valid) => {
        if (valid) {
            let data = ruleFormRef.value.model;
            if (!(data.interfaceType == "Rest")) {
                if (checkDataIsBlank(webServiceForm.value.webSpecification)) {
                    openPromptDialog("webService补充信息确认", "webService补充信息页面存在未填信息请确认")
                    loading.value = false

                    return;
                } else {
                    if (webServiceForm.value.webSpecification == "JAX-WS") {
                        if (checkDataIsBlank(webServiceForm.value.nameSpace) || checkDataIsBlank(webServiceForm.value.method)) {
                            openPromptDialog("webService补充信息确认", "webService补充信息页面存在未填信息请确认")
                            loading.value = false
                            return;
                        }
                    }
                }
            }

            if (!(data.interfaceType == "Rest")) {
                data.webSpecification = webServiceForm.value.webSpecification
                data.nameSpace = webServiceForm.value.nameSpace
                data.method = webServiceForm.value.method
            }
            data.interfaceType = interfaceType.value
            data.isOverwrite = isOverwrite.value
            let formData = new FormData();
            if (value2.value) {
                let parameterIds = authRef.value.getCheckData()

                if (parameterIds == undefined || parameterIds.length == 0) {
                    loading.value = false

                    openPromptDialog("权限参数确认", "已经开启鉴权，请至少选择一项权限参数")
                    return;
                }
                selectData.value = parameterIds;
            }
            data.limitInfo = JSON.stringify(limitInfoForm.value);
            // data.limitInfo = limitInfoForm.value;
            let parameterData = parameterDatas(1, true);
            if (typeof parameterData == 'boolean') {
                loading.value = false

                return;
            }
            data.parameters = JSON.stringify(parameterData);
            //获取请求参数数据
            let arrData = []
            let createKeys = ""
            let reqData
            if (reqMethod.value == 'post' && interfaceType.value == 'Rest') {
                reqData = requestParameterRef.value.getTableData()
                //处理数据查看映射权限参数，将多层结构转为一层结构遍历键
                selectChild(reqData, arrData)
                for (let it of arrData) {
                    if (!JSON.parse(it.isItems)) {
                        createKeys += it.parameterKey + ","
                    }
                }
            } else if (reqMethod.value == 'get' || interfaceType.value != 'Rest') {
                reqData = parameterDatas(2, true);
                if (typeof reqData == 'boolean') {
                    loading.value = false

                    return;
                }
                for (let it of reqData) {
                    createKeys += it.parameterKey + ",";
                }
            }

            if(value2.value){
                let fieldKeys = authRef.value.getCheckDataKeys()
                let keySet = new Set();
                for (let key of fieldKeys) {
                    keySet.add(key)
                }
                if (keySet.size != fieldKeys.length) {
                    loading.value = false
                    openPromptDialog("权限参数确认", "权限参数存在重复字段名称")
                    return;
                }
                let exitKeys = ""
                for (let it of fieldKeys) {
                    if (createKeys.indexOf(it + ",") == -1) {
                        exitKeys += it + ","
                    }
                }
                if (exitKeys.length != 0) {
                    exitKeys = exitKeys.substring(0, exitKeys.length - 1)
                    loading.value = false
                    openPromptDialog("权限参数确认", "权限参数：" + exitKeys + "没有在请求参数列表中体现。")
                    return;
                }
                let arrDataMap = new Map()
                if(arrData.length!=0){
                    for (let it of arrData) {
                        arrDataMap.set(it.parameterKey, it)
                    }
                }else{
                    for (let it of reqData) {
                        arrDataMap.set(it.parameterKey, it)
                    }
                }

                let isPrompt = false
                for (let it of fieldKeys) {
                    if(arrDataMap.get(it).required=="否"){
                        arrDataMap.get(it).required = "是"
                        isPrompt=true
                    }
                }
                if(isPrompt){
                    loading.value = false
                    openPromptDialog("是否必填确认", "请求参数列表中对应的数据请求范围参数的是否必填项已经自动变更为是请确认，数据请求范围的参数都是必填的")
                    return;
                }
            }
            data.reqParameters = JSON.stringify(reqData)
            //赋值系统名称和公司名称
            for(let it of deptList.value){
                if(it.value==data.deptId){
                    data.deptInfo = it.label
                }
            }
            for(let it of systemList.value){
                if(it.value==data.systemId){
                    data.systemName = it.label
                }
            }

            //获取返回参数数据
            let resData = responseParameterRef.value.getTableData()
            data.resParameters = JSON.stringify(resData)
            for (let key in data) {
                if (data[key] != null && key != "createTime" && key != "updateTime")
                    formData.append(key, data[key])
            }
            data.isAuth = (value3.value == true ? "是" : "否");
            data.isLimit = (value1.value == true ? "是" : "否");
            data.isLimitData = (value2.value==true?"是":"否");
            data.parameterIds = (selectData.value == undefined ? "" : ""+selectData.value);
            formData.set('isAuth', value3.value == true ? "是" : "否")
            formData.set('isLimit', value1.value == true ? "是" : "否")
            formData.set('isLimitData',value2.value==true?"是":"否")
            formData.set("parameterIds", selectData.value == undefined ? "" : selectData.value)
            let res;
            //正常保存调用接口
            if (saveType.value == "0") {
                res = await saveInterfaceInfo(
                    data
                )
            }
            //版本迭代升级调用接口
            else if (saveType.value == "1") {
                res = await saveUpdateVersionInfo(data)
            }
            if (res.code == 0) {
                if (res.status == 'success') {
                    ElMessage({
                        message: '数据保存成功',
                        type: 'success'
                    })
                    router.push({ path: 'registerInterface' })
                    isDelData.value = false;
                } else {
                    loading.value = false
                    openPromptDialog("数据保存失败确认", res.msg)
                }
            }

        } else {
            loading.value = false
        }
    });
}
//控制数据请求范围初始化
const ctlDataAuth = ()=>{
    if(value2.value){
        nextTick(()=>{
            authRef.value.initTableData();
        })
    }
}
//判断值为空
const checkDataIsBlank=(val)=>{
    if(val==undefined || val==null || val==""){
        return true;
    }else{
        return false;
    }
}
//取消接口注册数据
const cancelData = ()=>{
    loading.value = true
    router.push({path:'registerInterface'})
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
//解析json数据
function analysisData(data){
    //存储需要的列表数据
    let tableData = []
    //获取注册应用数据列表
    let applications = data.applications.application
    //解析转化为table数据
    for(let application of applications){
        let appName = application.name
        //解析实例
        for(let instance of application.instance){
            let instanceData = {
                name: appName,
                instanceId: instance.instanceId,
                hostName:instance.hostName,
                ip: instance.ipAddr,
                port: instance.port["$"],
                status: instance.status,
                createTime: instance.leaseInfo.registrationTimestamp,
                baseUrl: instance.healthCheckUrl.slice(0,-6)
            }
            tableData.push(instanceData)
        }
    }
    return tableData
}
defineExpose({addDialog,edit,view,updateVersion})
</script>
<style scoped>
.leftMargin {
    margin-left: 5px;
}
.operate{
    cursor: pointer;
}
.limitClass {
    :deep(.el-form-item) {
        margin-bottom: 18px !important;
    }
}
:deep(.y9-filter-item){
    margin-bottom: 10px!important;
}
</style>
<style>
.btnDiv{
    width: 100%;
    text-align: right;
}
</style>
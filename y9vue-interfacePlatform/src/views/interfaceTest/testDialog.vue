<template>
    <!-- 注册接口信息 -->
    <div v-loading="loading" element-loading-text="加载中..."
        style="height: calc(100% - 30px);padding: 15px;background-color: #ffffff">
        <el-row :gutter="10" class="rowHeight">
            <el-col :span="14" class="rowHeight">
                <el-scrollbar>
                    <y9Form ref="ruleFormRef" :config="ruleFormConfig">
                        <template #interfaceType>
                            <el-radio-group v-model="interfaceType" :disabled="!isView">
                                <el-radio-button label="Rest" value="Rest" />
                                <el-radio-button label="webService" value="webService"
                                    @click="openWebServiceDialog()" />
                            </el-radio-group>
                        </template>
                        <template #network>
                            <el-select v-model="network" disabled>
                                <el-option :label="'http'" :value="'http'"></el-option>
                                <el-option :label="'https'" :value="'https'"></el-option>
                            </el-select>
                        </template>
                        <template #parameterHeader>
                            <y9VxeTable ref="editRequestHeaderRef" :config="requestHeaderParameterTable"
                                v-model:selectedVal="headerSelectedCheckboxVal"
                                :filterConfig="requestHeaderParameterTableFilter">
                                <template v-slot:slotBtns>
                                    <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                        :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main"
                                        type="primary" @click="addParameter('1')">
                                        <i class="ri-add-line"></i>
                                        <span>{{ $t('新增') }}</span>
                                    </el-button>
                                    <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                                        class="el-button el-button--default global-btn-third" type="primary"
                                        @click="delParameter('1')">
                                        <i class="ri-delete-bin-line"></i>
                                        <span>{{ $t('删除') }}</span>
                                    </el-button>
                                </template>
                            </y9VxeTable>
                        </template>
                        <template #parameter>
                            <y9VxeTable ref="editRequestRef" :config="requestParameterTable"
                                v-model:selectedVal="requestSelectedCheckboxVal"
                                :filterConfig="requestHeaderParameterTableFilter">
                                <template v-slot:slotBtns>
                                    <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                        :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main"
                                        type="primary" @click="addParameter('2')">
                                        <i class="ri-add-line"></i>
                                        <span>{{ $t('新增') }}</span>
                                    </el-button>
                                    <el-button v-if="isView" :size="fontSizeObj.buttonSize"
                                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                                        class="el-button el-button--default global-btn-third" type="primary"
                                        @click="delParameter('2')">
                                        <i class="ri-delete-bin-line"></i>
                                        <span>{{ $t('删除') }}</span>
                                    </el-button>
                                </template>
                            </y9VxeTable>
                        </template>
                        <template #parameterResponse>
                            <parameter ref="responseParameterRef" v-model:parameterStatus="responseStatus"
                                v-model:data="resData" v-model:isView="isView"></parameter>
                        </template>
                    </y9Form>
                </el-scrollbar>
            </el-col>
            <el-col :span="10" class="rowHeight">
                <div class="rtDataBackground">
                    <div class="rtDataTitleDiv" style="display: flex;align-items: center !important;vertical-align: middle;text-align:center;">
                        <el-row style="width: 100%;">
                            <el-col :span="4" class="elRow"><span class="rtDataTitle">返回数据</span></el-col>
                            <el-col :span="16"></el-col>
                            <el-col :span="4" class="btnDiv"><el-button class="global-btn-main" type="primary" @click="submitData">
                                    <i class="ri-send-plane-line"></i>
                                    <span>{{ $t('发送') }}</span>

                                </el-button></el-col>
                        </el-row>
                    </div>
                    <div class="rtDataContent">
                        <el-scrollbar height="calc(100% - 1px)">
                            <VueJsonPretty :data="jsonData"></VueJsonPretty>
                        </el-scrollbar>
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>

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
            <el-form-item label="命名空间" prop="nameSpace" v-if="webServiceForm.webSpecification == 'JAX-WS'">
                <el-input v-model="webServiceForm.nameSpace" :disabled="!isView" placeholder="请输入命名空间"
                    maxlength="300" />
            </el-form-item>
            <el-form-item label="调用方法名" prop="method" v-if="webServiceForm.webSpecification == 'JAX-WS'">
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
import { saveUpdateVersionInfo, saveInterfaceInfo, getInterfaceId, getInterfaceInfoById, testInterface } from '@/api/interface/interface'
import { getListByType, getListByPid } from '@/api/systemidentifier/systemidentifier'
import { ElMessage, ElMessageBox } from 'element-plus';
import parameter from '../parameter/parameterTable.vue';
import { useRoute } from 'vue-router';
import { nextTick } from 'vue';
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css'

// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const loading = ref(false)
const settingStore = useSettingStore();
const route = useRoute();
const { t } = useI18n();
const query: any = ref({});
const filterRef = ref();
const editRequestHeaderRef = ref();
const requestSelectedCheckboxVal = ref([])
const headerSelectedCheckboxVal = ref([])
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
const value2 = ref('false')
const value3 = ref('false')
const selectData = ref()
const isView = ref(true)
const jsonData = ref({})
const discardInterfaceId = ref("0")
const saveType = ref("0")
const isDelData = ref(true)
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
    thresholdType: "0",
    limitTime: "",
    limitCount: "",
    thresholdVal: "",
    effect: "1",
    waitTime: ""
})
const props = defineProps({
    status: {
        type: String,
        default: () => "其他"
    },
    isShow: {
        type: Boolean,
        default: () => true
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
        for (let it of vals) {
            if (isNaN(Number(it))) {
                callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
            }
            if (checkDataIsBlank(it)) {
                callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
            }
            let numStr = Number(it)
            if (numStr.toString().length != it.length) {
                callback(new Error("版本格式错误,正确格式为V1.1.1，错误格式为V1.01;V1..;V0.1等"));
            }
            if (isFirst) {
                if (it <= 0) {
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
    margin: "5vh auto",
    showFooter: true,
    onOkLoading: true,
    visibleChange: (visible) => {
    }
});

//请求头参数table配置
const requestHeaderParameterTable = ref({
    headerBackground: true,
    pageConfig: false,
    editConfig: {
        trigger: 'click',
        enable: true,
        mode: "cell"
    },
    editRules: {
        //	表单验证规则。类型：FormRules
        parameterKey: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        parameterType: [{ required: true, message: computed(() => t('参数类型不能为空')), trigger: 'blur' }],
        val: [{ required: true, message: computed(() => t('参数值不能为空')), trigger: 'blur' }],
    },
    height: 200,
    keepSource: true,
    columns: [
        {
            type: "checkbox",
            width: 50,
            fixed: 'left'
        },
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 50,
            fixed: 'left'
        },
        {
            title: computed(() => t('参数key')),
            key: 'parameterKey',
            editRender: {
                name: "input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数value')),
            key: 'val',
            editRender: {
                name: "input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType',
            editRender: {
                name: "select",
                useElement: true,
                options: [
                    { label: computed(() => t('String')), value: 'String' },
                    { label: computed(() => t('integer')), value: 'integer' },
                    { label: computed(() => t('double')), value: 'double' },
                    { label: computed(() => t('boolean')), value: 'boolean' },
                ]
            }
        },
        {
            title: computed(() => t('操作')),
            width: 70,
            fixed: 'right',
            render: (row) => {
                if (isView.value)
                    return h('div', [h('span', {
                        onClick: () => { delById(row.parameterStatus, row.id) },
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
    editConfig: {
        trigger: 'click',
        enable: true,
        mode: "cell"
    },
    height: 200,
    editRules: {
        //	表单验证规则。类型：FormRules
        parameterKey: [{ required: true, message: computed(() => t('参数key不能为空')), trigger: 'blur' }],
        parameterType: [{ required: true, message: computed(() => t('参数类型不能为空')), trigger: 'blur' }],
        val: [{ required: true, message: computed(() => t('参数值不能为空')), trigger: 'blur' }],
    },
    keepSource: true,
    columns: [
        {
            type: "checkbox",
            width: 50,
            fixed: 'left'
        },
        {
            type: 'index',
            title: computed(() => t('序号')),
            width: 50,
            fixed: 'left'
        },
        {
            title: computed(() => t('参数key')),
            key: 'parameterKey',
            editRender: {
                name: "input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数value')),
            key: 'val',
            editRender: {
                name: "input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType',
            editRender: {
                name: "select",
                useElement: true,
                options: [
                    { label: computed(() => t('String')), value: 'String' },
                    { label: computed(() => t('integer')), value: 'integer' },
                    { label: computed(() => t('double')), value: 'double' },
                    { label: computed(() => t('boolean')), value: 'boolean' },
                ]
            }
        },
        {
            title: computed(() => t('操作')),
            width: 70,
            fixed: 'right',
            render: (row) => {
                if (isView.value)
                    return h('div', [h('span', {
                        onClick: () => { delById(row.parameterStatus, row.id) },
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
    editConfig: {
        trigger: 'click',
        enable: true,
        mode: "cell",
        autoClear: false
    },
    height: 200,
    keepSource: true,
    editRules: {
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
            editRender: {
                name: "input",
                useElement: true
            }
        },
        {
            title: computed(() => t('参数类型')),
            key: 'parameterType',
            editRender: {
                name: "select",
                useElement: true,
                options: [
                    {
                        value: "String",
                        label: "String"
                    },
                    {
                        value: "int",
                        label: "int"
                    },
                    {
                        value: "boolean",
                        label: "boolean"
                    },
                    {
                        value: "double",
                        label: "double"
                    },

                ]
            }
        },
        {
            title: computed(() => t('是否必填')),
            key: 'required',
            editRender: {
                name: "select",
                useElement: true,
                options: [
                    {
                        value: "是",
                        label: "是"
                    },
                    {
                        value: "否",
                        label: "否"
                    },

                ]
            }
        },
        {
            title: computed(() => t('参数描述')),
            key: 'notes',
            editRender: {
                name: "input",
                useElement: true
            }
        },
        {
            title: computed(() => t('操作')),
            width: 100,
            fixed: 'right',
            render: (row) => {
                if (isView.value)
                    return h('div', [h('span', {
                        onClick: () => { delById(row.parameterStatus, row.id) },
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
const rtDataFormConfig = ref({
    //表单配置
    model: {

    },
    rules: {
        //	表单验证规则。类型：FormRules
        interfaceName: [{ required: true, message: computed(() => t('接口名称不能为空')), trigger: 'blur' },
        { validator: validateSpecial, trigger: 'blur' }
        ],
        interfaceUrl: [{ required: true, message: computed(() => t('接口调用地址不能为空')), trigger: 'blur' }
            , { validator: validateUrl, trigger: 'blur' }
        ],
        interfaceType: [{ required: true, message: computed(() => t('接口类型不能为空')), trigger: 'blur' }],
        interfaceMethod: [{ required: true, message: computed(() => t('请求方式不能为空')), trigger: 'blur' }],
        networkAgreement: [{ required: true, message: computed(() => t('网络协议不能为空')), trigger: 'blur' }],
        head: [{ required: true, message: computed(() => t('接口负责人不能为空')), trigger: 'blur' }],
        headPhone: [{ required: true, message: computed(() => t('接口负责人联系方式不能为空')), trigger: 'blur' }
            , { validator: validatePhone, trigger: 'blur' }
        ],
        deptInfo: [{ required: true, message: computed(() => t('接口负责单位不能为空')), trigger: 'blur' }],
        isOverwrite: [{ required: true, message: computed(() => t('是否覆盖更新不能为空')), trigger: 'blur' }],
        version: [{ required: true, message: computed(() => t('版本不能为空')), trigger: 'blur' }
            , { validator: validateVersion, trigger: 'blur' }
        ],
        url: [
            { required: true, message: computed(() => t('请输入链接地址')), trigger: 'blur' },
            { validator: validateUrl, trigger: 'blur' }
        ],
    },
    itemList: [
        {
            type: 'textarea',
            label: computed(() => t('请求头参数')),
            prop: '',
        },

    ],
    descriptionsFormConfig: {
        labelWidth: '100px',
        labelAlign: 'center',
    }
})
// 接口表单
let ruleFormConfig = ref({
    //表单配置
    model: {
        networkAgreement: 'http'
    },
    rules: {
        //	表单验证规则。类型：FormRules
        interfaceName: [{ required: true, message: computed(() => t('接口名称不能为空')), trigger: 'blur' },
        { validator: validateSpecial, trigger: 'blur' }
        ],
        interfaceUrl: [{ required: true, message: computed(() => t('接口调用地址不能为空')), trigger: 'blur' }
            , { validator: validateUrl, trigger: 'blur' }
        ],
        interfaceType: [{ required: true, message: computed(() => t('接口类型不能为空')), trigger: 'blur' }],
        interfaceMethod: [{ required: true, message: computed(() => t('请求方式不能为空')), trigger: 'blur' }],
        networkAgreement: [{ required: true, message: computed(() => t('网络协议不能为空')), trigger: 'blur' }],
        head: [{ required: true, message: computed(() => t('接口负责人不能为空')), trigger: 'blur' }],
        isResponseFile: [{ required: true, message: computed(() => t('是否返回文件')), trigger: 'blur' }],
        headPhone: [{ required: true, message: computed(() => t('接口负责人联系方式不能为空')), trigger: 'blur' }
            , { validator: validatePhone, trigger: 'blur' }
        ],
        deptInfo: [{ required: true, message: computed(() => t('接口负责单位不能为空')), trigger: 'blur' }],
        isOverwrite: [{ required: true, message: computed(() => t('是否覆盖更新不能为空')), trigger: 'blur' }],
        version: [{ required: true, message: computed(() => t('版本不能为空')), trigger: 'blur' }
            , { validator: validateVersion, trigger: 'blur' }
        ],
        url: [
            { required: true, message: computed(() => t('请输入链接地址')), trigger: 'blur' },
            { validator: validateUrl, trigger: 'blur' }
        ],
    },
    itemList: [
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
                events: {
                    change: (val) => {
                        ruleFormConfig.value.model = ruleFormRef.value.model

                        for (let it of ruleFormConfig.value.itemList) {
                            if (it.prop == "interfaceUrl") {
                                it.props.prependText = val + "://"
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
            props: {
                placeholder: "请输入接口地址，例如www.baidu.com",
                maxlength: 500,
                prependText: "http://",
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
            label: computed(() => t('请求方式')),
            prop: 'interfaceMethod',
            props: {
                options: [
                    //选项列表
                    { label: computed(() => t('get')), value: 'get' },
                    { label: computed(() => t('post')), value: 'post' }
                ],
                events: {
                    change: (val) => { reqMethod.value = val }
                }
            }
        },
        {
            type: 'select',
            label: computed(() => t('是否返回文件')),
            prop: 'isResponseFile',
            props: {
                disabled: true,
                options: [
                    //选项列表
                    { label: computed(() => t('是')), value: 'true' },
                    { label: computed(() => t('否')), value: 'false' }
                ]
            }
        },
        {
            type: 'slot',
            label: computed(() => t('请求头参数')),
            prop: '',
            props: {
                slotName: "parameterHeader"
            }
        },
        {
            type: 'slot',
            label: computed(() => t('请求参数')),
            prop: '',
            props: {
                slotName: "parameter"
            }
        },
    ],
    descriptionsFormConfig: {
        labelWidth: '130px',
        labelAlign: 'center',
    }
});

//table参数新增
function addParameter(type) {
    let itemData = {
        id: "" + Date.now(),
        parameterKey: "",
        parameterType: "",
        required: "",
        defaultVal: "",
        notes: "",
        parameterStatus: type,
        isItems: false,
        pid: 0,
        level: 1,
    }
    if (type == 1) {
        let newData = parameterDatas(type, false);
        itemData.sort = newData.length == 0 ? 1 : newData[newData.length - 1].sort + 1
        newData.push(itemData);
        requestHeaderParameterTable.value.tableData = newData;
    } else if (type == 2) {
        let newData = parameterDatas(type, false);
        itemData.sort = newData.length == 0 ? 1 : newData[newData.length - 1].sort + 1
        newData.push(itemData)
        requestParameterTable.value.tableData = newData;
    } else if (type == 3) {
        let newData = parameterDatas(type, false);
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
function parameterDatas(type, isCheck) {
    let data = [];
    let oldData = [];
    if (type == 1) {
        data = editRequestHeaderRef.value.vxeTableRef.getRecordset().updateRecords;
        oldData = requestHeaderParameterTable.value.tableData;
    } else if (type == 2) {
        data = editRequestRef.value.vxeTableRef.getRecordset().updateRecords;
        oldData = requestParameterTable.value.tableData;
    } else if (type == 3) {
        data = editResponseRef.value.vxeTableRef.getRecordset().updateRecords;
        oldData = responseParameterTable.value.tableData;
    }
    let newData = []
    let index = 0;
    for (let it of oldData) {
        if (index < data.length && it.id == data[index].id) {
            newData.push(data[index])
            index++;
        } else {
            newData.push(it)
        }
    }
    if (isCheck) {
        let index = 1
        for (let it of newData) {
            if (!checkData(it.parameterKey)) {
                ElMessage({ type: 'warning', message: (type == 1 ? "请求头参数，" : type == 2 ? "请求参数，" : type == 3 ? "返回参数" : "") + '第' + index + '行，参数key不能为空' })
                return false;
            }
            if (!checkData(it.parameterType)) {
                ElMessage({ type: 'warning', message: (type == 1 ? "请求头参数" : type == 2 ? "请求参数" : type == 3 ? "返回参数" : "") + '第' + index + '行，参数类型不能为空' })
                return false;
            }
            if (!checkData(it.val)) {
                ElMessage({ type: 'warning', message: (type == 1 ? "请求头参数" : type == 2 ? "请求参数" : type == 3 ? "返回参数" : "") + '第' + index + '行，参数value不能为空' })
                return false;
            }
            index++;
        }
    }

    return newData;
}
//检查值是否为空
function checkData(data) {
    if (data == null || data == undefined || data == "") {
        return false;
    } else {
        return true;
    }
}
//删除参数table数据
function delById(type, id) {
    let data = []
    let newData = []
    if (type == 1) {
        data = parameterDatas(type, false);
        for (let it of data) {
            if (it.id != id) {
                newData.push(it)
            }
        }
        requestHeaderParameterTable.value.tableData = newData;
    } else if (type == 2) {
        data = parameterDatas(type, false);
        for (let it of data) {
            if (it.id != id) {
                newData.push(it)
            }
        }
        requestParameterTable.value.tableData = newData;
    } else if (type == 3) {
        data = parameterDatas(type, false);
        for (let it of data) {
            if (it.id != id) {
                newData.push(it)
            }
        }
        responseParameterTable.value.tableData = newData;
    }
}

//打开配置信息
function openView(type) {
    if (type == 1) {
        limitInfo.value = true
        limitInfoForm.value = JSON.parse(JSON.stringify(oldLimitInfoForm.value))
    } else {
        openDialog.value = true
    }
}
//关闭配置信息
function closeDialog(type) {
    if (type == 1) {
        limitInfo.value = false
        limitInfoForm.value = JSON.parse(JSON.stringify(oldLimitInfoForm.value))
    } else if (type == 2) {
        webService.value = false
        let jsData = JSON.parse(JSON.stringify(oldWebServiceForm.value))
        webServiceForm.value.method = jsData.method
        webServiceForm.value.nameSpace = jsData.nameSpace
        webServiceForm.value.webSpecification = jsData.webSpecification
    }
}
//确定配置信息
function confirDialog(type) {
    if (type == 1) {
        limitInfoRef.value.validate((valid) => {
            if (valid) {
                limitInfo.value = false
                oldLimitInfoForm.value = JSON.parse(JSON.stringify(limitInfoForm.value))
            } else {
                return false
            }
        })
    } else if (type == 2) {
        webServiceRef.value.validate((valid) => {
            if (valid) {
                webService.value = false
                oldWebServiceForm.value = JSON.parse(JSON.stringify(webServiceForm.value))
            } else {
                return false
            }
        })
    }
}

//新增按钮
async function addDialog() {
    ruleFormConfig.value.model = {}
    ruleFormConfig.value.model.networkAgreement = "http"
    ruleFormConfig.value.model.interfaceUrl = ""
    ruleFormConfig.value.model.interfaceType = "Rest"
    ruleFormConfig.value.model.interfaceMethod = "post"
    ruleFormConfig.value.model.isResponseFile = "false"
    initFormData()
    let res = await getInterfaceId()
    ruleFormConfig.value.model.id = res.data
    discardInterfaceId.value = res.data
    interfaceId.value = res.data
    // for (let it of ruleFormConfig.value.itemList) {
    //     if (it.props == undefined) {
    //         it.props = {
    //             disabled: false
    //         }
    //     } else {
    //         it.props.disabled = false
    //     }
    // }
    analysisYableData('[]', false)
    analysisTreeData(res)
}
addDialog()
//编辑按钮
async function edit(id) {
    initFormData()
    let para = {
        id: id
    }
    let res = await getInterfaceInfoById(para)
    ruleFormConfig.value.model = res.data
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props == undefined) {
            it.props = {
                disabled: false
            }
        } else {
            it.props.disabled = false
        }
    }
    initData(res.data)
    analysisYableData(res.data.parameters, false)
    analysisTreeData(res)
    addDialogConfig.value.title = computed(() => t('编辑接口信息'))
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.show = true
}
//版本升级按钮
async function updateVersion(id) {
    initFormData()
    let para = {
        id: id
    }
    let res = await getInterfaceInfoById(para)
    ruleFormConfig.value.model = res.data
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props == undefined) {
            it.props = {
                disabled: false
            }
        } else {
            it.props.disabled = false
        }
    }
    initData(res.data)
    analysisYableData(res.data.parameters, false)
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
        props: {
            slotName: "isOverwrite"
        }
    }
    ruleFormConfig.value.itemList.push(isOverwriteItem)
    addDialogConfig.value.title = computed(() => t('接口版本升级维护信息'))
    addDialogConfig.value.okText = "保存"
    addDialogConfig.value.show = true
}
//详情按钮
async function view(id, show) {
    initFormData()
    let para = {
        id: id
    }
    let res = await getInterfaceInfoById(para)
    ruleFormConfig.value.model = res.data
    for (let it of ruleFormConfig.value.itemList) {
        if (it.props == undefined) {
            it.props = {
                disabled: true
            }
        } else {
            it.props.disabled = true
        }
    }
    initData(res.data)
    analysisYableData(res.data.parameters, true)
    analysisTreeData(res)
    addDialogConfig.value.okText = false
    addDialogConfig.value.title = computed(() => t('查看接口信息'))
    addDialogConfig.value.show = true
    props.isShow = show
}

//解析请求参数json数据并赋值
function analysisYableData(jsonData, isDisabled) {
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
    requestHeaderParameterTable.value.editConfig = isView.value ? {
        trigger: 'click',
        enable: true,
        mode: "cell"
    } : false
    requestHeaderParameterTable.value.tableData = jzData1

    requestParameterTable.value.editConfig = isView.value ? {
        trigger: 'click',
        enable: true,
        mode: "cell"
    } : false
    requestParameterTable.value.tableData = jzData2

    responseParameterTable.value.editConfig = isView.value ? {
        trigger: 'click',
        enable: true,
        mode: "cell"
    } : false
    responseParameterTable.value.tableData = jzData3
}

//解析参数json树形数据并赋值
function analysisTreeData(res) {
    if (res.data.reqParameters != undefined) {
        reqData.value = JSON.parse(res.data.reqParameters)
    } else {
        reqData.value = []
    }
    if (res.data.resParameters != undefined) {
        resData.value = JSON.parse(res.data.resParameters)
    } else {
        resData.value = []
    }
}
//初始化switch组件的值
function initData(data, isDisabled) {
    if (data.isLimit == "是") {
        value1.value = true
    } else {
        value1.value = false
    }
    if (data.isAuth == "是") {
        value3.value = true
    } else {
        value3.value = false
    }
    if (data.isLimitData == "是") {
        value2.value = true
    } else {
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
function initFormData() {
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
    for (let it of ruleFormConfig.value.itemList) {
        if (it.prop != "isOverwrite") {
            newData.push(it)
        }
    }
    ruleFormConfig.value.itemList = newData
}
//弹出提示信息
function openPromptDialog(title, msg) {
    ElMessageBox.alert(msg, title, {
        confirmButtonText: '确认'
    })
}
//递归遍历数据
function selectChild(child, arrayData) {
    if (child != undefined) {
        for (let it of child) {
            if (it.children instanceof Array) {
                selectChild(it.children, arrayData)
            }
            arrayData.push(it)
        }
    }
}
//打开webService补充页面
const openWebServiceDialog = () => {
    webService.value = true
    console.log(ruleFormRef.value.model)
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

            // data.limitInfo = limitInfoForm.value;
            let parameterData = parameterDatas(1, true);
            if (typeof parameterData == 'boolean') {
                loading.value = false

                return;
            }
            data.parameters = JSON.stringify(parameterData);
            //获取请求参数数据
            let reqData = parameterDatas(2, true);
            if (typeof reqData == 'boolean') {
                loading.value = false
                return;
            }

            data.reqParameters = JSON.stringify(reqData)
            let res = await testInterface(
                data
            );
            if (data.isResponseFile == "true") {
                const a = document.createElement('a')
                a.href = URL.createObjectURL(new Blob(res, { type: 'application/octet-stream' }))

                // 获取文件名
                const contentDisposition = res.headers.get('Content-Disposition');
                let fileName = '流文件';
                if (contentDisposition && contentDisposition.includes('attachment')) {
                    const match = contentDisposition.match(/filename="(["]*)"/);
                    if (match) {
                        fileName = match;
                    }
                }
                a.download = fileName
                a.click()
            } else {
                if (res.code == 0) {

                    ElMessage({
                        message: '接口请求成功',
                        type: 'success'
                    })
                    try {
                        res.data.data = JSON.parse(res.data.data)
                    } catch (e) {

                    }
                    jsonData.value = res.data
                    loading.value = false
                } else {
                    loading.value = false
                    jsonData.value = {}
                    openPromptDialog("接口请求失败", res.msg)
                }
            }
        } else {
            loading.value = false
        }
    });
}

//判断值为空
const checkDataIsBlank = (val) => {
    if (val == undefined || val == null || val == "") {
        return true;
    } else {
        return false;
    }
}
//取消接口注册数据
const cancelData = () => {
    loading.value = true
    router.push({ path: 'alreadyInterface' })
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
defineExpose({ addDialog, edit, view, updateVersion })
</script>
<style>
.leftMargin {
    margin-left: 5px;
}

.operate {
    cursor: pointer;
}
.elRow {
    display: flex;
    align-items: center !important;
}
.rowHeight{
    height: calc(100%);
}
.btnDiv{
    width: 100%;
    text-align: right;
}
</style>
<style scoped>
:deep(.y9-filter-item){
    margin-bottom: 10px!important;
}
.rtDataTitleDiv {
    border: 1px solid #ebeef5;
    padding: 8px 11px;
    background: #f5f7fa;
    color: #606266;
    height: 42px;
    display: flex;
    align-items: center !important;
    vertical-align: middle;text-align:center;
}
.rtDataBackground {
    background-color: #ffffff;
    height: calc(100%);
}

.rtDataContent {
    border: 1px solid #ebeef5;
    height: calc(100% - 82px);
    padding: 10px;
}

.rtDataTitle {
    color: #555555;
    font-size: 14px;
}
</style>
<p align="center">
 <img alt="logo" src="https://vue.youshengyun.com/files/img/qrCodeLogo.png">
</p>
<p align="center">基于SpringBoot+Vue前后端分离的接口管理平台</p>
<p align="center">
 <a href='https://gitee.com/risesoft-y9/y9-interface-platform/stargazers'><img src='https://gitee.com/risesoft-y9/y9-interface-platform/badge/star.svg?theme=dark' alt='star'></img></a>
    <img src="https://img.shields.io/badge/version-v9.6.8-yellow.svg">
    <img src="https://img.shields.io/badge/Spring%20Boot-2.7-blue.svg">
    <img alt="logo" src="https://img.shields.io/badge/Vue-3.3-red.svg">
    <img alt="" src="https://img.shields.io/badge/JDK-11-green.svg">
    <a href="https://gitee.com/risesoft-y9/y9-core/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/license-GPL3-blue.svg"></a>
    <img src="https://img.shields.io/badge/total%20lines-810.2k-blue.svg">
</p>

## 简介

接口管理通过接口的注册、审批、集市和日志等方式对接口共享进行全生命周期的管理。接口管理注重流程化的接口共享模式和支持大规模接口转发的部署模式，从而面向多接口来源、高流量交互和复杂组织结构的场景。接口管理同时是一个轻量化的内核，支持各种面向特定场景的定制化业务改造。接口管理需要依赖开源的数字底座进行相关数据目录权限的管控。[系统在线体验----->>>>>](#在线体验)


## 开源地址

源码地址：<https://github.com/risesoft-y9/API-Platform>

## 源码目录

```
vue -- 前端工程
 ├── y9vue-interfacePlatform -- 接口管理平台前端项目
y9-module-interface -- 后端工程
 ├── risenet-y9boot-support-interface-jpa-repository  -- 接口管理平台实体类项目
 ├── risenet-y9boot-webapp-interface-platform-execute -- 接口管理平台执行端项目
 ├── risenet-y9boot-webapp-interface-platform-manager -- 接口管理平台管理端项目
```

## 逻辑架构图

<div><img src="https://vue.youshengyun.com/files/interfacePlatform/img/interfacePlatform_logic_framework.png"></div>

1. 接口生产方（接口提供方）通过接口注册和接口发布申请对接口进入集市进行配置和准备

2. 接口消费方（接口调用方）通过对接口集市中的接口进行调用申请，从而在后续流程中可以对接并使用接口

3. 接口管理方负责对接口的审批、测试、管理、校验、限流、转发、监控，其中执行端根据管理端的配置做无状态的执行

## 功能架构图

<div><img src="https://vue.youshengyun.com/files/interfacePlatform/img/interfacePlatform_function_frameword.png"></div>

## 部署架构图

<div><img src="https://vue.youshengyun.com/files/interfacePlatform/img/interfacePlatform_deploy.png"></div>

1. 管理端可以平行部署，执行端可以根据接口数量和单体流量进行分区部署

2. 接口管理需要数字底座进行人员权限控制和数据字典控制

3. 接口管理支持容器化方式部署

4. 接口管理的性能指标随执行端的扩展而增加，单体的效率由总体接口转发的频繁程度和传输数据量来衡量

## 产品特点

### 流程化接口审审批
已内嵌基本的接口注册审批（由系统管理员审批），支持对接工作流引擎进行复杂流程、多级机构、正式表单的接口审批流。

### 管执分离部署
管理端和执行端分开部署，管理端负责注册调用、全局配置、日志汇集；执行端无状态进行接口转发、校验和限流，从而保护接口提供方。

### 接口限流与校验
支持对接口进行不同策略的限流，支持对接口中参数进行数据校验。

### 白名单与密钥对管控
利用IP白名单限制和接口提供方密钥对认证的方式提高本项目的安全系数，防御一定的入侵行为。

## 功能描述

| 序号  | 功能名称 | 功能描述 |
|-----|------|------|
| 1   |   首页监控   |  提供接口调用趋势、日志播报、调用异常情况展示    |
| 2   |   接口注册   |   支持用户和管理员对接口进行注册和配置   |
| 3   |   接口集市   |  展示和管理所有已经注册的接口    |
| 4   |   接口审批   |  对申请注册和申请调用的接口进行基础的审批功能，支持对接工作流进行复杂的审批流程    |
| 5   |   接口测试   |  对单个接口的情况进行测试    |
| 6   |   日志监控   |   支持对所有接口调用情况的日志汇总和查找   |
| 7   |   管理中心   |   根据已注册接口、已发布接口、已申请接口和已停用接口四个分类对用户和管理员进行分别展示   |
| 8   |   白名单管理   |   管理执行端和接口提供方的IP白名单   |
| 9   |   接口限流	   |  支持对需要调用的接口进行限流配置，从而在转发前进行流量管制    |
| 10  |   接口校验   |  支持对接口中的字段、参数进行一定程度的校验，从而满足细颗粒的接口调用、标准和管理需求    |
| 11  |   系统标识管理   |   对注册至本平台的第三方系统进行标识和管理   |
| 12  |   接口转发   |   在执行端无状态的情况下，依据管理端的配置进行接口的实际转发，保护接口提供方的信息   |


## 后端技术选型

| 序号  | 依赖              | 版本      | 官网                                                                                                                 |
|-----|-----------------|---------|--------------------------------------------------------------------------------------------------------------------|
| 1   | Spring Boot     | 2.7.10  | <a href="https://spring.io/projects/spring-boot" target="_blank">官网</a>                                            |
| 2   | SpringDataJPA   | 2.7.10  | <a href="https://spring.io/projects/spring-data-jpa" target="_blank">官网</a>                                        |
| 3   | SpringDataRedis | 2.7.10  | <a href="https://spring.io/projects/spring-data-redis" target="_blank">官网</a>                                      |
| 4   | druid           | 1.2.16  | <a href="https://github.com/alibaba/druid/wiki/%E9%A6%96%E9%A1%B5" target="_blank">官网</a>                          |
| 5   | Jackson         | 2.13.5  | <a href="https://github.com/FasterXML/jackson-core" target="_blank">官网</a>                                         |
| 6   | javers          | 6.13.0  | <a href="https://github.com/javers/javers" target="_blank">官网</a>                                                  |
| 7   | lombok          | 1.18.26 | <a href="https://projectlombok.org/" target="_blank">官网</a>                                                        |
| 8   | logback         | 1.2.11  | <a href="https://www.docs4dev.com/docs/zh/logback/1.3.0-alpha4/reference/introduction.html" target="_blank">官网</a> |

## 前端技术选型

| 序号 | 依赖           | 版本      | 官网                                                                     |
|----|--------------|---------|------------------------------------------------------------------------|
| 1  | vue          | 3.3.2   | <a href="https://cn.vuejs.org/" target="_blank">官网</a>                 |
| 2  | vite4        | 4.4.9   | <a href="https://vitejs.cn/" target="_blank">官网</a>                    |
| 3  | vue-router   | 4.0.13  | <a href="https://router.vuejs.org/zh/" target="_blank">官网</a>          |
| 4  | pinia        | 2.0.11  | <a href="https://pinia.vuejs.org/zh/" target="_blank">官网</a>           |
| 5  | axios        | 0.24.0  | <a href="https://www.axios-http.cn/" target="_blank">官网</a>            |
| 6  | typescript   | 4.5.4   | <a href="https://www.typescriptlang.org/" target="_blank">官网</a>       |
| 7  | core-js      | 3.20.1  | <a href="https://www.npmjs.com/package/core-js" target="_blank">官网</a> |
| 8  | element-plus | 2.2.29  | <a href="https://element-plus.org/zh-CN/" target="_blank">官网</a>       |
| 9  | sass         | 1.58.0  | <a href="https://www.sass.hk/" target="_blank">官网</a>                  |
| 10 | animate.css  | 4.1.1   | <a href="https://animate.style/" target="_blank">官网</a>                |
| 11 | vxe-table    | 4.3.5   | <a href="https://vxetable.cn" target="_blank">官网</a>                   |
| 12 | echarts      | 5.3.2   | <a href="https://echarts.apache.org/zh/" target="_blank">官网</a>        |
| 13 | svgo         | 1.3.2   | <a href="https://github.com/svg/svgo" target="_blank">官网</a>           |
| 14 | lodash       | 4.17.21 | <a href="https://lodash.com/" target="_blank">官网</a>                   |

## 中间件选型

| 序号 | 工具               | 版本   | 官网                                                                        |
|----|------------------|------|---------------------------------------------------------------------------|
| 1  | JDK              | 11   | <a href="https://openjdk.org/" target="_blank">官网</a>                     |
| 2  | Tomcat           | 9.0+ | <a href="https://tomcat.apache.org/" target="_blank">官网</a>               |

## 数据库选型

| 序号 | 工具            | 版本         | 官网                                                                        |
|----|---------------|------------|---------------------------------------------------------------------------|
| 1  | Mysql         | 5.7 / 8.0+ | <a href="https://www.mysql.com/cn/" target="_blank">官网</a>                |
| 2  | Redis         | 6.2+       | <a href="https://redis.io/" target="_blank">官网</a>                        |


## 信创兼容适配

| **序号** | 类型   | 对象                 |
|:-------|------|--------------------|
| 1      | 浏览器  | 奇安信、火狐、谷歌、360等     |
| 2      | 插件   | 金山、永中、数科、福昕等       |
| 3      | 中间件  | 东方通、金蝶、宝兰德等        |
| 4      | 数据库  | 人大金仓、达梦、高斯等        |
| 5      | 操作系统 | 统信、麒麟、中科方德等        |
| 6      | 芯片   | ARM体系、MIPS体系、X86体系 |

<a name="在线体验"></a>

## 在线体验

演示地址：<https://demo.youshengyun.com/interface/>

> 演示账号：
>
> 系统管理员：systemManager 密码：Risesoft@2024
> 
> 普通用户：user 密码：Risesoft@2024

>使用说明：
> 
> 1.接入接口管理平台的接口调用方式，请参考：文档专区-接口管理平台调用文档
> 
> 2.体验环境，可以使用以下地址对已申请接口进行调用测试：
>
>https://demo-api.youshengyun.com/interfaceexecute/openInterface/forward
>
> 3.本地部署的接口管理平台调用地址请按照文档说明操作
> 


## 文档专区

| 序号  | 名称                                                                                                              |
|:----|-----------------------------------------------------------------------------------------------------------------|
| 1   | <a href="https://vue.youshengyun.com/files/单点登录对接文档.pdf" target="_blank">单点登录对接文档</a>                           |
| 2   | <a href="https://vue.youshengyun.com/files/数字底座接口文档.pdf" target="_blank">数字底座接口文档</a>                           |
| 3   | <a href="https://vue.youshengyun.com/files/interfacePlatform/接口管理平台部署文档V1.1.pdf" target="_blank">接口管理平台部署文档</a> |
| 4   | <a href="https://vue.youshengyun.com/files/interfacePlatform/接口管理产品操作手册.pdf" target="_blank">接口管理产品操作手册</a>     |
| 5   | <a href="https://vue.youshengyun.com/files/interfacePlatform/接口管理平台调用文档V1.1.pdf" target="_blank">接口管理平台调用文档</a> |
| 6   | <a href="https://vue.youshengyun.com/files/内部Java开发规范手册.pdf" target="_blank">内部Java开发规范手册</a>                   |
| 7   | <a href="https://vue.youshengyun.com/files/日志组件使用文档.pdf" target="_blank">日志组件使用文档</a>                           |
| 8   | <a href="https://vue.youshengyun.com/files/文件组件使用文档.pdf" target="_blank">文件组件使用文档</a>                           |
| 9   | <a href="https://vue.youshengyun.com/files/代码生成器使用文档.pdf" target="_blank">代码生成器使用文档</a>                         |
| 10  | <a href="https://vue.youshengyun.com/files/配置文件说明文档.pdf" target="_blank">配置文件说明文档</a>                           |
| 11  | <a href="https://vue.youshengyun.com/files/常用工具类使用示例文档.pdf" target="_blank">常用工具类使用示例文档</a>                     |
| 12  | <a href="https://vue.youshengyun.com/files/有生博大Vue开发手册v1.0.pdf" target="_blank">前端开发手册</a>                      |
| 13  | <a href="https://vue.youshengyun.com/files/开发规范.pdf" target="_blank">前端开发规范</a>                                 |
| 14  | <a href="https://vue.youshengyun.com/files/代码格式化.pdf" target="_blank">前端代码格式化</a>                               |
| 15  | <a href="https://vue.youshengyun.com/files/系统组件.pdf" target="_blank">前端系统组件</a>                                 |
| 16  | <a href="https://vue.youshengyun.com/files/通用方法.pdf" target="_blank">前端通用方法</a>                                 |
| 17  | <a href="https://vue.youshengyun.com/files/国际化.pdf" target="_blank">前端国际化</a>                                   |
| 18  | <a href="https://vue.youshengyun.com/files/Icon图标.pdf" target="_blank">前端Icon图标</a>                             |
| 19  | <a href="https://vue.youshengyun.com/files/Oracle数据库适配文档.pdf" target="_blank">Oracle数据库适配文档</a>                 |
| 20  | <a href="https://vue.youshengyun.com/files/Dameng数据库适配文档.pdf" target="_blank">Dameng数据库适配文档</a>                 |
| 21  | <a href="https://vue.youshengyun.com/files/PostgreSQL数据库适配文档.pdf" target="_blank">PostgreSQL数据库适配文档</a>         |
| 22  | <a href="https://vue.youshengyun.com/files/Kingbase数据库适配文档.pdf" target="_blank">Kingbase数据库适配文档</a>             |
| 23  | <a href="https://vue.youshengyun.com/files/Mariadb数据库适配文档.pdf" target="_blank">Mariadb数据库适配文档</a>               |
| 24  | <a href="https://vue.youshengyun.com/files/OceanBase数据库适配文档.pdf" target="_blank">OceanBase数据库适配文档</a>           |

## 接口管理平台截图

#### 界面截图

<table>
    <tr>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/1.png"></td>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/2.png"></td>
    </tr>
    <tr>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/3.png"></td>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/4.png"></td>
    </tr>
    <tr>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/5.png"></td>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/6.png"></td>
    </tr>
    <tr>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/7.png"></td>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/8.png"></td>
    </tr>
    <tr>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/9.png"></td>
        <td><img src="https://vue.youshengyun.com/files/interfacePlatform/img/10.png"></td>
    </tr>
</table>

## 依赖开源项目

| 序&nbsp;号 | 项&nbsp;目&nbsp;&nbsp;名&nbsp;称          | 项目介绍           | 地&nbsp;址                                                         |
| ----- | ----------- | ----------------------------------------- | ----------- |
| 1    | 数字底座 | 数字底座是一款面向大型政府、企业数字化转型，基于身份认证、组织架构、岗位职务、应用系统、资源角色等功能构建的统一且安全的管理支撑平台。数字底座基于三员管理模式，具备微服务、多租户、容器化和国产化，支持用户利用代码生成器快速构建自己的业务应用，同时可关联诸多成熟且好用的内部生态应用。      | <a href="https://gitee.com/risesoft-y9/y9-core" target="_blank">码云地址</a> |

## 赞助与支持

### 中关村软件和信息服务产业创新联盟

官网：<a href="https://www.zgcsa.net" target="_blank">https://www.zgcsa.net</a>

### 北京有生博大软件股份有限公司

官网：<a href="https://www.risesoft.net/" target="_blank">https://www.risesoft.net/</a>

### 中国城市发展研究会

官网：<a href="https://www.china-cfh.com/" target="_blank">https://www.china-cfh.com/</a>

## 咨询与合作

联系人：曲经理

微信号：qq349416828

备注：开源产品咨询-姓名
<div><img style="width: 40%" src="https://vue.youshengyun.com/files/dataflow/img/qjfewm.png"></div>
联系人：有生博大-咨询热线

座机号：010-86393151
<div><img style="width: 45%" src="https://vue.youshengyun.com/files/img/有生博大-咨询热线.png"></div>

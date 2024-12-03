package net.risesoft.y9public.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 接口调用日志记录实体类
 *
 * @author SLlbbc
 */
@NoArgsConstructor
@Data
@Entity
@Table(name = "y9_call_api_request_log_info")
@org.hibernate.annotations.Table(comment = "接口日志表", appliesTo = "y9_call_api_request_log_info")
public class CallApiRequestLogInfo implements Serializable {

    private static final long serialVersionUID = 347197006624364417L;

    /**
     * 主键
     */
    @Id
    private String id;

    /**
     * 接口类型
     * webservice/rest
     */
    @Column(name = "INTERFACETYPE", columnDefinition = "varchar(36) default '' comment '接口类型'")
    @Comment(value = "接口类型")
    private String interfaceType;

    /**
     * 接口id
     */
    @Column(name = "INTERFACEID", columnDefinition = "varchar(100) default '' comment '接口id'")
    @Comment(value = "接口id")
    private String interfaceId;

    /**
     * 请求接口名称
     * 需要添加该接口的中文名称
     */
    @Column(name = "APINAME", columnDefinition = "varchar(200) default '' comment '请求接口名称'")
    @Comment(value = "请求接口名称")
    private String apiName;

    /**
     * 调用用户名称
     */
    @Column(name = "REQUESTUSERNAME", columnDefinition = "varchar(200) default '' comment '调用用户名称'")
    @Comment(value = "调用用户名称")
    private String requestUserName;

    /**
     * 调用用户标识
     */
    @Column(name = "REQUESTUSERID", columnDefinition = "varchar(200) default '' comment '调用用户标识'")
    @Comment(value = "调用用户标识")
    private String requestUserId;

    /**
     * 用户标识
     */
    @Column(name = "USERKEY", columnDefinition = "varchar(100) default '' comment '用户标识'")
    @Comment(value = "用户标识")
    private String userKey;

    /**
     * 用户名称
     */
    @Column(name = "USERNAME", columnDefinition = "varchar(100) default '' comment '用户名称'")
    @Comment(value = "用户名称")
    private String userName;

    /**
     * 部门名称
     */
    @Column(name = "DEPTNAME", columnDefinition = "varchar(100) default '' comment '部门名称'")
    @Comment(value = "部门名称")
    private String deptName;

    /**
     * 法院名称
     */
    @Column(name = "COURTNAME", columnDefinition = "varchar(100) default '' comment '法院名称'")
    @Comment(value = "法院名称")
    private String courtName;

    /**
     * 请求开始时间戳
     */
    @Column(name = "REQUESTSTARTTIME", columnDefinition = "datetime(6) comment '请求开始时间'")
    @Comment(value = "请求开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date requestStartTime;

    /**
     * 请求结束时间戳
     */
    @Column(name = "REQUESTENDTIME", columnDefinition = "datetime(6) comment '请求结束时间'")
    @Comment(value = "请求结束时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date requestEndTime;

    /**
     * 请求参数
     */
    @Column(name = "REQUESTPARAM", columnDefinition = "varchar(100) default '' comment '请求参数'")
    @Comment(value = "请求参数")
    private String requestParam;

    /**
     * 接口响应情况
     */
    @Column(name = "RESPONSEMSG", columnDefinition = "text  comment '接口响应情况'")
    @Comment(value = "接口响应情况")
    private String responseMsg;

    /**
     * 响应代码
     */
    @Column(name = "RESPONSECODE", columnDefinition = "varchar(50) default '' comment '响应代码'")
    @Comment(value = "响应代码")
    private String responseCode;


    /**
     * 报错信息
     */
    @Column(name = "ERRORMESSAGE", columnDefinition = "text  comment '报错信息'")
    @Comment(value = "报错信息")
    private String errorMessage;

    /**
     * 被请求的服务器IP地址
     */
    @Column(name = "SERVERIP", columnDefinition = "varchar(50) default '' comment '被请求的服务器IP地址'")
    @Comment(value = "被请求的服务器IP地址")
    private String serverIP;

    /**
     * 请求者IP地址
     */
    @Column(name = "REQUESTIP", columnDefinition = "varchar(50) default '' comment '请求者IP地址'")
    @Comment(value = "请求者IP地址")
    private String requestIP;

    /**
     * 操作描述
     */
    @Column(name = "OPENERDES", columnDefinition = "varchar(100) default '' comment '操作描述'")
    @Comment(value = "操作描述")
    private String openerDes;

    /**
     * 鉴权结果
     */
    @Column(name = "AUTHENTICATIONRESULT", columnDefinition = "varchar(20) default '' comment '鉴权结果'")
    @Comment(value = "鉴权结果")
    private String authenticationResult;

    /**
     * 是否鉴权
     */
    @Column(name = "AUTHENTIC", columnDefinition = "varchar(2) default '0' comment '是否鉴权;0表示否，1表示是'")
    @Comment(value = "是否鉴权")
    private Integer authentic;

    /**
     * 是否限流
     */
    @Column(name = "ISLIMIT", columnDefinition = "varchar(2) default '0' comment '是否限流;0表示否，1表示是'")
    @Comment(value = "是否限流")
    private Integer isLimit;

    /**
     * 限流结果
     */
    @Column(name = "LIMITRESULT", columnDefinition = "text  comment '限流结果'")
    @Comment(value = "限流结果")
    private String limitResult;

    /**
     * 限流等待时长
     */
    @Column(name = "LIMITTIME", columnDefinition = "varchar(50) default ''  comment '限流等待时长'")
    @Comment(value = "限流等待时长")
    private String limitTime;

    /**
     * 申请系统
     */
    @Column(name = "APPLYSYSTEMNAME", columnDefinition = "varchar(50) default ''  comment '申请系统'")
    @Comment(value = "申请系统")
    private String applySystemName;

    /**
     * 申请系统标识
     */
    @Column(name = "APPLYSYSTEMNAMEID", columnDefinition = "varchar(50) default ''  comment '申请系统标识'")
    @Comment(value = "申请系统标识")
    private String applySystemNameId;

    /**
     * 转发开始时间戳
     */
    @Column(name = "FORWARDSTARTTIME", columnDefinition = "datetime(6) comment '转发开始时间'")
    @Comment(value = "转发开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date forwardStartTime;


    /**
     * 转发响应情况
     */
    @Column(name = "FORWARDRESPONSEMSG", columnDefinition = "varchar(200) default ''  comment '转发响应情况'")
    @Comment(value = "转发响应情况")
    private String forwardResponseMsg;

    /**
     * 转发响应代码
     */
    @Column(name = "FORWARDRESPONSECODE", columnDefinition = "varchar(50) default ''  comment '转发响应代码'")
    @Comment(value = "转发响应代码")
    private String forwardResponseCode;

    /**
     * 转发响应数据
     */
    @Column(name = "FORWARDRESPONSEDATA", columnDefinition = "text  comment '转发响应数据'")
    @Comment(value = "转发响应数据")
    private String forwardResponseData;

    /**
     * 转发报错信息
     */
    @Column(name = "FORWARDERRMSG", columnDefinition = "text  comment '转发报错信息'")
    @Comment(value = "转发报错信息")
    private String forwardErrMsg;

    /**
     * 转发参数
     */
    @Column(name = "FORWARDREQUESTPARAM", columnDefinition = "text  comment '转发参数'")
    @Comment(value = "转发参数")
    private String forwardRequestParam;
}

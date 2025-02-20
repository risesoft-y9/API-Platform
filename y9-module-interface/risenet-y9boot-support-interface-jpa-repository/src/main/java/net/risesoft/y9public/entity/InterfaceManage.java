package net.risesoft.y9public.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_INTERFACE_MANAGE_INFO")
@org.hibernate.annotations.Table(comment = "接口管理信息", appliesTo = "Y9_INTERFACE_MANAGE_INFO")
public class InterfaceManage extends BaseEntity implements Serializable{
	
	private static final long serialVersionUID = 3226632146259274287L;

	@Id
	@Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
	private String id;

	@Column(name = "INTERFACE_NAME", columnDefinition = "varchar(100) default '' comment '接口名称'")
	@Comment(value = "接口名称")
	private String interfaceName;

	@Column(name = "SAME_INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '同一接口id'")
	@Comment(value = "接口id")
	private String sameInterfaceId;

	@Column(name = "INTERFACE_URL", columnDefinition = "varchar(500) default '' comment '接口调用地址'")
	@Comment(value = "接口调用地址")
	private String interfaceUrl;

	@Column(name = "IS_AUTH", columnDefinition = "varchar(10) default '' comment '是否鉴权'")
	@Comment(value = "是否鉴权")
	private String isAuth;

	@Column(name = "AUTH_INFO", columnDefinition = "varchar(1000) default '' comment '鉴权信息'")
	@Comment(value = "鉴权信息")
	private String authInfo;

	@Column(name = "IS_LIMIT", columnDefinition = "varchar(10) default '' comment '是否限流'")
	@Comment(value = "是否限流")
	private String isLimit;

	@Column(name = "IS_LIMIT_DATA", columnDefinition = "varchar(10) default '' comment '是否控制数据权限范围'")
	@Comment(value = "是否控制数据权限范围")
	private String isLimitData;

	@Column(name = "ILLUSTRATE", columnDefinition = "varchar(500) default '' comment '接口说明'")
	@Comment(value = "接口说明")
	private String illustrate;

	@Column(name = "INTERFACE_TYPE", columnDefinition = "varchar(50) default '' comment '接口类型'")
	@Comment(value = "接口类型")
	private String interfaceType;

	@Column(name = "NETWORK_AGREEMENT", columnDefinition = "varchar(50) default '' comment '网络协议'")
	@Comment(value = "网络协议")
	private String networkAgreement;

	@Column(name = "INTERFACE_STATUS", columnDefinition = "varchar(10) default '' comment '接口状态'")
	@Comment(value = "接口状态")
	private String interfaceStatus;

	@Column(name = "INTERFACE_METHOD", columnDefinition = "varchar(10) default '' comment '请求方式'")
	@Comment(value = "请求方式")
	private String interfaceMethod;

	@Column(name = "VERSION", columnDefinition = "varchar(50) default '' comment '接口版本'")
	@Comment(value = "接口版本")
	private String version;

	@Column(name = "NOTES", columnDefinition = "varchar(254) default '' comment '备注'")
	@Comment(value = "备注")
	private String notes;

	@Column(name = "PERSON_NAME", columnDefinition = "varchar(50) default '' comment '接口填报人'")
	@Comment(value = "接口填报人")
	private String personName;

	@Column(name = "PERSON_ID", columnDefinition = "varchar(36) default '' comment '接口填报人ID'")
	@Comment(value = "接口填报人ID")
	private String personId;

	@Column(name = "DEPT_INFO", columnDefinition = "varchar(254) default '' comment '接口提供公司名称'")
	@Comment(value = "接口提供公司名称")
	private String deptInfo;

	@Column(name = "DEPT_ID", columnDefinition = "varchar(50) default '' comment '接口提供公司id'")
	@Comment(value = "接口提供公司id")
	private String deptId;

	@Column(name = "SYSTEM_NAME", columnDefinition = "varchar(254) default '' comment '接口提供系统名称'")
	@Comment(value = "接口提供系统名称")
	private String systemName;

	@Column(name = "SYSTEM_ID", columnDefinition = "varchar(50) default '' comment '接口提供系统标识'")
	@Comment(value = "接口提供系统标识")
	private String systemId;

	@Column(name = "HEAD", columnDefinition = "varchar(50) default '' comment '接口负责人名称'")
	@Comment(value = "接口负责人名称")
	private String head;

	@Column(name = "HEAD_PHONE", columnDefinition = "varchar(50) default '' comment '接口负责人'")
	@Comment(value = "接口负责人联系方式")
	private String headPhone;

	@Column(name = "IS_OVERWRITE", columnDefinition = "varchar(10) default '' comment '是否覆盖更新Y覆盖，N未覆盖'")
	@Comment(value = "是否覆盖更新")
	private String isOverwrite;

	@Column(name = "OVERWRITE_INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '覆盖更新接口id'")
	@Comment(value = "覆盖更新接口id")
	private String overwriteInterfaceId;

	@Column(name = "INTERFACE_FILE_URL", columnDefinition = "varchar(500) default '' comment '接口文档下载地址'")
	@Comment(value = "接口文档下载地址")
	private String interfaceFileUrl;

	@Column(name = "INTERFACE_FILE_NAME", columnDefinition = "varchar(100) default '' comment '接口文档名称'")
	@Comment(value = "接口文档名称")
	private String interfaceFileName;

	@Column(name = "IS_DELETE", columnDefinition = "varchar(50) default 'N' comment '是否删除，Y删除，N未删除'")
	@Comment(value = "是否已经删除")
	private String isDelete;

	@Column(name = "IS_TEST", columnDefinition = "varchar(10) default '' comment '是否测试接口'")
	@Comment(value = "是否测试接口")
	private String isTest;

	@Column(name = "PARAMETER_IDS", columnDefinition = "varchar(2000) default '' comment '鉴权参数id串'")
	@Comment(value = "鉴权参数id串")
	private String parameterIds;

	@Column(name = "NAME_SPACE", columnDefinition = "varchar(300) default '' comment '命名空间'")
	@Comment(value = "命名空间")
	private String nameSpace;

	@Column(name = "METHOD", columnDefinition = "varchar(300) default '' comment 'webService调用方法'")
	@Comment(value = "webService调用方法")
	private String method;

	@Column(name = "WEB_SPECIFICATION", columnDefinition = "varchar(20) default '' comment 'webService开发规范协议'")
	@Comment(value = "webService开发规范协议")
	private String webSpecification;

	@Column(name = "IS_RESPONSE_FILE", columnDefinition = "varchar(10) default 'false' comment '是否返回文件'")
	@Comment(value = "是否返回文件")
	private Boolean isResponseFile;

	@Column(name = "EXECUTE_INSTANCE_ID", columnDefinition = "varchar(500) default '' comment '执行端实例ID'")
	@Comment(value = "执行实例ID")
	private String executeInstanceId;

	@Column(name = "EXECUTE_INSTANCE_ID_BACK", columnDefinition = "varchar(500) default '' comment '备选执行端实例ID'")
	@Comment(value = "备用实例ID")
	private String executeInstanceIdBack;

	public InterfaceManage(InterfaceManageDTO dto) {
		this.id = dto.getId();
		this.interfaceName = dto.getInterfaceName();
		this.sameInterfaceId = dto.getSameInterfaceId();
		this.interfaceUrl = dto.getInterfaceUrl();
		this.parameterIds = dto.getParameterIds();
		this.isAuth = dto.getIsAuth();
		this.isLimit = dto.getIsLimit();
		this.illustrate = dto.getIllustrate();
		this.interfaceType = dto.getInterfaceType();
		this.interfaceStatus = dto.getInterfaceStatus();
		this.interfaceMethod = dto.getInterfaceMethod();
		this.version = dto.getVersion();
		this.notes = dto.getNotes();
		this.personName = dto.getPersonName();
		this.personId = dto.getPersonId();
		this.deptInfo = dto.getDeptInfo();
		this.head = dto.getHead();
		this.isOverwrite = dto.getIsOverwrite();
		this.interfaceFileUrl = dto.getInterfaceFileUrl();
		this.isDelete = dto.getIsDelete();
		this.isTest = dto.getIsTest();
		this.networkAgreement = dto.getNetworkAgreement();
		this.headPhone = dto.getHeadPhone();
		this.overwriteInterfaceId = dto.getOverwriteInterfaceId();
		this.nameSpace = dto.getNameSpace();
		this.method = dto.getMethod();
		this.webSpecification = dto.getWebSpecification();
		this.isLimitData = dto.getIsLimitData();
		this.deptId = dto.getDeptId();
		this.systemId = dto.getSystemId();
		this.systemName = dto.getSystemName();
		this.isResponseFile = dto.getIsResponseFile();
		this.executeInstanceId = dto.getExecuteInstanceId();
		this.executeInstanceIdBack = dto.getExecuteInstanceIdBack();
	}
}


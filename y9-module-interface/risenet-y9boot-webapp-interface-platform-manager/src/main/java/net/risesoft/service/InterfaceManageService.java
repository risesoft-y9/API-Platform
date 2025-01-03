package net.risesoft.service;

import net.risesoft.y9public.dto.InterfaceApplyDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface InterfaceManageService {

    /**
     * 获取page数据
     * @param interfaceManageDTO
     * @return
     */
    Page<InterfaceManageDTO> getInterfaceList(InterfaceManageDTO interfaceManageDTO);

    //保存接口信息
    Map<String,Object> saveInterfaceInfo(InterfaceManage interfaceManage);

    //删除接口信息
    Map<String,String> delInterfaceInfo(String id);
    //删除权限信息
    Map<String,String> delAuthInfoById(String id);
    //根据id查询服务器信息
    InterfaceManageDTO getInterfaceInfoById(String id);

    //发布接口
    Map<String,Object> pubInterface(InterfaceApplyDTO apply);

    //停用接口
    Map<String,Object> stopInterface(InterfaceApplyDTO apply);

    Map<String,Object> saveInterfaceInfo(InterfaceManageDTO interfaceManageDTO);

    Map<String,Object> saveInterfaceFile(MultipartFile file);

    Map<String,Object> updateVersionInfo(InterfaceManageDTO interfaceManageDTO);

    //根据id查询权限信息
    List<Map<String,Object>> getAuthListByInterfaceId(String id,Boolean isVIew);

    //根据接口id获取必填参数信息
    List<Parameter> getRequiredParameterData(String id);

    //调用申请接口,flag为true是指新增申请，flag为false是指变更申请
    Map<String,Object> useInterfaceApply(InterfaceApplyDTO apply,Boolean flag);

    //根据接口id查询申请信息
    InterfaceApply getApplyInfoByInterfaceId(String id);

    //根据接口id查询接口信息并导出
    void exportFile(List<String> ids, HttpServletResponse response);

    //导入文件
    Map<String,Object> uploadFile(MultipartFile file,Boolean isOverWrite);

    //上传接口文档
    Map<String,Object> uploadFile(MultipartFile file,String interfaceId);

    //下载接口文档
    void downLoadInterfaceFile(String sameId,String version,String fileName, HttpServletResponse response);
}

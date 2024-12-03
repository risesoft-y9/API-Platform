package net.risesoft.service;

import net.risesoft.y9public.dto.ApproveDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.dto.ViewApproveDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.vo.ViewApproveVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ApproveService {

    /**
     * 获取page数据
     *
     * @param interfaceManageDTO
     * @return
     */
    Page<ApproveDTO> getApproveList(InterfaceManageDTO interfaceManageDTO);

    /**
     * 获取page数据
     *
     * @param viewApproveDTO
     * @return
     */
    Page<ViewApproveVo> getViewApproveList(ViewApproveDTO viewApproveDTO);

    //保存接口信息
    Map<String, Object> saveApproveInfo(Approve approve);

    //根据id查询服务器信息
    ApproveDTO getApproveById(String id);

    //根据id查询服务器信息
    Approve findApproveById(String id);

    //审批通过接口信息
    Map<String, Object> agreeApproveInfo(Approve approve);

    //审批拒绝接口信息
    Map<String, Object> refuseApproveInfo(Approve approve);

    //审批通过接口信息
    Map<String, Object> submitData(Approve approve);

    //根据接口id获取审批进度
    List<Approve> getApproveByInterfaceId(String id, Boolean applyType);

    //构建审批记录
    Map<String, Object> buildApprove(Approve approve, String flowId);
}

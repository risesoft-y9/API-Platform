package net.risesoft.service;

import net.risesoft.y9public.dto.ApproveDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.vo.ApplyVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface InterfaceApplyService {

    /**
     * 获取page数据
     *
     * @param interfaceManageDTO
     * @return
     */
    Page<ApproveDTO> getApproveList(InterfaceManageDTO interfaceManageDTO);

    //保存接口信息
    Map<String, Object> saveInfo(InterfaceApply apply);


    //生成接口调用申请审批信息
    Map<String, Object> createData(InterfaceApply apply, String interfaceStatus);

    //审批通过接口信息
    Map<String, Object> agreeApproveInfo(Approve approve);

    //审批拒绝接口信息
    Map<String, Object> refuseApproveInfo(Approve approve);

    //根据id查询申请信息
    InterfaceApply getApplyInfoById(String id);

    //获取已申请接口id
    List<String> getInterfaceIdsByPersonId();

    //获取对应接口的申请id
    List<String> getApplyIdsByPersonIdAndInterfaceId(String interfaceId);

    //获取对应接口的申请信息
    InterfaceApply getApplyInfoByPersonIdAndInterfaceId(String interfaceId);

    //根据id查询申请信息
    Page<ApplyVo> getApplyListById(String id, Integer page, Integer limit);

    //根据id查询判断当前登录人是否已经通过接口调用申请审批
    Boolean findIsPass(String id);

    //生成用户key和密钥
    void createSecret(InterfaceApply interfaceApply);
}

package net.risesoft.service.impl;

import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.ApplyType;
import net.risesoft.model.ApproveStatus;
import net.risesoft.model.InterfaceStatus;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.ApproveService;
import net.risesoft.util.RedissonUtil;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.ApproveDTO;
import net.risesoft.y9public.dto.InterfaceApplyDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.dto.ViewApproveDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.repository.*;
import net.risesoft.y9public.vo.ViewApproveVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Service(value = "ApproveService")
public class ApproveServiceImpl implements ApproveService {
    @Autowired
    private InterfaceManageRepository interfaceManageRepository;
    @Autowired
    private ApproveRepository approveRepository;
    @Autowired
    private InterfaceLimitInfoRepository interfaceLimitInfoRepository;
    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private ViewApproveRepository viewApproveRepository;
    @Autowired
    private InterfaceApplyRepository interfaceApplyRepository;

    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public Page<ApproveDTO> getApproveList(InterfaceManageDTO interfaceManageDTO) {
        Page<Approve> page = null;
        Page<ApproveDTO> page1 = null;
        Pageable pageable = PageRequest.of(interfaceManageDTO.getPage() - 1, interfaceManageDTO.getLimit());
        List<Approve> list = approveRepository.findList();
        if (StringUtils.isNotBlank(interfaceManageDTO.getInterfaceName())) {
            page = approveRepository.findListPage("%" + interfaceManageDTO.getInterfaceName() + "%", pageable);
        } else {
            page = approveRepository.findListPage("%" + "%", pageable);
        }
        List<String> listIds = new ArrayList<>();
        List<ApproveDTO> dtoList = new ArrayList<>();
        Map<String, InterfaceManage> map = new HashMap<>();
        for (Approve approve : page.getContent()) {
            listIds.add(approve.getInterfaceId());
            ApproveDTO approveDTO = new ApproveDTO(approve);
            dtoList.add(approveDTO);
        }
        List<InterfaceManage> manageList = new ArrayList<>();
        if (listIds.size() != 0) {
            manageList = interfaceManageRepository.findByIdIn(listIds);
            for (InterfaceManage interfaceManage : manageList) {
                map.put(interfaceManage.getId(), interfaceManage);
            }
            for (ApproveDTO approveDTO : dtoList) {
                if (map.get(approveDTO.getInterfaceId()) != null) {
                    approveDTO.setInterfaceName(map.get(approveDTO.getInterfaceId()).getInterfaceName());
                    approveDTO.setVersion(map.get(approveDTO.getInterfaceId()).getVersion());
                }
            }
        }
        page1 = new PageImpl<ApproveDTO>(dtoList, pageable, page.getTotalElements());
        return page1;
    }

    @Override
    public Page<ViewApproveVo> getViewApproveList(ViewApproveDTO viewApprove) {
        Specification<ViewApprove> spec = new Specification<ViewApprove>() {
            @Override
            public Predicate toPredicate(Root<ViewApprove> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(viewApprove.getInterfaceName())) {
                    predicates.add(criteriaBuilder.like(root.get("interfaceName"), "%" + viewApprove.getInterfaceName() + "%"));
                }
                if (StringUtils.isNotBlank(viewApprove.getApplyPersonId())) {
                    predicates.add(criteriaBuilder.equal(root.get("applyPersonId"), viewApprove.getApplyPersonId()));
                }
                if (StringUtils.isNotBlank(viewApprove.getInterfaceStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("interfaceStatus"), viewApprove.getInterfaceStatus()));
                }
                if (StringUtils.isNotBlank(viewApprove.getApplyType())) {
                    predicates.add(criteriaBuilder.equal(root.get("applyType"), viewApprove.getApplyType()));
                }
                if (StringUtils.isNotBlank(viewApprove.getApproveStatus()) && ("-1".equals(viewApprove.getApproveStatus()) || "-2".equals(viewApprove.getApproveStatus()))) {
                    if ("-1".equals(viewApprove.getApproveStatus())) {
                        predicates.add(criteriaBuilder.equal(root.get("isOver"), "N"));
                    }
                    if ("-2".equals(viewApprove.getApproveStatus())) {
                        predicates.add(criteriaBuilder.equal(root.get("isOver"), "Y"));

                    }
                } else if (StringUtils.isNotBlank(viewApprove.getApproveStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("approveStatus"), viewApprove.getApproveStatus()));
                }
                if (viewApprove.getStartDate() != null && viewApprove.getEndDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        predicates.add(criteriaBuilder.between(root.get("applyTime"), sdf.parse(viewApprove.getStartDate()), sdf.parse(viewApprove.getEndDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(viewApprove.getPage() - 1, viewApprove.getLimit(), Sort.by("isOver").ascending().and(Sort.by("updateTime").descending()));
        Page<ViewApprove> pageData = viewApproveRepository.findAll(spec, pageable);
        List<ViewApprove> list = pageData.getContent();
        List<ViewApproveVo> data = new ArrayList<>();
        for (ViewApprove viewApprove1 : list) {
            ViewApproveVo viewApproveVo = new ViewApproveVo(viewApprove1);
            if(StringUtils.isNotBlank(viewApproveVo.getCurrentUserId())){
                if("Y".equals(viewApproveVo.getIsOver())){
                    viewApproveVo.setApproveStatus("已审批");
                }
                if("N".equals(viewApproveVo.getIsOver())){
                    viewApproveVo.setApproveStatus("未审批");
                }
            }
            if ("N".equals(viewApprove1.getIsOver())) {
                viewApproveVo.setIsNow("Y");
            } else {
                viewApproveVo.setIsNow("N");
            }
            data.add(viewApproveVo);
        }
        Page<ViewApproveVo> pageVo = new PageImpl<ViewApproveVo>(data, pageable, pageData.getTotalElements());
        return pageVo;
    }

    @Override
    public Map<String, Object> saveApproveInfo(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        if (StringUtils.isNotBlank(approve.getId())) {
            Approve approve1 = approveRepository.findById(approve.getId()).orElse(null);
            approve1.setNotes(approve.getNotes());
            approve1.setIllustrate(approve.getIllustrate());
            approve1.setPersonId(person.getPersonId());
            approve1.setPersonName(person.getName());
            approveRepository.save(approve1);
            map.put("status", "true");
        } else {
            map.put("status", "false");
        }
        return map;
    }

    @Override
    public ApproveDTO getApproveById(String id) {
        ApproveDTO approveDTO = new ApproveDTO(approveRepository.findById(id).orElse(null));
        return approveDTO;
    }

    @Override
    public Approve findApproveById(String id) {
        return approveRepository.findById(id).orElse(null);
    }

    @Override
    public Map<String, Object> agreeApproveInfo(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();

        if (StringUtils.isNotBlank(approve.getId())) {
            Approve approve1 = approveRepository.findById(approve.getId()).orElse(null);
            if (approve1 == null) {
                map.put("status", "false");
                map.put("msg", "审批信息不存在");
                return map;
            }
            if("Y".equals(approve1.getIsOver())){
                map.put("status", "false");
                map.put("msg", "该条记录已经审批,不需要再次审批!");
                return map;
            }
            approve1.setApproveStatus(ApproveStatus.APPROVE.getName());
            approve1.setIsOver("Y");
            approve1.setPersonId(person.getPersonId());
            approve1.setPersonName(person.getName());
            approve1.setIllustrate(approve.getIllustrate());
            approve1.setNotes(approve.getNotes());
            if (ApplyType.STOP_INTERFACE.getEnName().equals(approve1.getApplyType()) || ApplyType.PUB_INTERFACE.getEnName().equals(approve1.getApplyType())) {
                InterfaceManage interfaceManage = interfaceManageRepository.findById(approve1.getInterfaceId()).orElse(null);
                interfaceManage.setInterfaceStatus(approve1.getInterfaceStatus());
                interfaceManageRepository.save(interfaceManage);
                if ("Y".equals(interfaceManage.getIsOverwrite())) {
                    if (StringUtils.isNotBlank(interfaceManage.getOverwriteInterfaceId())) {
                        InterfaceManage interfaceManage1 = interfaceManageRepository.findById(interfaceManage.getOverwriteInterfaceId()).orElse(null);
                        interfaceManage1.setIsDelete("Y");
                        interfaceManageRepository.save(interfaceManage1);
                    }
                }
                //初始化限流器
                if ("是".equals(interfaceManage.getIsLimit())) {
                    InterfaceLimitInfo interfaceLimitInfo = interfaceLimitInfoRepository.findAllByInterfaceId(interfaceManage.getId());
                    redissonUtil.init(interfaceLimitInfo, person.getPersonId());
                }
                List<String> applyTypes = new ArrayList<>();
                applyTypes.add(ApplyType.PUB_INTERFACE.getEnName());
                applyTypes.add(ApplyType.STOP_INTERFACE.getEnName());
                List<Approve> approveList = approveRepository.findByInterfaceIdAndIsNewAndApplyTypeIn(approve1.getInterfaceId(), "N", applyTypes);
                if(approveList!=null&&approveList.size()!=0){
                    for (Approve approve2 : approveList){
                        approve2.setIsNew("Y");
                    }
                }
                approveRepository.saveAll(approveList);
            }

            if (ApplyType.INVOKE.getEnName().equals(approve1.getApplyType())) {
                InterfaceApply apply = interfaceApplyRepository.findById(approve1.getApplyId()).orElse(null);
                createSecret(apply);
                interfaceApplyRepository.save(apply);
            }
            approveRepository.save(approve1);
            map.put("status", "true");
        }
        return map;
    }

    @Override
    public Map<String, Object> refuseApproveInfo(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        if (StringUtils.isNotBlank(approve.getId())) {
            Approve approve1 = approveRepository.findById(approve.getId()).orElse(null);
            if (approve1 == null) {
                map.put("status", "false");
                map.put("msg", "审批信息不存在");
                return map;
            }
            if("Y".equals(approve1.getIsOver())){
                map.put("status", "false");
                map.put("msg", "该条记录已经审批,不需要再次审批!");
                return map;
            }
            approve1.setApproveStatus(ApproveStatus.UN_APPROVE.getName());
            approve1.setIsOver("Y");
            approve1.setPersonId(userInfo.getPersonId());
            approve1.setPersonName(userInfo.getName());
            approve1.setIllustrate(approve.getIllustrate());
            approve1.setNotes(approve.getNotes());
            if (ApplyType.STOP_INTERFACE.getEnName().equals(approve1.getApplyType()) || ApplyType.PUB_INTERFACE.getEnName().equals(approve1.getApplyType())) {
                List<String> applyTypes = new ArrayList<>();
                applyTypes.add(ApplyType.PUB_INTERFACE.getEnName());
                applyTypes.add(ApplyType.STOP_INTERFACE.getEnName());
                List<Approve> approveList = approveRepository.findByInterfaceIdAndIsNewAndApplyTypeIn(approve1.getInterfaceId(), "N", applyTypes);
                if(approveList!=null&&approveList.size()!=0){
                    for (Approve approve2 : approveList){
                        approve2.setIsNew("Y");
                    }
                }
                approveRepository.saveAll(approveList);
            }
            approveRepository.save(approve1);
            map.put("status", "true");
        }
        map.put("status", "true");
        return map;
    }

    @Override
    public Map<String, Object> submitData(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        lock.lock();
        //0通过，其他为拒绝
        try {
            if ("0".equals(approve.getApproveStatus())) {
                map = agreeApproveInfo(approve);
            } else {
                map = refuseApproveInfo(approve);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return map;
    }

    @Override
    public List<Approve> getApproveByInterfaceId(String id, Boolean applyType) {
        List<Approve> list = null;
        List<String> applyTypes = new ArrayList<>();
        if (!applyType) {
            applyTypes.add(ApplyType.PUB_INTERFACE.getEnName());
            applyTypes.add(ApplyType.STOP_INTERFACE.getEnName());
            list = approveRepository.findByInterfaceIdAndIsNewAndApplyTypeIn(id, "N", applyTypes);
        } else {
            applyTypes.add(ApplyType.INVOKE.getEnName());
            list = approveRepository.findByInterfaceIdAndIsNewAndApplyTypeIn(id, "N", applyTypes);
        }
        return list;
    }

    @Override
    public Map<String, Object> buildApprove(Approve approve, String flowId) {
        return null;
    }

    @Override
    public Map<String, Object> pubInterface(InterfaceApplyDTO apply) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        if (StringUtils.isNotBlank(apply.getInterfaceId())) {
            String id = apply.getInterfaceId();
            InterfaceManage interfaceManage = interfaceManageRepository.findById(id).orElse(null);
            //只有注册状态的接口可以提交发布审批,或者是没有正在审核的记录可以提交发布审批
            List<Approve> approveList = new ArrayList<>();
            if (InterfaceStatus.SUBMIT_APPROVE.getName().equals(interfaceManage.getInterfaceStatus()) || InterfaceStatus.UN_APPROVE.getName().equals(interfaceManage.getInterfaceStatus())) {
                approveList = approveRepository.findByInterfaceIdAndIsOver(id, "N");
            }
            if ((InterfaceStatus.SUBMIT_APPROVE.getName().equals(interfaceManage.getInterfaceStatus()) && approveList.size() == 0)
                    || (InterfaceStatus.UN_APPROVE.getName().equals(interfaceManage.getInterfaceStatus()) && approveList.size() == 0)) {
                if (interfaceManage != null) {
                    apply.setApplyType(ApplyType.PUB_INTERFACE.getEnName());
                    interfaceManage.setInterfaceStatus(InterfaceStatus.SUBMIT_APPROVE.getName());
                    Map<String, Object> applyMap = createData(new InterfaceApply(apply),InterfaceStatus.APPROVE.getName());
                    if (applyMap.get("status") != null && (boolean) applyMap.get("status")) {
                        InterfaceManage interfaceManage1 = interfaceManageRepository.save(interfaceManage);
                        if (interfaceManage1 != null) {
                            map.put("status", true);
                        }
                    } else {
                        map.put("msg", applyMap.get("msg"));
                    }
                }
            } else {
                map.put("msg", "已经有正在审核的记录，请等待");
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> stopInterface(InterfaceApplyDTO apply) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(apply.getInterfaceId())) {
            InterfaceManage interfaceManage = interfaceManageRepository.findById(apply.getInterfaceId()).orElse(null);
            //只有发布状态的接口可以提交停用审批,且是没有正在审核的记录
            List<Approve> approveList = new ArrayList<>();
            if (InterfaceStatus.APPROVE.getName().equals(interfaceManage.getInterfaceStatus())) {
                approveList = approveRepository.findByInterfaceIdAndIsOver(apply.getInterfaceId(), "N");
            }
            if (InterfaceStatus.APPROVE.getName().equals(interfaceManage.getInterfaceStatus()) && approveList.size() == 0) {
                if (interfaceManage != null) {
                    apply.setApplyType(ApplyType.STOP_INTERFACE.getEnName());
                    Map<String, Object> applyMap = createData(new InterfaceApply(apply), InterfaceStatus.UN_APPROVE.getName());
                    if (applyMap.get("status") != null && (boolean) applyMap.get("status")) {
                        map.put("status", true);
                        return map;
                    } else {
                        map.put("msg", applyMap.get("msg"));
                    }
                }
            } else {
                map.put("msg", "停用失败，有正在审批的停用");
            }
        }
        map.put("status", false);
        return map;
    }

    @Override
    public Map<String, Object> useInterfaceApply(InterfaceApplyDTO apply, Boolean flag) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        if (StringUtils.isNotBlank(apply.getInterfaceId())) {
            String id = apply.getInterfaceId();
            InterfaceManage interfaceManage = interfaceManageRepository.findById(id).orElse(null);
            //只有发布状态的接口可以提交调用申请，并且是没有正在审批记录
            List<Approve> approveList = new ArrayList<>();
            if (InterfaceStatus.APPROVE.getName().equals(interfaceManage.getInterfaceStatus())) {
                if (StringUtils.isNotBlank(apply.getInterfaceId())) {
                    List<String> ids = new ArrayList<>();
                    if (StringUtils.isNotBlank(apply.getOldId())) {
                        ids.add(apply.getOldId());
                        approveList = approveRepository.findByApplyIdInAndApplyTypeAndIsOver(ids, ApplyType.INVOKE.getEnName(), "N");
                    }

                } else {
                    map.put("msg", "接口id不存在！");
                    return map;
                }
            } else {
                map.put("msg", "接口不存在！");
                return map;
            }
            if (approveList.size() == 0 || flag) {
                if (interfaceManage != null) {
                    apply.setApplyType(ApplyType.INVOKE.getEnName());
                    Map<String, Object> applyMap = createData(new InterfaceApply(apply), "");
                    if (StringUtils.isNotBlank(apply.getOldId())) {
                        InterfaceApply info = interfaceApplyRepository.findById(apply.getOldId()).orElse(null);
                        info.setIsEffective("N");
                        interfaceApplyRepository.save(info);
                    }
                    if (applyMap.get("status") != null && (boolean) applyMap.get("status")) {
                        map.put("status", true);
                    } else {
                        map.put("status", false);
                        map.put("msg", applyMap.get("msg"));
                    }
                }
            } else {
                map.put("msg", "已经有正在审核的记录，请等待");
            }
        }
        return map;
    }
    public void createSecret(InterfaceApply interfaceApply) {
        Date date = new Date();
        interfaceApply.setUserKey(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        interfaceApply.setUserSecret(Y9IdGenerator.genId(IdType.SNOWFLAKE));
    }

    public Map<String, Object> createData(InterfaceApply apply,String interfaceStatus) {
        Map<String,Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        apply.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        apply.setApplyPersonId(person.getPersonId());
        apply.setApplyPersonName(person.getName());
        if(StringUtils.isBlank(apply.getIsEffective())){
            apply.setIsEffective("Y");
        }
        if (StringUtils.isBlank(apply.getApplyType())){
            apply.setApplyType(ApplyType.INVOKE.getEnName());
        }
        InterfaceApply apply1 = interfaceApplyRepository.save(apply);

        Approve approve = new Approve();
        approve.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        approve.setApproveStatus(ApproveStatus.SUBMIT_APPROVE.getName());
        approve.setInterfaceId(apply1.getInterfaceId());
        approve.setApplyId(apply1.getId());
        approve.setIsOver("N");
        approve.setIsNew("N");
        approve.setApplyType(apply1.getApplyType());
        approve.setInterfaceStatus(interfaceStatus);


        //处理多个申请信息审批无法获取最新审批的问题
        if (ApplyType.INVOKE.getEnName().equals(apply1.getApplyType())){
            List<InterfaceApply> listApply = interfaceApplyRepository.findByInterfaceIdAndApplyPersonIdAndApplyType(apply1.getInterfaceId(),person.getPersonId(),ApplyType.INVOKE.getEnName());
            List<String> ids = new ArrayList<>();
            for(InterfaceApply apply2 : listApply){
                ids.add(apply2.getId());
            }
            List<Approve> list = approveRepository.findByApplyIdInAndApplyTypeAndIsNew(ids,ApplyType.INVOKE.getEnName(),"N");
            if (list!=null && list.size()!=0){
                for (Approve approve1: list){
                    approve1.setIsNew("Y");
                }
                approveRepository.saveAll(list);
            }
        }

        Approve approve1 = approveRepository.save(approve);
        if (approve1!=null){
            map.put("status",true);
        }
        return map;
    }
}

package net.risesoft.service.impl;

import net.risesoft.model.*;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.ApproveService;
import net.risesoft.service.InterfaceApplyService;
import net.risesoft.util.RedissonUtil;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.ApproveDTO;
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
    private InterfaceApplyService interfaceApplyService;

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
                InterfaceApply apply = interfaceApplyService.getApplyInfoById(approve1.getApplyId());
                interfaceApplyService.createSecret(apply);
                interfaceApplyService.saveInfo(apply);
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
}

package net.risesoft.service.impl;

import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.ApplyType;
import net.risesoft.model.ApproveStatus;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.InterfaceApplyService;
import net.risesoft.util.RedissonUtil;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.ApproveDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.repository.ApproveRepository;
import net.risesoft.y9public.repository.InterfaceApplyRepository;
import net.risesoft.y9public.repository.InterfaceLimitInfoRepository;
import net.risesoft.y9public.repository.InterfaceManageRepository;
import net.risesoft.y9public.vo.ApplyVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service(value = "InterfaceApplyService")
public class InterfaceApplyServiceImpl implements InterfaceApplyService {
    @Autowired
    private InterfaceManageRepository interfaceManageRepository;
    @Autowired
    private ApproveRepository approveRepository;
    @Autowired
    private InterfaceLimitInfoRepository interfaceLimitInfoRepository;
    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private InterfaceApplyRepository interfaceApplyRepository;

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
    public Map<String, Object> saveInfo(InterfaceApply apply) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        InterfaceApply interfaceApply = interfaceApplyRepository.save(apply);
        if (interfaceApply != null) {
            map.put("status", "true");
            map.put("interfaceId", interfaceApply.getInterfaceId());
        } else {
            map.put("status", "false");
        }
        return map;
    }

    //接口调用申请数据生成
    @Override
    public Map<String, Object> createData(InterfaceApply apply, String interfaceStatus) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        apply.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        apply.setApplyPersonId(person.getPersonId());
        apply.setApplyPersonName(person.getName());
        if (StringUtils.isBlank(apply.getIsEffective())) {
            apply.setIsEffective("Y");
        }
        if (StringUtils.isBlank(apply.getApplyType())) {
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
        if (ApplyType.INVOKE.getEnName().equals(apply1.getApplyType())) {
            List<InterfaceApply> listApply = interfaceApplyRepository.findByInterfaceIdAndApplyPersonIdAndApplyType(apply1.getInterfaceId(), person.getPersonId(), ApplyType.INVOKE.getEnName());
            List<String> ids = new ArrayList<>();
            for (InterfaceApply apply2 : listApply) {
                ids.add(apply2.getId());
            }
            List<Approve> list = approveRepository.findByApplyIdInAndApplyTypeAndIsNew(ids, ApplyType.INVOKE.getEnName(), "N");
            if (list != null && list.size() != 0) {
                for (Approve approve1 : list) {
                    approve1.setIsNew("Y");
                }
                approveRepository.saveAll(list);
            }
        }

        Approve approve1 = approveRepository.save(approve);
        if (approve1 != null) {
            map.put("status", true);
        }
        return map;
    }

    @Override
    public Map<String, Object> agreeApproveInfo(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        if (StringUtils.isNotBlank(approve.getId())) {
            Approve approve1 = approveRepository.findById(approve.getId()).orElse(null);
            if (ApproveStatus.SUBMIT_APPROVE.getName().equals(approve1.getApproveStatus())) {
                approve1.setIsOver("Y");
                approve1.setApproveStatus(ApproveStatus.APPROVE.getName());
                approve1.setPersonId(person.getPersonId());
                approve1.setPersonName(person.getName());
                approve1.setNotes(approve.getNotes());
                approve1.setIllustrate(approve.getIllustrate());
                InterfaceManage interfaceManage = interfaceManageRepository.findById(approve1.getInterfaceId()).orElse(null);
                interfaceManage.setInterfaceStatus(approve1.getInterfaceStatus());
                interfaceManageRepository.save(interfaceManage);
                approveRepository.save(approve1);
                //初始化限流器
                if ("是".equals(interfaceManage.getIsLimit())) {
                    InterfaceLimitInfo interfaceLimitInfo = interfaceLimitInfoRepository.findAllByInterfaceId(interfaceManage.getId());
                    redissonUtil.init(interfaceLimitInfo, person.getPersonId());
                }
                map.put("status", "true");
            } else {
                map.put("status", "false");
                map.put("msg", "审批状态不正确，审批已经结束");
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> refuseApproveInfo(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        if (StringUtils.isNotBlank(approve.getId())) {
            Approve approve1 = approveRepository.findById(approve.getId()).orElse(null);
            if (ApproveStatus.SUBMIT_APPROVE.getName().equals(approve1.getApproveStatus())) {
                approve1.setIsOver("Y");
                approve1.setApproveStatus(ApproveStatus.UN_APPROVE.getName());
                approve1.setPersonId(person.getPersonId());
                approve1.setPersonName(person.getName());
                approve1.setNotes(approve.getNotes());
                approve1.setIllustrate(approve.getIllustrate());
                approveRepository.save(approve1);
                map.put("status", "true");
            } else {
                map.put("status", "false");
                map.put("msg", "审批状态不正确，审批已经结束");
            }
        }
        return map;
    }

    @Override
    public InterfaceApply getApplyInfoById(String id) {
        return interfaceApplyRepository.findById(id).orElse(null);
    }

    @Override
    public List<String> getInterfaceIdsByPersonId() {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        List<InterfaceApply> list = interfaceApplyRepository.findByApplyPersonIdAndApplyType(person.getPersonId(), ApplyType.INVOKE.getEnName());
        Set<String> set = new HashSet<>();
        for (InterfaceApply apply : list) {
            set.add(apply.getInterfaceId());
        }
        return new ArrayList<>(set);
    }

    @Override
    public List<String> getApplyIdsByPersonIdAndInterfaceId(String interfaceId) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        List<InterfaceApply> list = interfaceApplyRepository.findByInterfaceIdAndApplyPersonIdAndApplyType(interfaceId, person.getPersonId(), ApplyType.INVOKE.getEnName());
        Set<String> set = new HashSet<>();
        for (InterfaceApply apply : list) {
            set.add(apply.getId());
        }
        return new ArrayList<>(set);
    }

    @Override
    public InterfaceApply getApplyInfoByPersonIdAndInterfaceId(String interfaceId) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        List<InterfaceApply> list = interfaceApplyRepository.findByInterfaceIdAndApplyPersonIdAndApplyTypeOrderByCreateTimeDesc(interfaceId, person.getPersonId(), ApplyType.INVOKE.getEnName());
        return list.get(0);
    }

    @Override
    public Page<ApplyVo> getApplyListById(String id, Integer page, Integer limit) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        Pageable pageable = PageRequest.of(page - 1, limit);
        return interfaceApplyRepository.findListPage(person.getPersonId(), id, pageable);
    }

    @Override
    public Boolean findIsPass(String id) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        Integer pass = interfaceApplyRepository.findListIsPass(person.getPersonId(), id);
        if (pass > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void createSecret(InterfaceApply interfaceApply) {
        Date date = new Date();
        interfaceApply.setUserKey(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        interfaceApply.setUserSecret(Y9IdGenerator.genId(IdType.SNOWFLAKE));
    }

    private Page<InterfaceApply> page(InterfaceApply dto, Integer page, Integer limit) {
        Specification<InterfaceApply> spec = new Specification<InterfaceApply>() {
            @Override
            public Predicate toPredicate(Root<InterfaceApply> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(dto.getApplyPersonId())) {
                    predicates.add(criteriaBuilder.equal(root.get("applyPersonId"), dto.getApplyPersonId()));
                }
                if (StringUtils.isNotBlank(dto.getInterfaceId())) {
                    predicates.add(criteriaBuilder.equal(root.get("interfaceId"), dto.getInterfaceId()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createTime").descending());
        return interfaceApplyRepository.findAll(spec, pageable);
    }
}

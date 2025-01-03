package net.risesoft.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.risesoft.enums.platform.ManagerLevelEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.*;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.InterfaceApplyService;
import net.risesoft.service.InterfaceManageService;
import net.risesoft.service.ParameterService;
import net.risesoft.util.BizException;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.InterfaceApplyDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.repository.*;
import net.risesoft.y9public.vo.AuthSelectVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service(value = "InterfaceManageService")
public class InterfaceManageServiceImpl implements InterfaceManageService {
    @Autowired
    private InterfaceManageRepository interfaceManageRepository;
    @Autowired
    private ApproveRepository approveRepository;
    @Autowired
    private ParameterService parameterService;
    @Autowired
    private InterfaceLimitInfoRepository interfaceLimitInfoRepository;
    @Autowired
    private InterfaceApplyService interfaceApplyService;
    @Autowired
    private AuthDictRepository authDictRepository;

    @Override
    public Page<InterfaceManageDTO> getInterfaceList(InterfaceManageDTO interfaceManageDTO) {
        Specification<InterfaceManage> spec = new Specification<InterfaceManage>() {
            @Override
            public Predicate toPredicate(Root<InterfaceManage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(interfaceManageDTO.getInterfaceName())) {
                    predicates.add(criteriaBuilder.like(root.get("interfaceName"), "%" + interfaceManageDTO.getInterfaceName() + "%"));
                }
                if (StringUtils.isNotBlank(interfaceManageDTO.getPersonId())) {
                    UserInfo person = Y9LoginUserHolder.getUserInfo();

                    if (person.getManagerLevel().getValue() != ManagerLevelEnum.SYSTEM_MANAGER.getValue()) {
                        predicates.add(criteriaBuilder.equal(root.get("personId"), interfaceManageDTO.getPersonId()));
                    }
                }
                predicates.add(criteriaBuilder.equal(root.get("isDelete"), "N"));
                predicates.add(criteriaBuilder.equal(root.get("isTest"), "N"));
                //可申请接口列表,先查下申请表将当前登陆人已经申请接口id查询出来，在筛掉
                if (StringUtils.isNotBlank(interfaceManageDTO.getMayApply())) {
                    List<String> ids = interfaceApplyService.getInterfaceIdsByPersonId();
                    if (ids != null && ids.size() != 0) {
                        CriteriaBuilder.In<Object> interfaceIds = criteriaBuilder.in(root.get("id"));
                        for (String it : ids) {
                            interfaceIds.value(it);
                        }
                        if ("发布".equals(interfaceManageDTO.getMayApply())) {
//                            predicates.add(criteriaBuilder.not(interfaceIds));
                        } else {
                            predicates.add(interfaceIds);
                        }
                    } else {
                        if (!"发布".equals(interfaceManageDTO.getMayApply())) {
                            CriteriaBuilder.In<Object> interfaceIds = criteriaBuilder.in(root.get("id"));
                            interfaceIds.value("-1");
                            predicates.add(interfaceIds);
                        }
                    }
                    predicates.add(criteriaBuilder.equal(root.get("interfaceStatus"), InterfaceStatus.APPROVE.getName()));
                }
                if (StringUtils.isNotBlank(interfaceManageDTO.getInterfaceType())) {
                    predicates.add(criteriaBuilder.equal(root.get("interfaceType"), interfaceManageDTO.getInterfaceType()));
                }
                if (StringUtils.isNotBlank(interfaceManageDTO.getInterfaceStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("interfaceStatus"), interfaceManageDTO.getInterfaceStatus()));
                }
                if (StringUtils.isNotBlank(interfaceManageDTO.getInterfaceMethod())) {
                    predicates.add(criteriaBuilder.equal(root.get("interfaceMethod"), interfaceManageDTO.getInterfaceMethod()));
                }
                if (StringUtils.isNotBlank(interfaceManageDTO.getIsLimit())) {
                    predicates.add(criteriaBuilder.equal(root.get("isLimit"), interfaceManageDTO.getIsLimit()));
                }
                if (StringUtils.isNotBlank(interfaceManageDTO.getIsAuth())) {
                    predicates.add(criteriaBuilder.equal(root.get("isAuth"), interfaceManageDTO.getIsAuth()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(interfaceManageDTO.getPage() - 1, interfaceManageDTO.getLimit(), Sort.by("createTime").descending());
        Page<InterfaceManage> page = interfaceManageRepository.findAll(spec, pageable);
        List<String> interfaceIds = new ArrayList<>();
        List<InterfaceManageDTO> list = new ArrayList<>();
        Map<String, InterfaceManageDTO> map = new HashMap<>();
        for (InterfaceManage interfaceManage : page.getContent()) {
            interfaceIds.add(interfaceManage.getId());
            InterfaceManageDTO interfaceManageDTO1 = new InterfaceManageDTO(interfaceManage);
            list.add(interfaceManageDTO1);
            map.put(interfaceManageDTO1.getId(), interfaceManageDTO1);
        }
        List<Approve> approves = null;
        if (interfaceIds.size() != 0) {
            List<String> applyTypes = new ArrayList<>();
            if ((StringUtils.isNotBlank(interfaceManageDTO.getMayApply()) && "申请".equals(interfaceManageDTO.getMayApply()))) {
                applyTypes.add(ApplyType.INVOKE.getEnName());
            } else {
                applyTypes.add(ApplyType.PUB_INTERFACE.getEnName());
                applyTypes.add(ApplyType.STOP_INTERFACE.getEnName());
            }
            applyTypes.add(ApplyType.INVOKE.getEnName());
            approves = approveRepository.findByInterfaceIdInAndIsNewAndApplyTypeIn(interfaceIds, "N", applyTypes);
        }
        if (approves != null && approves.size() != 0) {
            for (Approve approve : approves) {
                if (map.get(approve.getInterfaceId()) != null) {
                    map.get(approve.getInterfaceId()).setApproveStatus(approve.getApproveStatus());
                }
            }
        }
        for (InterfaceManageDTO interfaceManageDTO1 : list) {
            if (StringUtils.isBlank(interfaceManageDTO1.getApproveStatus())) {
                interfaceManageDTO1.setApproveStatus(ApproveStatus.UN_SUBMIT_APPROVE.getName());
            }
        }
        Page<InterfaceManageDTO> pageDto = new PageImpl<InterfaceManageDTO>(list, pageable, page.getTotalElements());
        return pageDto;
    }

    @Override
    public Map<String, Object> saveInterfaceInfo(InterfaceManage interfaceManage) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        InterfaceManage interfaceManage2 = interfaceManageRepository.findById(interfaceManage.getId()).orElse(null);
        if (interfaceManage2 == null || StringUtils.isBlank(interfaceManage2.getId())) {
            if (StringUtils.isBlank(interfaceManage.getId())) {
                interfaceManage.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            }
            if (StringUtils.isBlank(interfaceManage.getSameInterfaceId())) {
                interfaceManage.setSameInterfaceId(interfaceManage.getId());
            }
            if (StringUtils.isBlank(interfaceManage.getIsOverwrite())) {
                interfaceManage.setIsOverwrite("N");
            }
            interfaceManage.setIsDelete("N");
            if (StringUtils.isBlank(interfaceManage.getIsTest())) {
                interfaceManage.setIsTest("N");
            }
            interfaceManage.setInterfaceStatus(InterfaceStatus.SUBMIT_APPROVE.getName());
            interfaceManage.setPersonId(person.getPersonId());
            interfaceManage.setPersonName(person.getName());
        }

        InterfaceManage interfaceManage1 = interfaceManageRepository.save(interfaceManage);
        if (interfaceManage1 == null) {
            map.put("status", false);
        } else {
            map.put("id", interfaceManage1.getId());
            map.put("status", true);
        }
        return map;
    }

    @Override
    public Map<String, String> delInterfaceInfo(String id) {
        Map<String, String> map = new HashMap<>();
        try {
            if (StringUtils.isNotBlank(id)) {
                InterfaceManage interfaceManage = interfaceManageRepository.findById(id).orElse(null);
                if (interfaceManage != null) {
                    if (InterfaceStatus.SUBMIT_APPROVE.getName().equals(interfaceManage.getInterfaceStatus())) {
                        List<Approve> approveList = approveRepository.findByInterfaceIdAndIsOver(id, "N");
                        if (approveList.size() != 0) {
                            map.put("status", "false");
                            map.put("msg", "删除失败，已经提交审批流程");
                            return map;
                        }
                    } else if (InterfaceStatus.APPROVE.getName().equals(interfaceManage.getInterfaceStatus())) {
                        map.put("status", "false");
                        map.put("msg", "删除失败，接口已经发布");
                        return map;
                    }
                    interfaceManage.setIsDelete("Y");
                    InterfaceManage interfaceManage1 = interfaceManageRepository.save(interfaceManage);
                    authDictRepository.deleteInfoByInterfaceIdLogic(interfaceManage1.getId());
                    if ("Y".equals(interfaceManage1.getIsDelete())) {
                        map.put("status", "true");
                    }
                }
            } else {
                map.put("status", "false");
                map.put("msg", "id为空");
            }
        } catch (Exception e) {
            map.put("status", "false");
            map.put("msg", "删除失败，删除出错");
        }
        return map;
    }

    @Override
    public Map<String, String> delAuthInfoById(String id) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(id)) {
            InterfaceManage interfaceManage = interfaceManageRepository.findById(id).orElse(null);
            //如果接口存在不清理废数据
            if (interfaceManage != null) {

            } else {
                authDictRepository.deleteInfoByInterfaceId(id);
                map.put("status", "true");
            }
        } else {
            map.put("status", "false");
            map.put("msg", "没有传入id");
        }
        return map;
    }

    @Override
    public InterfaceManageDTO getInterfaceInfoById(String id) {
        InterfaceManage interfaceManage = interfaceManageRepository.findById(id).orElse(null);
        InterfaceManageDTO interfaceManageDTO = new InterfaceManageDTO(interfaceManage);
        if (StringUtils.isNotBlank(interfaceManageDTO.getId())) {
            Map<String, String> map = parameterService.getParameterData(interfaceManageDTO.getId(), interfaceManageDTO.getInterfaceMethod(), interfaceManageDTO.getInterfaceType());
            interfaceManageDTO.setParameters(map.get("parameters"));
            interfaceManageDTO.setReqParameters(map.get("reqParameters"));
            interfaceManageDTO.setResParameters(map.get("resParameters"));
            InterfaceLimitInfo interfaceLimitInfo = interfaceLimitInfoRepository.findAllByInterfaceId(interfaceManageDTO.getId());
            interfaceManageDTO.setLimitInfo(JSONArray.toJSONString(interfaceLimitInfo));
        }
        return interfaceManageDTO;
    }

    @Override
    public Map<String, Object> pubInterface(InterfaceApplyDTO apply) {
        Y9Context.getProperty("y9.interfaceManage.manageRole");
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
                    Map<String, Object> applyMap = interfaceApplyService.createData(new InterfaceApply(apply),InterfaceStatus.APPROVE.getName());
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
                    Map<String, Object> applyMap = interfaceApplyService.createData(new InterfaceApply(apply), InterfaceStatus.UN_APPROVE.getName());
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

    @Transactional(rollbackOn = RuntimeException.class)
    @Override
    public Map<String, Object> saveInterfaceInfo(InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            InterfaceManage interfaceManage = new InterfaceManage(interfaceManageDTO);
            Map<String, Object> map = saveInterfaceInfo(interfaceManage);
            UserInfo person = Y9LoginUserHolder.getUserInfo();
            if ("true".equals(map.get("status").toString())) {
                String interfaceId = map.get("id").toString();
                if (StringUtils.isNotBlank(interfaceId)) {
                    if (StringUtils.isNotBlank(interfaceManageDTO.getId())) {
                        try {
                            interfaceLimitInfoRepository.deleteByInterfaceId(interfaceId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (StringUtils.isNotBlank(interfaceManageDTO.getLimitInfo())) {
                        InterfaceLimitInfo interfaceLimitInfo = JSONArray.parseObject(interfaceManageDTO.getLimitInfo(), InterfaceLimitInfo.class);
                        interfaceLimitInfo.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                        interfaceLimitInfo.setInterfaceId(interfaceId);
                        interfaceLimitInfo.setPersonId(person.getPersonId());
                        interfaceLimitInfo.setPersonName(person.getName());
                        interfaceLimitInfoRepository.save(interfaceLimitInfo);
                    }
                    interfaceManageDTO.setId(interfaceId);
                    Map<String, Object> saveMap = parameterService.saveParameterData(interfaceManageDTO);
                    if (!(boolean) saveMap.get("status")) {
                        dataMap.put("status", "error");
                        dataMap.put("msg", "新增失败！" + saveMap.get("msg"));
                    }
                    dataMap.put("status", "success");
                } else {
                    dataMap.put("status", false);
                    dataMap.put("msg", "新增失败！");
                }
            } else {
                dataMap.put("status", false);
                dataMap.put("msg", "新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    @Override
    public Map<String, Object> saveInterfaceFile(MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> dataMap = upload(file);
        if (StringUtils.isNotBlank(dataMap.get("path"))) {
            map.putAll(dataMap);
            map.put("status", true);
            return map;
        } else {
            map.put("status", false);
        }
        return map;
    }

    @Override
    public Map<String, Object> updateVersionInfo(InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> dataMap = new HashMap<>();
        InterfaceManage interfaceManage1 = interfaceManageRepository.findById(interfaceManageDTO.getSameInterfaceId()).orElse(null);
        List<InterfaceManage> oldList = interfaceManageRepository.findBySameInterfaceIdAndIsDelete(interfaceManage1.getSameInterfaceId(), "N");
        int compare = 0;
        String maxVersion = interfaceManageDTO.getVersion();
        Boolean isMax = true;
        if (StringUtils.isNotBlank(interfaceManage1.getVersion()) && StringUtils.isNotBlank(interfaceManageDTO.getVersion())) {
            for (InterfaceManage interfaceManage : oldList) {
                if (StringUtils.isNotBlank(interfaceManage.getVersion())) {
                    compare = compareVersion(interfaceManage.getVersion().substring(1), maxVersion.substring(1));
                }
                if (compare != -1) {
                    isMax = false;
                    maxVersion = interfaceManage.getVersion();
                }
            }
        } else {
            dataMap.put("status", "success");
            dataMap.put("msg", "接口版本维护失败，版本为空");
        }
        if (!isMax) {
            dataMap.put("status", "error");
            dataMap.put("msg", "接口版本维护失败，当前输入版本过低，请输入更高的版本号，当前最高版本为:" + maxVersion);
            return dataMap;
        }
        interfaceManageDTO.setSameInterfaceId(interfaceManage1.getSameInterfaceId());
        if ("Y".equals(interfaceManageDTO.getIsOverwrite())) {
            interfaceManageDTO.setOverwriteInterfaceId(interfaceManage1.getId());
        }
        Map<String, Object> map = saveInterfaceInfo(interfaceManageDTO);
        if ("success".equals(map.get("status").toString())) {
            dataMap.put("status", "success");
        } else {
            dataMap.put("status", false);
            dataMap.put("msg", "新增失败");
        }
        return dataMap;
    }

    @Override
    public List<Map<String, Object>> getAuthListByInterfaceId(String id, Boolean isView) {
        List<Map<String, Object>> data = new ArrayList<>();
        InterfaceManage interfaceManage = interfaceManageRepository.findById(id).orElse(null);
        List<AuthDict> authDicts = null;
        if (interfaceManage != null) {
            if (StringUtils.isNotBlank(interfaceManage.getParameterIds())) {
                List<String> ids = Arrays.stream(interfaceManage.getParameterIds().split(",")).collect(Collectors.toList());
                authDicts = authDictRepository.findByParameterIdIn(ids);
                Map<String, List<AuthSelectVo>> mapData = new HashMap<>();
                for (AuthDict authDict1 : authDicts) {
                    if (StringUtils.isNotBlank(authDict1.getIsPrimary()) && "Y".equals(authDict1.getIsPrimary())) {
                        Map<String, Object> map = new HashMap<>();
                        if (StringUtils.isNotBlank(authDict1.getIsTree()) && "是".equals(authDict1.getIsTree())) {
                            map.put("type", "slot");
                        } else {
                            map.put("type", "select");
                        }
                        map.put("label", authDict1.getParameterName());
                        map.put("prop", authDict1.getFieldName());
                        map.put("parameterId", authDict1.getParameterId());
                        map.put("id", authDict1.getId());
                        data.add(map);
                    } else if ("N".equals(authDict1.getIsPrimary())) {
                        if (mapData.get(authDict1.getParameterId()) != null) {
                            AuthSelectVo selectVo = new AuthSelectVo(authDict1);
                            selectVo.setDisabled(isView);
                            mapData.get(authDict1.getParameterId()).add(selectVo);
                        } else {
                            List<AuthSelectVo> list = new ArrayList<>();
                            mapData.put(authDict1.getParameterId(), list);
                            AuthSelectVo selectVo = new AuthSelectVo(authDict1);
                            selectVo.setDisabled(isView);
                            mapData.get(authDict1.getParameterId()).add(selectVo);
                        }
                    }
                }
                for (Map<String, Object> map : data) {
                    if (StringUtils.isNotBlank(map.get("type").toString())) {
                        if ("slot".equals(map.get("type").toString())) {
                            map.put("options", integraTreeData(mapData.get(map.get("parameterId").toString())));
                        } else {
                            map.put("options", mapData.get(map.get("parameterId").toString()));
                        }
                    }
                }
            }
        }
        return data;
    }

    @Override
    public List<Parameter> getRequiredParameterData(String id) {
        return parameterService.getRequiredParameterData(id);
    }

    @Override
    public Map<String, Object> useInterfaceApply(InterfaceApplyDTO apply, Boolean flag) {
        Y9Context.getProperty("y9.interfaceManage.manageRole");
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
                    Map<String, Object> applyMap = interfaceApplyService.createData(new InterfaceApply(apply), "");
                    if (StringUtils.isNotBlank(apply.getOldId())) {
                        InterfaceApply info = interfaceApplyService.getApplyInfoById(apply.getOldId());
                        info.setIsEffective("N");
                        interfaceApplyService.saveInfo(info);
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

    @Override
    public InterfaceApply getApplyInfoByInterfaceId(String id) {
        return interfaceApplyService.getApplyInfoByPersonIdAndInterfaceId(id);
    }

    @Override
    public void exportFile(List<String> ids, HttpServletResponse response) {
        List<InterfaceManage> manages = interfaceManageRepository.findByIdIn(ids);

        List<InterfaceLimitInfo> limitInfos = interfaceLimitInfoRepository.findByIds(ids);
        Map<String, InterfaceLimitInfo> limitMap = new HashMap<>();
        for (InterfaceLimitInfo info : limitInfos) {
            limitMap.put(info.getInterfaceId(), info);
        }

        List<Parameter> parameters = parameterService.getParameterDataByInterfaceIds(ids);
        Map<String, List<Parameter>> parameterMap = new HashMap<>();
        for (Parameter info : parameters) {
            if (parameterMap.get(info.getInterfaceId()) != null) {
                parameterMap.get(info.getInterfaceId()).add(info);
            } else {
                List<Parameter> list = new ArrayList<>();
                list.add(info);
                parameterMap.put(info.getInterfaceId(), list);
            }
        }

        List<InterfaceManageDTO> dtos = new ArrayList<>();
        for (InterfaceManage entity : manages) {
            InterfaceManageDTO dto = new InterfaceManageDTO(entity);
            if (limitMap.get(dto.getId()) != null) {
                dto.setLimitInfoEntity(limitMap.get(dto.getId()));
            }
            if (parameterMap.get(dto.getId()) != null) {
                dto.setParameterList(parameterMap.get(dto.getId()));
            }
            dtos.add(dto);
        }
        List<File> files = new ArrayList<>();
        try {
            // 将json文件放入到压缩包当中
            // key 文件名称, value 文件地址
            HashMap<String, String> maps = new HashMap<>();

            for (InterfaceManageDTO dto : dtos) {
                String fileName = dto.getId() + "_" + dto.getInterfaceName() + "_" + dto.getVersion();
                File tempFile = File.createTempFile(fileName, ".json");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                objectMapper.writeValue(tempFile, dto);
                maps.put(fileName + ".json", tempFile.getAbsolutePath());
                files.add(tempFile);
            }

            FileInputStream fileInputStream = null;
            byte[] b = new byte[1024];
            int len;
            File zipfile = File.createTempFile("接口导出信息", ".zip");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                File newFile = new File(entry.getValue());
                fileInputStream = new FileInputStream(newFile);
                out.putNextEntry(new ZipEntry(entry.getKey()));

                while ((len = fileInputStream.read(b)) > 0) {
                    out.write(b, 0, len);
                }
                out.closeEntry();
                fileInputStream.close();
            }
            out.close();
            files.add(zipfile);
            response.addHeader("Content-Disposition", "attachment;filename=" + "接口导出信息" + ".zip");
            response.setContentType("application/octet-stream");
            try {
                InputStream fis = new BufferedInputStream(new FileInputStream(zipfile.getAbsolutePath()));
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                OutputStream resOut = new BufferedOutputStream(response.getOutputStream());
                resOut.write(buffer);
                resOut.flush();
                resOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, Boolean isOverWrite) {
        HashMap<String, Object> map = new HashMap<>();
        String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        Date nowDate = new Date();
        List<String> requiredFileds = Arrays.asList(
                "interfaceName",
                "interfaceUrl",
                "isAuth",
                "isLimit",
                "isLimitData",
                "interfaceType",
                "networkAgreement",
                "interfaceMethod",
                "version",
                "deptInfo",
                "deptId",
                "systemName",
                "systemId",
                "head",
                "headPhone"
        );
        List<String> parameterRequiredFileds = Arrays.asList(
                "interfaceId",
                "parameterKey",
                "parameterType",
                "required"
        );
        List<String> postParameterRequiredFileds = Arrays.asList(
                "pid",
                "level",
                "isItems"
        );
        try {
            if (".zip".equals(fileSuffix)) {
                List<InterfaceManageDTO> entity = new ArrayList<>();
                // 转为压缩文件流
                ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream(), Charset.forName("UTF-8"));
                ZipEntry zipEntry = null;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".json")) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int length = -1;
                        while ((length = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                        outputStream.close();
                        entity.add(readJson(new ByteArrayInputStream(outputStream.toByteArray())));
                        zipInputStream.closeEntry();
                    }
                }
                zipInputStream.close();
                List<InterfaceManage> entityList = new ArrayList<>();
                List<InterfaceLimitInfo> limitEntityList = new ArrayList<>();
                List<Parameter> parameters = new ArrayList<>();
                Map<String, String> idChange = new HashMap<>();
                for (InterfaceManageDTO dto : entity) {
                    UserInfo person = Y9LoginUserHolder.getUserInfo();
                    dto.setIsDelete("N");
                    if (StringUtils.isBlank(dto.getIsTest())) {
                        dto.setIsTest("N");
                    }
                    dto.setInterfaceStatus(InterfaceStatus.SUBMIT_APPROVE.getName());
                    dto.setPersonId(person.getPersonId());
                    dto.setPersonName(person.getName());
                    //isOverWrite是否生成新的接口id，true生成
                    if (isOverWrite) {
                        dto.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                        if (dto.getParameterList() != null) {
                            for (Parameter en : dto.getParameterList()) {
                                en.setInterfaceId(dto.getId());
                                idChange.put(en.getId(), Y9IdGenerator.genId(IdType.SNOWFLAKE));
                                en.setId(idChange.get(en.getId()));
                                en.setPersonId(person.getPersonId());
                                if ("post".equals(dto.getInterfaceMethod().toLowerCase())) {
                                    if (checkObjFieldsIsNull(en, postParameterRequiredFileds)) {
                                        throw new BizException("接口参数列表必填信息校验失败！");
                                    }
                                }
                            }
                        }
                        if (dto.getLimitInfoEntity() != null) {
                            dto.getLimitInfoEntity().setInterfaceId(dto.getId());
                            dto.getLimitInfoEntity().setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                            dto.getLimitInfoEntity().setPersonId(person.getPersonId());
                            dto.getLimitInfoEntity().setPersonName(person.getName());
                        }
                    }
                    dto.setInterfaceStatus(InterfaceStatus.SUBMIT_APPROVE.getName());
                    entityList.add(new InterfaceManage(dto));
                    if (dto.getLimitInfoEntity() != null) {
                        limitEntityList.add(dto.getLimitInfoEntity());
                    }
                    if (dto.getParameterList() != null) {
                        parameters.addAll(dto.getParameterList());
                    }
                    if ("true".equals(dto.getIsLimit())) {
                        if (!checkInterfaceLimitInfo(dto.getLimitInfoEntity())) {
                            throw new BizException("接口限流必填信息校验未通过请检查！");
                        }
                    }
                }
                if (isOverWrite) {
                    for (Parameter parameter : parameters) {
                        if (StringUtils.isNotBlank(parameter.getPid())) {
                            parameter.setPid(idChange.get(parameter.getPid()));
                        }
                    }
                }
                //必填信息校验
                for (InterfaceManage manage : entityList) {
                    manage.setSameInterfaceId(manage.getId());
                    if (checkObjFieldsIsNull(manage, requiredFileds)) {
                        throw new BizException("接口必填信息校验未通过请检查！");
                    }
                }
                for (Parameter parameter : parameters) {
                    if (checkObjFieldsIsNull(parameter, parameterRequiredFileds)) {
                        throw new BizException("接口参数列表必填信息校验未通过请检查！");
                    }
                }
                interfaceManageRepository.saveAll(entityList);
                interfaceLimitInfoRepository.saveAll(limitEntityList);
                parameterService.saveAllParameterData(parameters);
                map.put("status", true);
            }
            if (".json".equals(fileSuffix)) {
                Map<String, String> idChange = new HashMap<>();

                InterfaceManageDTO dto = readJson(file.getInputStream());
                List<Parameter> parameters = new ArrayList<>();

                UserInfo person = Y9LoginUserHolder.getUserInfo();
                dto.setIsDelete("N");
                if (StringUtils.isBlank(dto.getIsTest())) {
                    dto.setIsTest("N");
                }
                dto.setInterfaceStatus(InterfaceStatus.SUBMIT_APPROVE.getName());
                dto.setPersonId(person.getPersonId());
                dto.setPersonName(person.getName());
                //isOverWrite是否生成新的接口id，true生成
                if (isOverWrite) {
                    dto.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                    if (dto.getParameterList() != null) {
                        for (Parameter en : dto.getParameterList()) {
                            en.setInterfaceId(dto.getId());
                            idChange.put(en.getId(), Y9IdGenerator.genId(IdType.SNOWFLAKE));
                            en.setId(idChange.get(en.getId()));
                            en.setPersonId(person.getPersonId());
                            if ("post".equals(dto.getInterfaceMethod().toLowerCase())) {
                                if (checkObjFieldsIsNull(en, postParameterRequiredFileds)) {
                                    throw new BizException("接口参数列表必填信息校验失败！");
                                }
                            }
                        }
                    }
                    if (dto.getLimitInfoEntity() != null) {
                        dto.getLimitInfoEntity().setPersonId(person.getPersonId());
                        dto.getLimitInfoEntity().setPersonName(person.getName());
                        dto.getLimitInfoEntity().setInterfaceId(dto.getId());
                        dto.getLimitInfoEntity().setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                    }
                }
                dto.setInterfaceStatus(InterfaceStatus.SUBMIT_APPROVE.getName());
                if (dto.getParameterList() != null) {
                    parameters.addAll(dto.getParameterList());
                }

                //置换pid为新的id
                if (isOverWrite) {
                    for (Parameter parameter : parameters) {
                        if (StringUtils.isNotBlank(parameter.getPid())) {
                            parameter.setPid(idChange.get(parameter.getPid()));
                        }
                    }
                }
                //必填信息校验
                if (checkObjFieldsIsNull(new InterfaceManage(dto), requiredFileds)) {
                    throw new BizException("接口必填信息校验失败！");
                }

                for (Parameter parameter : parameters) {
                    if (checkObjFieldsIsNull(parameter, parameterRequiredFileds)) {
                        throw new BizException("接口参数列表必填信息校验未通过请检查！");
                    }
                }
                if ("true".equals(dto.getIsLimit())) {
                    if (!checkInterfaceLimitInfo(dto.getLimitInfoEntity())) {
                        throw new BizException("接口限流必填信息校验未通过请检查！");
                    }
                }
                dto.setSameInterfaceId(dto.getId());
                interfaceManageRepository.save(new InterfaceManage(dto));
                interfaceLimitInfoRepository.save(dto.getLimitInfoEntity());
                parameterService.saveAllParameterData(parameters);
                map.put("status", true);
            }
        } catch (BizException e) {
            map.put("status", false);
            map.put("msg", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", false);
            map.put("msg", "文件解析失败，请检查文件格式和内容数据格式是否正确！");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return map;
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String interfaceId) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(interfaceId)) {
            map.put("status", false);
            map.put("msg", "为传入接口id");
            return map;
        }
        InterfaceManage manage = interfaceManageRepository.findById(interfaceId).orElse(null);
        if (manage == null) {
            map.put("status", false);
            map.put("msg", "不存在该接口信息");
            return map;
        }
        String path = Y9Context.getProperty("y9.interfacePath") + File.separator + manage.getSameInterfaceId() + File.separator + manage.getVersion();
        File folder = new File(path);
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            if (!result) {
                map.put("status", false);
                map.put("msg", "文件夹创建失败");
                return map;
            }
        }
        String fileId = Y9IdGenerator.genId(IdType.SNOWFLAKE);
        File interfacefile = new File(path + File.separator + fileId + "_" + file.getOriginalFilename());
        try {
            file.transferTo(interfacefile);
        } catch (IOException e) {
            map.put("status", false);
            map.put("msg", "文件写入失败");
            e.printStackTrace();
            return map;
        }
        manage.setInterfaceFileUrl("api/rest/interface/downLoadInterfaceFile?sameId=" + manage.getSameInterfaceId()
                + "&version=" + manage.getVersion() + "&fileName=" + fileId + "_" + file.getOriginalFilename());
        manage.setInterfaceFileName(file.getOriginalFilename());
        interfaceManageRepository.save(manage);
        map.put("status", true);
        return map;
    }

    @Override
    public void downLoadInterfaceFile(String sameId, String version, String fileName, HttpServletResponse response) {
        String path = Y9Context.getProperty("y9.interfacePath") + File.separator + sameId + File.separator + version + File.separator + fileName;
        try {
            URL u = new URL("file://"+"file.pdf");
            response.setContentType(u.openConnection().getContentType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.addHeader("Content-Disposition", "inline;");
        response.setContentType("application/octet-stream");
        try {
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            OutputStream resOut = new BufferedOutputStream(response.getOutputStream());
            resOut.write(buffer);
            resOut.flush();
            resOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InterfaceManageDTO readJson(InputStream inputStream) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        InterfaceManageDTO jsonStr = objectMapper.readValue(inputStream, InterfaceManageDTO.class);
        return jsonStr;
    }

    //外层组装树形数据
    private List<AuthSelectVo> integraTreeData(List<AuthSelectVo> lists) {
        List<AuthSelectVo> list = new ArrayList<>();
        if (lists == null || lists.size() == 0) {
            return list;
        }
        Map<String, List<AuthSelectVo>> pidMaps = new HashMap<>();
        for (AuthSelectVo it : lists) {
            if ("0".equals(it.getPid())) {
                list.add(it);
            } else {
                if (pidMaps.get(it.getPid()) != null) {
                    pidMaps.get(it.getPid()).add(it);
                } else {
                    List<AuthSelectVo> parameters = new ArrayList<>();
                    parameters.add(it);
                    pidMaps.put(it.getPid(), parameters);
                }
            }
        }
        for (AuthSelectVo dto : list) {
            assembleData(dto, pidMaps);
        }
        return list;
    }

    //递归组装树形数据pidMaps根据父级id组装的子级list
    private void assembleData(AuthSelectVo node, Map<String, List<AuthSelectVo>> pidMaps) {
        if (pidMaps.get(node.getId()) != null) {
            List<AuthSelectVo> list = new ArrayList<>();
            if (pidMaps.get(node.getId()).size() != 0) {
                for (AuthSelectVo it : pidMaps.get(node.getId())) {
                    assembleData(it, pidMaps);
                    list.add(it);
                }
            }
            node.setChildren(list);
        }
    }

    public Map<String, String> upload(MultipartFile file) {
        Map<String, String> map = new HashMap<>();
        String path = "";
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String datePath = sdf.format(date);
            String ftp_risesoft_url = Y9Context.getProperty("y9.common.ftpURL");
            String ftp_risesoft_user = Y9Context.getProperty("y9.common.ftpUser");
            String ftp_risesoft_pwd = Y9Context.getProperty("y9.common.ftpPassword");
            FTPClient ftp_risesoft = new FTPClient();
            ftp_risesoft.connect(ftp_risesoft_url, 9021);
            ftp_risesoft.login(ftp_risesoft_user, ftp_risesoft_pwd);
            int reply_risesoft = ftp_risesoft.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply_risesoft)) {
                ftp_risesoft.disconnect();
                System.out.println("FTP连接失败");
            }
            ftp_risesoft.setFileType(FTP.BINARY_FILE_TYPE);
            boolean isCreateZipRoot = true;
            path += "fileStorage/" + datePath + "/";
            isCreateZipRoot = ftp_risesoft.makeDirectory(path);
            String fileName = Y9IdGenerator.genId(IdType.SNOWFLAKE) + "_" + file.getName();
            path += fileName;
            if (isCreateZipRoot) {
                boolean isUpload = ftp_risesoft.storeFile(path, file.getInputStream());
                if (isUpload) {
                    map.put("path", path);
                    map.put("fileName", fileName);
                } else {
                    System.out.println("存储risesoft：" + file.getName() + "失败！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    //第一个值大于第二个值返回1，第一个值等于第二个值返回0，第一个值小于第二个值返回-1
    private int compareVersion(String str1, String str2) {
        String[] str1s = str1.split("\\.");
        String[] str2s = str2.split("\\.");
        int len1 = str1s.length;
        int len2 = str2s.length;
        int rt = 0;
        for (int i = 0; i < len1; i++) {
            Integer ft = Integer.valueOf(str1s[i]);
            if (i < len2) {
                Integer st = Integer.valueOf(str2s[i]);
                if (ft > st) {
                    return 1;
                }
                if (ft < st) {
                    return -1;
                }
            } else {
                Integer mt = Integer.valueOf(str1s[i]);
                if (mt > 0) {
                    return 1;
                } else if (mt == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }
        if (len2 > len1) {
            Integer mt = Integer.valueOf(str2s[len1]);
            if (mt > 0) {
                return -1;
            } else if (mt == 0) {
                return 0;
            } else {
                return 1;
            }
        }
        return rt;
    }

    //对导入数据进行校验
    public static boolean checkObjFieldsIsNull(Object object, List<String> includeFieldNames) {
        if (null == object) {
            return false;
        }

        try {
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (includeFieldNames.contains(f.getName()) && (f.get(object) == null || StringUtils.isBlank(f.get(object).toString()))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    //对限流信息必填信息校验
    private boolean checkInterfaceLimitInfo(InterfaceLimitInfo entity) {
        if (StringUtils.isBlank(entity.getInterfaceId())) {
            return false;
        }
        if (StringUtils.isBlank(entity.getThresholdType())) {
            return false;
        } else {
            if (ThresholdType.TYPE_ZDY.getEnName().equals(entity.getThresholdType())) {
                if (StringUtils.isBlank(entity.getLimitCount()) || StringUtils.isBlank(entity.getLimitTime())) {
                    return false;
                }
            } else {
                if (StringUtils.isBlank(entity.getThresholdVal())) {
                    return false;
                }
            }
        }
        if (StringUtils.isBlank(entity.getEffect())) {
            return false;
        } else {
            if (Effect.WAIT.getEnName().equals(entity.getEffect())) {
                if (StringUtils.isBlank(entity.getWaitTime())) {
                    return false;
                }
            }
        }
        return true;
    }
}

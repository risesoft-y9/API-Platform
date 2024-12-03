package net.risesoft.service.impl;

import net.risesoft.enums.platform.ManagerLevelEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.AuthDictService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.AuthDictDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "AuthDictService")
public class AuthDictServiceImpl implements AuthDictService {

    @Autowired
    private AuthDictRepository authDictRepository;

    @Autowired
    private InterfaceManageRepository interfaceManageRepository;

    @Override
    public Page<AuthDict> getAuthDictList(AuthDict authDict, int page, int limit) {
        Specification<AuthDict> spec = new Specification<AuthDict>() {
            @Override
            public Predicate toPredicate(Root<AuthDict> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(authDict.getId())) {
                    CriteriaBuilder.In<Object> ids = criteriaBuilder.in(root.get("id"));
                    String[] checkIds = authDict.getId().split(",");
                    for (String it : checkIds) {
                        ids.value(it);
                    }
                    predicates.add(ids);
                }
                if (StringUtils.isNotBlank(authDict.getFieldName())) {
                    predicates.add(criteriaBuilder.like(root.get("fieldName"), "%" + authDict.getFieldName() + "%"));
                }
                if (StringUtils.isNotBlank(authDict.getParameterName())) {
                    predicates.add(criteriaBuilder.like(root.get("parameterName"), "%" + authDict.getParameterName() + "%"));
                }
                if (StringUtils.isNotBlank(authDict.getParameterId())) {
                    predicates.add(criteriaBuilder.equal(root.get("parameterId"), authDict.getParameterId()));
                }
                if (StringUtils.isNotBlank(authDict.getShowVal())) {
                    predicates.add(criteriaBuilder.like(root.get("showVal"), "%" + authDict.getShowVal() + "%"));
                }
                if (StringUtils.isNotBlank(authDict.getFieldVal())) {
                    predicates.add(criteriaBuilder.like(root.get("fieldVal"), "%" + authDict.getFieldVal() + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("isDelete"), "N"));
                if (StringUtils.isNotBlank(authDict.getIsPrimary())) {
                    predicates.add(criteriaBuilder.equal(root.get("isPrimary"), authDict.getIsPrimary()));
                }
                if (StringUtils.isNotBlank(authDict.getPersonId())) {
                    UserInfo person = Y9LoginUserHolder.getUserInfo();
                    //判断登录人是否有管理员权限
                    if (person.getManagerLevel().getValue() == ManagerLevelEnum.SYSTEM_MANAGER.getValue()) {

                    } else {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.or(criteriaBuilder.equal(root.get("personId"), authDict.getPersonId()), criteriaBuilder.equal(root.get("parameterType"), "公有"))));
                    }
                }
                if (StringUtils.isNotBlank(authDict.getParameterType())) {
                    predicates.add(criteriaBuilder.equal(root.get("parameterType"), authDict.getParameterType()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("sort").ascending());
        Page<AuthDict> page1 = authDictRepository.findAll(spec, pageable);
        return page1;
    }

    @Override
    public List<AuthDictDTO> getAuthDictTreeList(AuthDict authDict) {
        Page<AuthDict> page = getAuthDictList(authDict, 1, 999999);
        List<AuthDict> dataList = page.getContent();
        Map<String, String> pidsMap = new HashMap<>();
        List<AuthDictDTO> dtos = new ArrayList<>();
        if (dataList != null && dataList.size() != 0) {
            if (StringUtils.isBlank(authDict.getPid())) {
                return null;
            }
            for (AuthDict authDict1 : dataList) {
                pidsMap.put(authDict1.getPid(), "1");
            }
            for (AuthDict authDict1 : dataList) {
                if (StringUtils.isNotBlank(authDict.getPid()) && authDict.getPid().equals(authDict1.getPid())) {
                    AuthDictDTO authDictDTO = new AuthDictDTO(authDict1);
                    if (pidsMap.get(authDictDTO.getId()) != null) {
                        authDictDTO.setHasChildren(true);
                    } else {
                        authDictDTO.setHasChildren(false);
                    }
                    dtos.add(authDictDTO);
                } else if (StringUtils.isNotBlank(authDict.getFieldVal()) || StringUtils.isNotBlank(authDict.getShowVal())) {
                    AuthDictDTO authDictDTO = new AuthDictDTO(authDict1);
                    dtos.add(authDictDTO);
                }
            }
        }
        return dtos;
    }

    @Override
    public Map<String, String> delInfo(String id) {
        Map<String, String> map = new HashMap<>();
        AuthDict authDict = authDictRepository.findById(id).orElse(null);
        InterfaceManage interfaceManage = null;
        if (authDict != null) {
            if (StringUtils.isNotBlank(authDict.getInterfaceId())) {
                interfaceManage = interfaceManageRepository.findById(authDict.getInterfaceId()).orElse(null);
            }
            if (interfaceManage != null) {
                if (!"Y".equals(interfaceManage.getIsDelete())) {
                    if (interfaceManage.getParameterIds().indexOf(authDict.getParameterId()) != -1) {
                        map.put("status", "error");
                        map.put("msg", "删除失败，有接口正在使用该参数");
                        return map;
                    }
                }
            }

            if ("Y".equals(authDict.getIsPrimary())) {
                List<AuthDict> list = authDictRepository.findByParameterId(authDict.getParameterId());
                for (AuthDict authDict1 : list) {
                    authDict1.setIsDelete("Y");
                }
                authDictRepository.saveAll(list);
                map.put("status", "success");
            } else {
                authDict.setIsDelete("Y");
                authDictRepository.save(authDict);
                map.put("status", "success");
            }
        } else {
            map.put("status", "error");
            map.put("msg", "未查询到该条数据，请刷新重试！！！");
        }
        return map;
    }

    @Override
    public Map<String, String> delInfoPerson(String id) {
        Map<String, String> map = new HashMap<>();
        AuthDict authDict = authDictRepository.findById(id).orElse(null);
        if (authDict != null) {
            if ("Y".equals(authDict.getIsPrimary())) {
                List<AuthDict> list = authDictRepository.findByParameterId(authDict.getParameterId());
                for (AuthDict authDict1 : list) {
                    authDict1.setIsDelete("Y");
                }
                authDictRepository.saveAll(list);
                map.put("status", "success");
            } else {
                authDict.setIsDelete("Y");
                authDictRepository.save(authDict);
                map.put("status", "success");
            }
        } else {
            map.put("status", "error");
            map.put("msg", "id为空不能删除");
        }
        return map;
    }

    @Override
    public AuthDict getAuthDictInfoById(String id) {
        return authDictRepository.findById(id).orElse(null);
    }

    @Override
    public Map<String, Object> saveAuthDictInfo(AuthDict authDict) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(authDict.getId())) {
            authDict.setIsDelete("N");
            authDict.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            if (StringUtils.isBlank(authDict.getParameterId())) {
                authDict.setParameterId(authDict.getId());
            }
        } else {
            AuthDict authDict1 = authDictRepository.findById(authDict.getId()).orElse(null);
            if (StringUtils.isBlank(authDict1.getId())) {
                map.put("status", false);
                return map;
            }
        }
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        //判断登录人是否有管理员权限
        if (person.getManagerLevel().getValue().equals(ManagerLevelEnum.SYSTEM_MANAGER.getValue())) {
            authDict.setParameterType("公有");
        } else {
            authDict.setParameterType("私有");
        }
        authDict.setPersonId(person.getPersonId());
        authDict.setPersonName(person.getName());
        AuthDict authDict1 = authDictRepository.save(authDict);
        if (StringUtils.isNotBlank(authDict1.getId())) {
            map.put("status", true);
        } else {
            map.put("status", false);
        }
        return map;
    }

    @Override
    public Page<AuthDict> getDictKeyList(AuthDict authDict, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<AuthDict> page1 = authDictRepository.findDictKeyPage("%" + authDict.getFieldName() + "%", "%" + authDict.getParameterName() + "%", pageable);
        return page1;
    }

    @Override
    public Map<String, Object> copyAuthDictListByInterfaceId(String oldId, String newId) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(oldId) && StringUtils.isNotBlank(newId)) {
            List<AuthDict> list = authDictRepository.findByInterfaceId(oldId);
            for (AuthDict authDict : list) {
                authDict.setInterfaceId(newId);
                authDict.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            }
            authDictRepository.saveAll(list);
            map.put("status", true);
        } else {
            map.put("status", false);
            map.put("msg", "id为空");
        }
        return map;
    }


}

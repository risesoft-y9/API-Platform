package net.risesoft.service.impl;

import net.risesoft.id.Y9IdGenerator;
import net.risesoft.service.SystemIdentifierService;
import net.risesoft.y9public.entity.SystemIdentifier;
import net.risesoft.y9public.repository.SystemIdentifierRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@Service(value = "SystemIdentifierService")
public class SystemIdentifierServiceImpl implements SystemIdentifierService {

    @Autowired
    private SystemIdentifierRepository systemIdentifierRepository;
    @Override
    public Page<SystemIdentifier> getSystemIdentifierPage(SystemIdentifier systemIdentifier,int page ,int limit) {
        Specification<SystemIdentifier> spec= new Specification<SystemIdentifier>(){
            @Override
            public Predicate toPredicate(Root<SystemIdentifier> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(systemIdentifier.getName())){
                    predicates.add(criteriaBuilder.like(root.get("name"),"%"+systemIdentifier.getName()+"%"));
                }
                if (StringUtils.isNotBlank(systemIdentifier.getParameterType())){
                    predicates.add(criteriaBuilder.equal(root.get("parameterType"),systemIdentifier.getParameterType()));
                }
                predicates.add(criteriaBuilder.equal(root.get("isDelete"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(page-1,limit, Sort.by("sort").ascending());
        Page<SystemIdentifier> page1 = systemIdentifierRepository.findAll(spec,pageable);
        return page1;
    }

    @Override
    public List<SystemIdentifier> getSystemIdentifierList(String type) {
        return systemIdentifierRepository.findByParameterTypeAndIsDelete(type,"N");
    }

    @Override
    public List<SystemIdentifier> getSystemIdentifierListByPid(String id) {
        return systemIdentifierRepository.findByParameterTypeAndPidAndIsDelete("1",id,"N");
    }

    @Override
    public Map<String, String> delInfo(String id) {
        Map<String,String> map = new HashMap<>();
        if(StringUtils.isBlank(id)){
            map.put("status","err");
            map.put("msg","id为空不能删除");
        }
        SystemIdentifier systemIdentifier = systemIdentifierRepository.findById(id).orElse(null);
        if (systemIdentifier!=null){
            systemIdentifier.setIsDelete("Y");
            List<SystemIdentifier> systemIdentifiers = systemIdentifierRepository.findByParameterTypeAndPidAndIsDelete("1", id, "N");
            if (systemIdentifiers.size()!=0){
                map.put("status","err");
                map.put("msg","此数据下有系统未删除，请先删除下属系统");
            }else {
                systemIdentifierRepository.save(systemIdentifier);
                map.put("status","success");
            }
        }else {
            map.put("status","err");
            map.put("msg","没有数据");
        }
        return map;
    }

    @Override
    public Map<String, Object> saveInfo(SystemIdentifier systemIdentifier) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(systemIdentifier.getId())){
            systemIdentifier.setId(Y9IdGenerator.genId());
            systemIdentifier.setIsDelete("N");
        }
        SystemIdentifier identifier = systemIdentifierRepository.save(systemIdentifier);
        if(identifier!=null){
            map.put("status",true);
        }else {
            map.put("status",false);
        }
        return map;
    }

    @Override
    public SystemIdentifier getInfoById(String id) {
        return systemIdentifierRepository.findById(id).orElse(null);
    }

}

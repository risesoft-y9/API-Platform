package net.risesoft.service.impl;

import net.risesoft.id.Y9IdGenerator;
import net.risesoft.service.BlacklistingService;

import net.risesoft.y9public.entity.Blacklisting;
import net.risesoft.y9public.repository.BlacklistingRepository;
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

@Service(value = "BlacklistingService")
public class BlacklistingServiceImpl implements BlacklistingService {

    @Autowired
    private BlacklistingRepository blacklistingRepository;

    @Override
    public Page<Blacklisting> getPage(Blacklisting blacklisting, int page, int limit) {
        Specification<Blacklisting> spec = new Specification<Blacklisting>() {
            @Override
            public Predicate toPredicate(Root<Blacklisting> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(blacklisting.getName())) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + blacklisting.getName() + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("isDelete"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("sort").ascending());
        Page<Blacklisting> page1 = blacklistingRepository.findAll(spec, pageable);
        return page1;
    }

    @Override
    public Map<String, String> delInfo(String id) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(id)) {
            map.put("status", "err");
            map.put("msg", "id为空不能删除");
        }
        Blacklisting blacklisting = blacklistingRepository.findById(id).orElse(null);
        if (blacklisting != null) {
            blacklisting.setIsDelete("Y");
            blacklistingRepository.save(blacklisting);
            map.put("status", "success");
        } else {
            map.put("status", "err");
            map.put("msg", "没有数据");
        }
        return map;
    }

    @Override
    public Map<String, Object> saveInfo(Blacklisting blacklisting) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(blacklisting.getId())) {
            blacklisting.setId(Y9IdGenerator.genId());
            blacklisting.setIsDelete("N");
        }
        blacklisting.setInterfaceIds("null," + blacklisting.getInterfaceIds());
        Blacklisting identifier = blacklistingRepository.save(blacklisting);
        if (identifier != null) {
            map.put("status", true);
        } else {
            map.put("status", false);
        }
        return map;
    }

    @Override
    public Blacklisting getInfoById(String id) {
        return blacklistingRepository.findById(id).orElse(null);
    }

    @Override
    public Map<String, Object> updateEnable(Blacklisting blacklisting) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(blacklisting.getId())) {
            Blacklisting entity = blacklistingRepository.findById(blacklisting.getId()).orElse(null);
            if (entity != null) {
                if (StringUtils.isNotBlank(blacklisting.getIsEnable())) {
                    entity.setIsEnable(blacklisting.getIsEnable());
                    blacklistingRepository.save(entity);
                    map.put("status", true);
                } else {
                    map.put("status", false);
                    map.put("msg", "停启用信息为空");
                }
            } else {
                map.put("status", false);
                map.put("msg", "没有查询到信息");
            }
        } else {
            map.put("status", false);
            map.put("msg", "id为空");
        }
        return map;
    }

    @Override
    public List<Blacklisting> getListByInterfaceId(String interfaceId) {
        List<Blacklisting> list = blacklistingRepository.findByInterfaceId("%," + interfaceId + "%");
        return list;
    }

}

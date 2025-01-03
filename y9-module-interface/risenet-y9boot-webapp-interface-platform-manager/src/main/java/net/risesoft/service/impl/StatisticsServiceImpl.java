package net.risesoft.service.impl;

import cn.hutool.core.map.MapBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.risesoft.model.InterfaceStatus;
import net.risesoft.service.StatisticsService;
import net.risesoft.y9public.entity.CallApiRequestLogInfo;
import net.risesoft.y9public.entity.InterfaceLimitInfo;
import net.risesoft.y9public.repository.CallApiLogRepository;
import net.risesoft.y9public.repository.InterfaceLimitInfoRepository;
import net.risesoft.y9public.repository.InterfaceManageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : lxd
 * @description : 用于统计首页可视化的统计数据
 * @createDate : 2024/10/30 14:48
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private InterfaceLimitInfoRepository interfaceLimitInfoRepository;

    @Autowired
    private InterfaceManageRepository interfaceManageRepository;

    @Autowired
    private CallApiLogRepository callApiLogRepository;


    @Override
    public Map<String, Object> getInterfaceOverview() {
        Map<String, Object> res = new HashMap<>();
        try {
            //停用数、发布数、注册数
            long stopCount = interfaceManageRepository.getStatusCount(InterfaceStatus.UN_APPROVE.getName());
            long publishCount = interfaceManageRepository.getStatusCount(InterfaceStatus.APPROVE.getName());
            long registerCount = interfaceManageRepository.getAllCount();
            Map<String, Long> data = MapBuilder.create(new HashMap<String, Long>())
                    .put("stopCount", stopCount)
                    .put("publishCount", publishCount)
                    .put("registerCount", registerCount)
                    .map();

            res.put("msg", "");
            res.put("code", "0");
            res.put("data", data);
        } catch (Exception e) {
            res.put("code", "1");
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public Map<String, Object> getCallTrend(String type) {
        return null;
    }

    @Override
    public Map<String, Object> getLogOverview() {
        return null;
    }

    @Override
    public Map<String, Object> getException(int page, int limit) {
        return null;
    }

    @Override
    public Map<String, Object> getLogMonitoringOptions() {
        Map<String, Object> res = new HashMap<>();
        try {
            Map<String,Set<String>> data = new HashMap();

            List<Map<String, Object>> dataGroup = callApiLogRepository.getDataGroup();
            List<Map<String, Object>> deptName = callApiLogRepository.getDataGroupByDeptName();
            for(Map<String,Object> map : dataGroup){
                for(String key : map.keySet()){
                    if(data.get(key)==null){
                        Set<String> set = new HashSet<>();
                        data.put(key,set);
                    }
                    if(map.get(key)!=null && !map.get(key).equals("null") && StringUtils.isNotBlank(map.get(key).toString())){
                        String[] strs = map.get(key).toString().split(",");
                        for(String str : strs){
                            data.get(key).add(str);
                        }
                    }
                }
            }
            List<Map<String,Object>> deptList = new ArrayList<>();
            for (Map<String,Object> map : deptName){
                if (map.get("deptName") != null && !map.get("deptName").equals("null") && StringUtils.isNotBlank(map.get("deptName").toString())) {
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("label", map.get("deptName"));
                    pMap.put("value", map.get("deptName"));
                    if (map.get("applySystemName") != null && !map.get("applySystemName").equals("null") && StringUtils.isNotBlank(map.get("applySystemName").toString())) {
                        String[] applySystemNames = map.get("applySystemName").toString().split(",");
                        List<Map<String, Object>> cMapList = new ArrayList<>();
                        for (String applySystemName : applySystemNames) {
                            Map<String, Object> cMap = new HashMap<>();
                            cMap.put("label", applySystemName);
                            cMap.put("value", applySystemName);
                            cMapList.add(cMap);
                        }
                        pMap.put("children", cMapList);
                    }
                    deptList.add(pMap);
                }

            }
            Map rtData = new HashMap();
            rtData.putAll(data);
            rtData.put("deptName",deptList);
            res.put("data", rtData);
            res.put("code", "0");
            res.put("msg", "");
            return res;
        } catch (Exception e) {
            res.put("code", "1");
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Map<String, Object> getLogMonitoringInfo(Map<String, Object> conditionMap) {
        Map<String, Object> res = new HashMap<>();
        try {
            Specification<CallApiRequestLogInfo> query = buildLogQueryBuilder(conditionMap);
            int pageSize = (int) conditionMap.getOrDefault("pageSize", 10);
            int page = (int) conditionMap.getOrDefault("page", 1);
            page = page -1;
            PageRequest pageRequest = PageRequest.of(page, pageSize,Sort.by("requestStartTime").descending());
            Page<CallApiRequestLogInfo> all = callApiLogRepository.findAll(query, pageRequest);
            List<Map> entiyList = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            all.getContent().stream().forEach(m ->{
                Map<String,Object> map = mapper.convertValue(m,Map.class);
                entiyList.add(map);
            });
            Map<String, List<Map>> tempData = entiyList.stream()
                    .peek(map -> {
                        String requestStartTime = String.valueOf(map.get("requestStartTime"));
                        map.put("requestStartTime",removeMilliseconds(requestStartTime));
                    })
                    .collect(Collectors.groupingBy(map -> String.valueOf(map.get("interfaceId")), Collectors.toList()));

            //到限流表中根据interfaceId查找限流接口的信息，最后补充数据返回。（没有找到的证明非限流接口不做处理）
            Set<String> ids = new HashSet<>();
            for(String key: tempData.keySet()){
                if(tempData.get(key)!=null && tempData.get(key).get(0)!=null && "1".equals(tempData.get(key).get(0).get("isLimit").toString())){
                    ids.add(key);
                }
            }
            List<InterfaceLimitInfo> interfaces = interfaceLimitInfoRepository.findByIds(ids);
            interfaces.stream().forEach(entity -> {
                String limitRule = "";
                String type = entity.getThresholdType();
                //自定义
                if ("0".equals(type)) {
                    limitRule = String.format("%.2f 次/秒", Double.parseDouble(entity.getLimitCount()) / Double.parseDouble(entity.getLimitTime()));
                }
                //阈值
                if ("1".equals(type)) {
                    limitRule = entity.getThresholdVal();
                }

                String finalLimitRule = limitRule;
                Optional.ofNullable(tempData.get(entity.getInterfaceId()))
                        .ifPresent(listMap -> listMap.forEach(m -> m.put("rule", finalLimitRule)));

            });

            //合并，并根据请求时间排序
            List<Map> data = tempData.values().stream()
                    .flatMap(Collection::stream)
                    .sorted((o1, o2) -> String.valueOf(o2.get("requestStartTime")).compareTo(String.valueOf(o1.get("requestStartTime"))))
                    .collect(Collectors.toList());

            res.put("data", data);
            res.put("code", "0");
            res.put("count", all.getTotalElements());
            res.put("msg", "");
            return res;
        } catch (Exception e) {
            res.put("code", "1");
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Map<String, Object> getRealTimeLog(int page, int limit) {
        return null;
    }

    @Override
    public Map<String, Object> getRunningCount() {
        return null;
    }


    private Specification<CallApiRequestLogInfo> buildLogQueryBuilder(Map<String, Object> conditionMap) {
        List<String> termFieldNames = List.of(
                "interfaceType", "deptName", "responseMsg", "authentic", "authenticationResult", "isLimit", "limitResult",
                "applySystemName", "courtName"
        );
        List<String> wildFieldNames = List.of("apiName", "userName", "serverIP", "requestIP", "requestUserName", "limitTime");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Specification<CallApiRequestLogInfo> spec= new Specification<CallApiRequestLogInfo>(){
            @Override
            public Predicate toPredicate(Root<CallApiRequestLogInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (conditionMap.get("requestStartTime") != null && StringUtils.isNotBlank(conditionMap.get("requestStartTime").toString())) {
                    String[] split = String.valueOf(conditionMap.get("requestStartTime")).split("~");
                    DateTimeFormatter aformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                    DateTimeFormatter bformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String startDate = LocalDateTime.parse(split[0], bformatter).format(aformatter);
                    String endDate = LocalDateTime.parse(split[1], bformatter).format(aformatter);

                    if (split.length == 2) {
                        try {
                            predicates.add(criteriaBuilder.between(root.get("requestStartTime"),sdf.parse(startDate),sdf.parse(endDate)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (conditionMap.get("deptSystem") != null && StringUtils.isNotBlank(conditionMap.get("deptSystem").toString())) {
                    if (conditionMap.get("deptSystem") instanceof Map) {
                        Map<String, List<String>> map = (Map) conditionMap.get("deptSystem");
                        if (!map.isEmpty()) {
                            for (String deptName : map.keySet()) {
                                predicates.add(criteriaBuilder.equal(root.get("deptName"),deptName));
                                if (map.get(deptName) != null && map.get(deptName).size() > 0) {
                                    CriteriaBuilder.In<Object> applySystemNames = criteriaBuilder.in(root.get("applySystemName"));
                                    for (String it : map.get(deptName)) {
                                        applySystemNames.value(it);
                                    }
                                    predicates.add(applySystemNames);
                                }
                            }
                        }
                    }
                }
                for (String fieldName : termFieldNames) {
                    Optional.ofNullable(conditionMap.get(fieldName))
                            .map(Object::toString)  // 将termValue转换为字符串，以便后续比较
                            .filter(StringUtils::isNotBlank)
                            .ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get(fieldName),value)));
                }

                for (String fieldName : wildFieldNames) {
                    Optional.ofNullable(conditionMap.get(fieldName))
                            .map(Object::toString)  // 将termValue转换为字符串，以便后续比较
                            .filter(StringUtils::isNotBlank)
                            .ifPresent(value -> predicates.add(criteriaBuilder.like(root.get(fieldName),"%"+value+"%")));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        return spec;
    }


    private String removeMilliseconds(String dateTime) {
        if (StringUtils.isBlank(dateTime) || "null".equals(dateTime)) {
            return "";
        }

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, inputFormatter);
        // 定义输出格式，不包括毫秒
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(outputFormatter);
    }
}
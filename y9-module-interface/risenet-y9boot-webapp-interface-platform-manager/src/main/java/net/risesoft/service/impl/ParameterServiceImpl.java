package net.risesoft.service.impl;

import com.alibaba.fastjson.JSONArray;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.service.ParameterService;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.dto.ParameterDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service(value = "ParameterService")
public class ParameterServiceImpl implements ParameterService {
    @Autowired
    private ParameterRepository parameterRepository;

    @Override
    public Map<String, String> getParameterData(String interfaceId,String method,String interfaceType) {
        Map<String,String> map = new HashMap<>();
        List<Parameter> list = parameterRepository.findAllByInterfaceId(interfaceId);
        List<Parameter> parameters = new ArrayList<>();
        List<Parameter> reqDatas = new ArrayList<>();
        List<Parameter> resDatas = new ArrayList<>();
        for(Parameter p : list){
            if ("1".equals(p.getParameterStatus())){
                parameters.add(p);
            }
            if ("2".equals(p.getParameterStatus())){
                if ("get".equals(method)||"webService".equals(interfaceType)){
                    parameters.add(p);
                    reqDatas.add(p);
                }else {
                    reqDatas.add(p);
                }
            }
            if ("3".equals(p.getParameterStatus())){
                resDatas.add(p);
            }
        }
        map.put("parameters",JSONArray.toJSONString(parameters));
        map.put("reqParameters",JSONArray.toJSONString(integraTreeData(reqDatas)));
        map.put("resParameters",JSONArray.toJSONString(integraTreeData(resDatas)));
        return map;
    }

    @Transactional(rollbackOn = RuntimeException.class)
    @Override
    public Map<String, Object> saveParameterData(InterfaceManageDTO dto) {
        Map<String,Object> map = new HashMap<>();
        List<Parameter> list = new ArrayList<>();
        if(StringUtils.isNotBlank(dto.getParameters())){
            List<Parameter> lists = JSONArray.parseArray(dto.getParameters(),Parameter.class);
            for (Parameter parameter : lists){
                parameter.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                parameter.setInterfaceId(dto.getId());
                parameter.setPersonId(dto.getPersonId());
            }
            list.addAll(lists);
        }
        if (StringUtils.isNotBlank(dto.getReqParameters())){
            List<ParameterDTO> lists = JSONArray.parseArray(dto.getReqParameters(),ParameterDTO.class);
            list.addAll(integraData(lists,dto.getPersonId(),dto.getId()));
        }
        if (StringUtils.isNotBlank(dto.getResParameters())){
            List<ParameterDTO> lists = JSONArray.parseArray(dto.getResParameters(),ParameterDTO.class);
            list.addAll(integraData(lists,dto.getPersonId(),dto.getId()));
        }
        if (StringUtils.isNotBlank(dto.getId())){
            try {
                parameterRepository.deleteByInterfaceId(dto.getId());
                if (list.size()!=0){
                    parameterRepository.saveAll(list);
                }
            }catch (Exception e){
                map.put("status",false);
                e.printStackTrace();
                return map;
            }
        }
        map.put("status",true);
        return map;
    }

    @Override
    public List<Parameter> getRequiredParameterData(String interfaceId) {
        return parameterRepository.findAllByInterfaceIdAndRequired(interfaceId,"是");
    }

    @Override
    public List<Parameter> getParameterDataByInterfaceIds(List<String> interfaceIds) {
        return parameterRepository.findAllByInterfaceIdIn(interfaceIds);
    }

    @Override
    public void saveAllParameterData(List<Parameter> list) {
        if(list.size()<30){
            parameterRepository.saveAll(list);
        }else {
            int i=0;
            int batch = 0;
            List<List<Parameter>> group = new ArrayList<>();
            for(Parameter parameter : list){
                if (i/30!=batch){
                    List<Parameter> newList = new ArrayList<>();
                    group.add(newList);
                    batch = i/30;
                }
                group.get(batch).add(parameter);
                i++;
            }
            for(List<Parameter> itemList : group){
                parameterRepository.saveAll(itemList);
            }
        }
    }

    //递归遍历数据，将多层结构转为一层结构
    private void selectChildren(List<ParameterDTO> list,List<ParameterDTO> arrData){
        for(ParameterDTO dto:list){
            if(dto.getChildren()!=null && dto.getChildren().size()!=0){
                selectChildren(dto.getChildren(),arrData);
            }
            arrData.add(dto);
        }
    }

    //保存转化树形数据
    private List<Parameter> integraData(List<ParameterDTO> lists,String personId,String interfaceId){
        List<Parameter> list = new ArrayList<>();
        List<ParameterDTO> arrData = new ArrayList<>();
        selectChildren(lists,arrData);
        //changeIdMap用来置换id，将前端的零时id置换为唯一id
        Map<String,String> changeIdMap = new HashMap<>();
        for (ParameterDTO dtoIt : arrData){
            changeIdMap.put(dtoIt.getId(),Y9IdGenerator.genId(IdType.SNOWFLAKE));
            dtoIt.setId(changeIdMap.get(dtoIt.getId()));
            dtoIt.setPersonId(personId);
            dtoIt.setInterfaceId(interfaceId);
        }
        //置换pid
        for (ParameterDTO dtoIt : arrData){
            if (!"0".equals(dtoIt.getPid())){
                dtoIt.setPid(changeIdMap.get(dtoIt.getPid()));
            }
        }
        //统一保存
        for (ParameterDTO dtoIt : arrData){
            Parameter entity = new Parameter(dtoIt);
            list.add(entity);
        }
        return list;
    }

    //递归组装树形数据
    private void assembleData(ParameterDTO node,Map<String,List<Parameter>> pidMaps){
        if(pidMaps.get(node.getId())!=null){
            List<ParameterDTO> list = new ArrayList<>();
            if(pidMaps.get(node.getId()).size()!=0){
                for (Parameter parameter : pidMaps.get(node.getId())){
                    ParameterDTO parameterDTO = new ParameterDTO(parameter);
                    assembleData(parameterDTO,pidMaps);
                    list.add(parameterDTO);
                }
            }
            node.setChildren(list);
        }
    }

    //外层组装树形数据
    private List<ParameterDTO> integraTreeData(List<Parameter> lists){
        List<ParameterDTO> list = new ArrayList<>();
        Map<String,List<Parameter>> pidMaps = new HashMap<>();
        for(Parameter parameter : lists){
            if("0".equals(parameter.getPid())){
                ParameterDTO parameterDTO = new ParameterDTO(parameter);
                list.add(parameterDTO);
            }else {
                if(pidMaps.get(parameter.getPid())!=null){
                    pidMaps.get(parameter.getPid()).add(parameter);
                }else {
                    List<Parameter> parameters = new ArrayList<>();
                    parameters.add(parameter);
                    pidMaps.put(parameter.getPid(),parameters);
                }
            }
        }
        for(ParameterDTO dto : list){
            assembleData(dto,pidMaps);
        }
        return list;
    }
}

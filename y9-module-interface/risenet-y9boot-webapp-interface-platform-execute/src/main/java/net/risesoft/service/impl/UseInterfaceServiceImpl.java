package net.risesoft.service.impl;

import com.alibaba.fastjson.JSONArray;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.UseInterfaceService;
import net.risesoft.util.*;
import net.risesoft.y9.Y9Context;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.dto.ParameterDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.repository.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "UseInterfaceService")
public class UseInterfaceServiceImpl implements UseInterfaceService {

    @Autowired
    private InterfaceManageRepository interfaceManageRepository;
    @Autowired
    private ParameterRepository parameterRepository;
    @Autowired
    private InterfaceLimitInfoRepository interfaceLimitInfoRepository;
    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private RestUtil restUtil;
    @Autowired
    private AuthCheckUtil authCheckUtil;
    @Autowired
    private InterfaceApplyRepository interfaceApplyRepository;
    @Autowired
    private WebServiceForwardUtil webServiceForwardUtil;
    @Autowired
    private CallLogUtil calllogUtil;
    @Autowired
    private BlacklistingRepository blacklistingRepository;

    public static final String regx = "@-@";

    @Override
    public ResponseEntity forward(String id, HttpServletRequest request,HttpServletResponse responseRt) {
        Calendar calendar = Calendar.getInstance();
        String logId = Y9IdGenerator.genId(IdType.SNOWFLAKE);
        HashMap<String, String> logMap = new HashMap<>();
        calendar.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        logMap.put("requestStartTime", sdf.format(calendar.getTime()));
        Map<String, Object> map = new HashMap<>();
        InterfaceManage interfaceManage = new InterfaceManage();
        InterfaceApply interfaceApply = new InterfaceApply();

        Map<String, Object> body = new HashMap<>();
        try {
            try {
                interfaceApply = interfaceApplyRepository.findDataByUserKey(id);
                if (StringUtils.isBlank(interfaceApply.getIsEffective()) || "N".equals(interfaceApply.getIsEffective())) {
                    map.put("msg", "请求失败！申请信息已经过期请重新申请");

                    logMap.put("status", "接口信息查询失败");
                    logMap.put("errMsg", "请求失败！申请信息已经过期请重新申请");
                    logMap.put("code", "401");
                    return ResponseEntity.ok().body(map);
                }
                if (request.getHeader("userName") == null) {
                    logMap.put("userName", interfaceApply.getUsePersonResponsible());
                }
                if (request.getHeader("userId") == null) {
                    logMap.put("userId", interfaceApply.getApplyPersonId());
                }
                List<Blacklisting> blacklistingList = blacklistingRepository.findByInterfaceId("%,"+interfaceApply.getInterfaceId()+"%");
                if (blacklistingList.size() != 0) {
                    String hostIp = Y9Context.getIpAddr(request);
                    for (Blacklisting blacklisting : blacklistingList) {
                        if (blacklisting.getIp().indexOf(hostIp) != -1) {
                            map.put("msg", "请求失败！该系统的IP已经拉入黑名单");

                            logMap.put("status", "失败");
                            logMap.put("errMsg", "请求失败！该系统的IP已经拉入黑名单！");
                            logMap.put("code", "401");
                            return ResponseEntity.ok().body(map);
                        }
                    }
                }
                if (interfaceApply != null && StringUtils.isNotBlank(interfaceApply.getInterfaceId())) {
                    interfaceManage = interfaceManageRepository.findById(interfaceApply.getInterfaceId()).orElse(null);
                }
            } catch (Exception e) {
                map.put("msg", "请求失败！接口相关信息查询失败");
                logMap.put("status", "接口相关信息查询失败");
                logMap.put("errMsg", e.getMessage());
                logMap.put("code", "501");
                e.printStackTrace();
                return ResponseEntity.ok().body(map);
            }
            ResponseEntity<String> response = null;
            if (interfaceManage == null) {
                map.put("msg", "请求失败！无权限");
                logMap.put("status", "请求失败");
                logMap.put("errMsg", "请求失败！无权限");
                logMap.put("code", "501");
                return ResponseEntity.ok().body(map);
            }
            InterfaceManageDTO interfaceManageDTO = new InterfaceManageDTO(interfaceManage);
            //判断开启限流
            if ("是".equals(interfaceManageDTO.getIsLimit())) {
                InterfaceLimitInfo interfaceLimitInfo = interfaceLimitInfoRepository.findAllByInterfaceId(interfaceManageDTO.getId());
                if (interfaceLimitInfo != null) {
                    try {
                        calendar.setTime(new Date());
                        Long limitStartTime = calendar.getTimeInMillis();
                        boolean isPass = redissonUtil.isPass(interfaceLimitInfo, "");
                        if (!isPass) {
                            map.put("msg", "请求失败！请求频率过高，已经达到该接口上限，请稍后尝试！");

                            logMap.put("limitMsg", "不通过");
                            logMap.put("status", "请求失败");
                            logMap.put("errMsg", "请求失败！请求频率过高，已经达到该接口上限，请稍后尝试！");
                            logMap.put("code", "401");
                            calendar.setTime(new Date());
                            Long limitEndTime = calendar.getTimeInMillis();
                            logMap.put("limitWaitTime", String.valueOf(limitEndTime - limitStartTime));
                            return ResponseEntity.ok().body(map);
                        }
                        logMap.put("limitMsg", "通过");
                        calendar.setTime(new Date());
                        Long limitEndTime = calendar.getTimeInMillis();
                        logMap.put("limitWaitTime", String.valueOf(limitEndTime - limitStartTime));
                    } catch (Exception e) {
                        map.put("msg", "请求失败");

                        logMap.put("status", "请求失败");
                        logMap.put("errMsg", "请求失败！限流失败！");
                        logMap.put("code", "401");
                        logMap.put("limitMsg", "限流失败");
                        return ResponseEntity.ok().body(map);
                    }
                }
            }

            String method = request.getMethod();
            if (!("post".equals(method.toLowerCase()))) {
                map.put("msg", "请求失败！请求方式不支持！");

                logMap.put("status", "请求失败");
                logMap.put("errMsg", "请求失败！请求方式不支持！");
                logMap.put("code", "401");
                return ResponseEntity.ok().body(map);
            } else {
                Enumeration<String> parameterNames = request.getParameterNames();
                if (!parameterNames.hasMoreElements()) {
                    body = readBody(request);
                }
                if (body == null) {
                    body = new HashMap<>();
                }
                while (parameterNames.hasMoreElements()) {
                    String key = parameterNames.nextElement();
                    String value = request.getParameter(key);
                    body.put(key, value);
                }
            }

            logMap.put("requestParam", JSONArray.toJSONString(body));
            List<String> errList = new ArrayList<>();
            List<Parameter> list = parameterRepository.findAllByInterfaceId(interfaceManageDTO.getId());
            HttpHeaders headers = new HttpHeaders();
            headers = getHeaders(request, list, errList);
            Map<String, Object> restMap = getRestParams(interfaceManageDTO, list, body, errList);

            if (errList.size() != 0) {
                map.put("msg", errList);
                logMap.put("status", "请求失败");
                logMap.put("errMsg", JSONArray.toJSONString(errList));
                logMap.put("code", "401");
                return ResponseEntity.ok().body(map);
            }
            if (!"Rest".equals(interfaceManageDTO.getInterfaceType())) {
                Map<String, Object> resultMap = new HashMap<>();
                if (!authCheckUtil.checkAki(request, body, interfaceManage, interfaceApply, resultMap, restMap)) {
                    map.putAll(resultMap);
                    logMap.put("status", "请求失败");
                    logMap.put("errMsg", resultMap.get("msg").toString());
                    logMap.put("code", resultMap.get("status").toString());
                    logMap.put("authMsg", "失败");
                    return ResponseEntity.ok().body(map);
                }
                logMap.put("status", "成功");
                logMap.put("authMsg", "成功");
                List<Object> params = getParams(interfaceManageDTO, list, body);
                logMap.put("forwardRequestParam", JSONArray.toJSONString(params));
                calendar.setTime(new Date());
                logMap.put("forwardStartTime", String.valueOf(calendar.getTimeInMillis()));
                Map<String, Object> objectMap = webServiceForwardUtil.forward(interfaceManage, params, getHeadersRtMap(request, list));
                if (!(boolean) objectMap.get("status")) {
                    logMap.put("status", "请求失败");
                    logMap.put("errMsg", objectMap.get("msg") != null ? objectMap.get("msg").toString() : "对方系统请求失败");
                    logMap.put("code", "402");
                    logMap.put("forwardResponseMsg", "失败");
                    logMap.put("forwardErrMsg", objectMap.get("msg").toString());
                    logMap.put("forwardResponseCode", JSONArray.toJSONString(objectMap.get("code")));
                } else {
                    logMap.put("status", "成功");
                    logMap.put("code", "200");
                    logMap.put("forwardResponseMsg", "成功");
                    logMap.put("forwardResponseCode", JSONArray.toJSONString(objectMap.get("code")));
                }
                logMap.put("forwardResponseData", JSONArray.toJSONString(objectMap.get("data")));
                map.putAll(objectMap);
            } else {
                Map<String, Object> resultMap = new HashMap<>();
                if (!authCheckUtil.checkAki(request, body, interfaceManage, interfaceApply, resultMap, restMap)) {
                    map.putAll(resultMap);
                    logMap.put("status", "请求失败");
                    logMap.put("errMsg", resultMap.get("msg").toString());
                    logMap.put("code", resultMap.get("status").toString());
                    logMap.put("authMsg", "失败");
                    return ResponseEntity.ok().body(map);
                }
                logMap.put("status", "成功");
                logMap.put("authMsg", "成功");
                logMap.put("forwardRequestParam", JSONArray.toJSONString(body));
                logMap.put("forwardStartTime", sdf.format(calendar.getTime()));
                if(interfaceManageDTO.getIsResponseFile()){
                    ResponseEntity<Resource> fileRes = null;
                    if ("post".equals(interfaceManageDTO.getInterfaceMethod())) {
                        postFile(interfaceManageDTO, headers, body,responseRt,logMap);
                    } else if ("get".equals(interfaceManageDTO.getInterfaceMethod())) {
                        getFile(interfaceManageDTO, headers, body,responseRt,logMap);
                    }
                }else {
                    if ("post".equals(interfaceManageDTO.getInterfaceMethod())) {
                        response = post(interfaceManageDTO, request, headers, body);
                    } else if ("get".equals(interfaceManageDTO.getInterfaceMethod())) {
                        response = get(interfaceManageDTO, headers, body);
                    }
                    if (response.getStatusCodeValue() == 200) {
                        logMap.put("status", "成功");
                        logMap.put("code", "200");
                        logMap.put("forwardResponseMsg", "成功");
                    } else {
                        logMap.put("status", "失败");
                        logMap.put("code", "502");
                        logMap.put("forwardResponseMsg", "失败");
                        logMap.put("forwardErrMsg", response.getBody());
                    }
                    logMap.put("forwardResponseCode", "" + response.getStatusCodeValue());
                    logMap.put("forwardResponseData", response.getBody());
                    Map<String, Object> dataMap = JSONArray.parseObject(response.getBody(), Map.class);
                    map.putAll(dataMap);
                    return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE,"application/json").body(map);
                }
            }
        } catch (Exception e) {
            map.put("msg", e.getMessage());
            logMap.put("status", "请求失败");
            logMap.put("errMsg", e.getMessage());
            logMap.put("code", "501");
        } finally {
            calendar.setTime(new Date());
            logMap.put("requestEndTime", sdf.format(calendar.getTime()));
            calllogUtil.createLog(logId, interfaceApply, interfaceManage, logMap, request);
        }
        return ResponseEntity.ok().body(map);
    }

    @Override
    public ResponseEntity testForward(InterfaceManageDTO interfaceManageDTO, HttpServletRequest request,HttpServletResponse responseRt) {
        ResponseEntity res = null;
        Calendar calendar = Calendar.getInstance();
        String logId = Y9IdGenerator.genId(IdType.SNOWFLAKE);
        HashMap<String, String> logMap = new HashMap<>();
        calendar.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        logMap.put("requestStartTime", sdf.format(calendar.getTime()));
        Map<String, Object> map = new HashMap<>();
        InterfaceManage interfaceManage = new InterfaceManage(interfaceManageDTO);
        InterfaceApply interfaceApply = new InterfaceApply();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        Map<String, Object> body = new HashMap<>();
        try {
            ResponseEntity<String> response = null;
            List<Parameter> reqList = JSONArray.parseArray(interfaceManageDTO.getReqParameters(), Parameter.class);
            List<Parameter> headerList = JSONArray.parseArray(interfaceManageDTO.getParameters(), Parameter.class);
            for (Parameter parameter : reqList) {
                switch (parameter.getParameterType()) {
                    case "String":
                        body.put(parameter.getParameterKey(), parameter.getVal());
                        break;
                    case "integer":
                        body.put(parameter.getParameterKey(), Integer.valueOf(parameter.getVal()));
                        break;
                    case "double":
                        body.put(parameter.getParameterKey(), Double.parseDouble(parameter.getVal()));
                        break;
                    case "boolean":
                        body.put(parameter.getParameterKey(), Boolean.valueOf(parameter.getVal()));
                        break;
                    default:
                        body.put(parameter.getParameterKey(), parameter.getVal());
                }
            }
            logMap.put("requestParam", JSONArray.toJSONString(body));
            HttpHeaders headers = new HttpHeaders();
            Map<String, String> webHeaders = new HashMap<>();
            for (Parameter parameter : headerList) {
                headers.set(parameter.getParameterKey(), parameter.getVal());
                webHeaders.put(parameter.getParameterKey(), parameter.getVal());
            }

            if (!"Rest".equals(interfaceManageDTO.getInterfaceType())) {
                Map<String, Object> resultMap = new HashMap<>();
                logMap.put("status", "成功");
                logMap.put("authMsg", "成功");
                List<Object> params = getParams(interfaceManageDTO, reqList, body);
                logMap.put("forwardRequestParam", JSONArray.toJSONString(params));
                calendar.setTime(new Date());
                logMap.put("forwardStartTime", String.valueOf(calendar.getTimeInMillis()));
                Map<String, Object> objectMap = webServiceForwardUtil.forward(interfaceManage, params, webHeaders);
                if (!(boolean) objectMap.get("status")) {
                    logMap.put("status", "请求失败");
                    logMap.put("errMsg", objectMap.get("msg") != null ? objectMap.get("msg").toString() : "对方系统请求失败");
                    logMap.put("code", "402");
                    logMap.put("forwardResponseMsg", "失败");
                    logMap.put("forwardErrMsg", objectMap.get("msg").toString());
                    logMap.put("forwardResponseCode", JSONArray.toJSONString(objectMap.get("code")));
                } else {
                    logMap.put("status", "成功");
                    logMap.put("code", "200");
                    logMap.put("forwardResponseMsg", "成功");
                    logMap.put("forwardResponseCode", JSONArray.toJSONString(objectMap.get("code")));
                }
                logMap.put("forwardResponseData", JSONArray.toJSONString(objectMap.get("data")));
                map.putAll(objectMap);
            } else {

                logMap.put("status", "成功");
                logMap.put("authMsg", "成功");
                logMap.put("forwardRequestParam", JSONArray.toJSONString(body));
                logMap.put("forwardStartTime", sdf.format(calendar.getTime()));
                if(interfaceManageDTO.getIsResponseFile()){
                    if ("post".equals(interfaceManageDTO.getInterfaceMethod())) {
                        postFile(interfaceManageDTO, headers, body,responseRt,logMap);
                    } else if ("get".equals(interfaceManageDTO.getInterfaceMethod())) {
                        getFile(interfaceManageDTO, headers, body,responseRt,logMap);
                    }
                }else {
                    if ("post".equals(interfaceManageDTO.getInterfaceMethod())) {
                        response = post(interfaceManageDTO, request, headers, body);
                    } else if ("get".equals(interfaceManageDTO.getInterfaceMethod())) {
                        response = get(interfaceManageDTO, headers, body);
                    }
                    if (response.getStatusCodeValue() == 200) {
                        logMap.put("status", "成功");
                        logMap.put("code", "200");
                        logMap.put("forwardResponseMsg", "成功");
                    } else {
                        logMap.put("status", "失败");
                        logMap.put("code", "502");
                        logMap.put("forwardResponseMsg", "失败");
                        logMap.put("forwardErrMsg", response.getBody());
                    }
                    logMap.put("forwardResponseCode", "" + response.getStatusCodeValue());
                    logMap.put("forwardResponseData", response.getBody());
                    try {
                        map.put("data", JSONArray.parseArray(response.getBody(),Map.class));
                    }catch (Exception e){
                        map.put("data", response.getBody());
                    }
                    map.put("resCode", response.getStatusCodeValue());
                }
            }
            if(!interfaceManageDTO.getIsResponseFile()){
                map.put("code","0");
                res = ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE,"application/json").body(map);
            }
        } catch (Exception e) {
            map.put("msg", e.getMessage());
            map.put("code", "501");
            logMap.put("status", "请求失败");
            logMap.put("errMsg", e.getMessage());
            logMap.put("code", "501");
            res = ResponseEntity.status(501).body(map);
        } finally {
            calendar.setTime(new Date());
            logMap.put("requestEndTime", sdf.format(calendar.getTime()));
            logMap.put("userId", person.getPersonId());
            logMap.put("userName", person.getName());
            calllogUtil.createLog(logId, interfaceApply, interfaceManage, logMap, request);
        }
        return res;
    }

    //get请求方法
    private ResponseEntity<String> get(InterfaceManageDTO interfaceManageDTO, HttpHeaders headers, Map<String, Object> body) {
        RestTemplate restTemplate = restUtil.restTemplate(5000, 300000);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(headers);
        String url = interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl();
        if (body.size() != 0) {
            url += "?";
        }
        for (String key : body.keySet()) {
            String value = body.get(key).toString();
            url += key + "=" + value + "&";
        }
        if (body.size() != 0) {
            url = url.substring(0, url.length() - 1);
        }
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response;
    }

    //post请求方法
    private ResponseEntity<String> post(InterfaceManageDTO interfaceManageDTO, HttpServletRequest request, HttpHeaders headers, Map<String, Object> requestBody) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (String key : requestBody.keySet()) {
            body.set(key, requestBody.get(key));
        }
        RestTemplate restTemplate = restUtil.restTemplate(5000, 300000);
        HttpEntity entity;
        if ("application/json".equals((headers.get("content-type") != null ? headers.get("content-type").get(0).toLowerCase() : ""))) {
            entity = new HttpEntity<>(requestBody, headers);
        } else {
            entity = new HttpEntity<>(body, headers);
        }
        ResponseEntity<String> response = restTemplate.postForEntity(interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl(), entity, String.class);
        return response;
    }

    //get请求方法
    private ResponseEntity<Resource> getFile(InterfaceManageDTO interfaceManageDTO, HttpHeaders headers, Map<String, Object> body) {
        RestTemplate restTemplate = restUtil.restTemplate(5000, 300000);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(headers);
        String url = interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl();
        if (body.size() != 0) {
            url += "?";
        }
        for (String key : body.keySet()) {
            String value = body.get(key).toString();
            url += key + "=" + value + "&";
        }
        if (body.size() != 0) {
            url = url.substring(0, url.length() - 1);
        }
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        return response;
    }

    //get请求方法
    private void getFile(InterfaceManageDTO interfaceManageDTO, HttpHeaders headers, Map<String, Object> body,HttpServletResponse responseRt,HashMap<String,String> logMap) {
        RestTemplate restTemplate = restUtil.restTemplate(5000, 300000);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(headers);
        String url = interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl();
        if (body.size() != 0) {
            url += "?";
        }
        for (String key : body.keySet()) {
            String value = body.get(key).toString();
            url += key + "=" + value + "&";
        }
        if (body.size() != 0) {
            url = url.substring(0, url.length() - 1);
        }
        responseRt.setHeader("Access-Control-Expose-Headers","content-disposition");
        RequestCallback requestCallback = restTemplate.httpEntityCallback(entity);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            restTemplate.execute(new URI(url), HttpMethod.GET, requestCallback, new ResponseExtractor<Void>() {
                @Override
                public Void extractData(ClientHttpResponse response) throws IOException {
                    if (response.getStatusCode().value() == 200) {
                        logMap.put("status", "成功");
                        logMap.put("code", "200");
                        logMap.put("forwardResponseMsg", "成功");
                    } else {
                        logMap.put("status", "失败");
                        logMap.put("code", "502");
                        logMap.put("forwardResponseMsg", "失败");
                        logMap.put("forwardErrMsg", "传输为文件");
                    }
                    logMap.put("forwardResponseCode", "" + response.getStatusCode().value());
                    logMap.put("forwardResponseData", "传输为文件");

                    responseRt.setHeader("content-disposition",response.getHeaders().get("content-disposition").get(0));
                    try (InputStream inputStream = response.getBody()) {
                        IOUtils.copy(inputStream,responseRt.getOutputStream());
                        responseRt.getOutputStream().flush();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    //post请求方法
    private ResponseEntity<Resource> postFile(InterfaceManageDTO interfaceManageDTO, HttpServletRequest request, HttpHeaders headers, Map<String, Object> requestBody) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (String key : requestBody.keySet()) {
            body.set(key, requestBody.get(key));
        }
        RestTemplate restTemplate = restUtil.restTemplate(5000, 300000);
        HttpEntity entity;
        if ("application/json".equals((headers.get("content-type") != null ? headers.get("content-type").get(0).toLowerCase() : ""))) {
            entity = new HttpEntity<>(requestBody, headers);
        } else {
            entity = new HttpEntity<>(body, headers);
        }
        ResponseEntity<Resource> response = restTemplate.postForEntity(interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl(), entity, Resource.class);
        return response;
    }

    private void postFile(InterfaceManageDTO interfaceManageDTO,HttpHeaders headers, Map<String, Object> requestBody,HttpServletResponse responseRt,HashMap<String,String> logMap) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (String key : requestBody.keySet()) {
            body.set(key, requestBody.get(key));
        }
        RestTemplate restTemplate = restUtil.restTemplate(5000, 300000);
        HttpEntity entity;
        if ("application/json".equals((headers.get("content-type") != null ? headers.get("content-type").get(0).toLowerCase() : ""))) {
            entity = new HttpEntity<>(requestBody, headers);
        } else {
            entity = new HttpEntity<>(body, headers);
        }
        //ResponseEntity<Resource> response = restTemplate.postForEntity(interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl(), entity, Resource.class);
        responseRt.setHeader("Access-Control-Expose-Headers","content-disposition");
        RequestCallback requestCallback = restTemplate.httpEntityCallback(entity);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String url = interfaceManageDTO.getNetworkAgreement() + "://" + interfaceManageDTO.getInterfaceUrl();
            restTemplate.execute(new URI(url), HttpMethod.POST, requestCallback, new ResponseExtractor<Void>() {
                @Override
                public Void extractData(ClientHttpResponse response) throws IOException {
                    if (response.getStatusCode().value() == 200) {
                        logMap.put("status", "成功");
                        logMap.put("code", "200");
                        logMap.put("forwardResponseMsg", "成功");
                    } else {
                        logMap.put("status", "失败");
                        logMap.put("code", "502");
                        logMap.put("forwardResponseMsg", "失败");
                        logMap.put("forwardErrMsg", "传输为文件");
                    }
                    logMap.put("forwardResponseCode", "" + response.getStatusCode().value());
                    logMap.put("forwardResponseData", "传输为文件");

                    responseRt.setHeader("content-disposition",response.getHeaders().get("content-disposition").get(0));
                    try (InputStream inputStream = response.getBody()) {
                        IOUtils.copy(inputStream,responseRt.getOutputStream());
                        responseRt.getOutputStream().flush();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    //获取头参数
    private HttpHeaders getHeaders(HttpServletRequest request, List<Parameter> list, List<String> errList) {
        HttpHeaders headers = new HttpHeaders();
        for (Parameter parameter : list) {
            if ("1".equals(parameter.getParameterStatus())) {
                String val = authCheckUtil.checkSQLInject(authCheckUtil.checkXSS(request.getHeader(parameter.getParameterKey().toLowerCase()), parameter.getParameterKey()), parameter.getParameterKey());
                if ("是".equals(parameter.getRequired())) {
                    if (StringUtils.isBlank(val)) {
                        errList.add("请求头参数：" + parameter.getParameterKey() + "是必填项，无值请确认！");
                        continue;
                    }
                    headers.set(parameter.getParameterKey().toLowerCase(), StringUtils.isNotBlank(val) ? val : parameter.getDefaultVal());
                } else {
                    if (StringUtils.isNotBlank(val)) {
                        headers.set(parameter.getParameterKey().toLowerCase(), StringUtils.isNotBlank(val) ? val : parameter.getDefaultVal());
                    }
                }
            }
        }
        return headers;
    }

    //获取webservice参数
    private Map<String, String> getHeadersRtMap(HttpServletRequest request, List<Parameter> list) {
        Map<String, String> headers = new HashMap<>();
        for (Parameter parameter : list) {
            if ("1".equals(parameter.getParameterStatus())) {
                String val = authCheckUtil.checkSQLInject(authCheckUtil.checkXSS(request.getHeader(parameter.getParameterKey()), parameter.getParameterKey()), parameter.getParameterKey());
                if ("是".equals(parameter.getRequired())) {
                    headers.put(parameter.getParameterKey(), StringUtils.isNotBlank(val) ? val : parameter.getDefaultVal());
                } else {
                    if (StringUtils.isNotBlank(val)) {
                        headers.put(parameter.getParameterKey(), StringUtils.isNotBlank(val) ? val : parameter.getDefaultVal());
                    }
                }
            }
        }
        return headers;
    }

    //解析请求参数并校验
    private Map<String, Object> getRestParams(InterfaceManageDTO interfaceManageDTO, List<Parameter> list, Map<String, Object> body, List<String> errList) {
        List<Parameter> reqParameters = new ArrayList<>();
        for (Parameter parameter : list) {
            if ("2".equals(parameter.getParameterStatus())) {
                reqParameters.add(parameter);
            }
        }
        List<ParameterDTO> dtos = integraTreeData(reqParameters);//带子级节点信息
        Map<String, Object> map = new HashMap<>();
        for (ParameterDTO parameter : dtos) {
            Object obj = body.get(parameter.getParameterKey());
            String val = obj != null ? obj.toString() : null;
            if ("是".equals(parameter.getRequired())) {
                if (StringUtils.isBlank(val)) {
                    errList.add("参数：" + parameter.getParameterKey() + "是必填项，无值请确认！");
                    continue;
                }
                if ("get".equals(interfaceManageDTO.getInterfaceMethod()) || !"Rest".equals(interfaceManageDTO.getInterfaceType())) {
                    //开始校验参数值
                    authCheckUtil.checkData(val, parameter.getParameterKey(), parameter.getParameterType(), errList);
                    map.put(parameter.getParameterKey(), val);
                    //结束校验参数值
                } else {
                    if ("map".equals(parameter.getParameterType())) {
                        Map childData = JSONArray.parseObject(val, Map.class);
                        dfsData(childData, parameter.getChildren(), parameter, errList, map);
                    } else if ("array".equals(parameter.getParameterType())) {
                        List childData = JSONArray.parseObject(val, List.class);
                        dfsData(childData, parameter.getChildren(), parameter, errList, map);
                    } else {
                        //开始校验参数值
                        authCheckUtil.checkData(val, parameter.getParameterKey(), parameter.getParameterType(), errList);
                        map.put(parameter.getParameterKey(), val);
                        //结束校验参数值
                    }
                }
            } else {
                if ("get".equals(interfaceManageDTO.getInterfaceMethod()) || !"Rest".equals(interfaceManageDTO.getInterfaceType())) {
                    //开始校验参数值
                    authCheckUtil.checkData(val, parameter.getParameterKey(), parameter.getParameterType(), errList);
                    map.put(parameter.getParameterKey(), val);
                    //结束校验参数值
                } else {
                    if ("map".equals(parameter.getParameterType())) {
                        Map childData = JSONArray.parseObject(val, Map.class);
                        dfsData(childData, parameter.getChildren(), parameter, errList, map);
                    } else if ("array".equals(parameter.getParameterType())) {
                        List childData = JSONArray.parseObject(val, List.class);
                        dfsData(childData, parameter.getChildren(), parameter, errList, map);
                    } else {
                        //开始校验参数值
                        authCheckUtil.checkData(val, parameter.getParameterKey(), parameter.getParameterType(), errList);
                        map.put(parameter.getParameterKey(), val);
                        //结束校验参数值
                    }
                }
            }

        }
        return map;
    }

    //获取参数
    private List<Object> getParams(InterfaceManageDTO interfaceManageDTO, List<Parameter> list, Map<String, Object> body) {
        List<Object> rtList = new ArrayList<>();
        List<Parameter> reqParameters = new ArrayList<>();
        for (Parameter parameter : list) {
            if ("2".equals(parameter.getParameterStatus())) {
                reqParameters.add(parameter);
            }
        }
        List<ParameterDTO> dtos = integraTreeData(reqParameters);//带子级节点信息
        Map<String, String> map = new HashMap<>();
        for (ParameterDTO parameter : dtos) {
            Object obj = body.get(parameter.getParameterKey());
            String val = obj != null ? obj.toString() : null;
            if (StringUtils.isNotBlank(val)) {
                if ("JAX-WS".equals(interfaceManageDTO.getWebSpecification())) {
                    switch (parameter.getParameterType()) {
                        case "String":
                            rtList.add(val.toString());
                            break;
                        case "integer":
                            rtList.add(Integer.valueOf(val));
                            break;
                        case "double":
                            rtList.add(Double.parseDouble(val.toString()));
                            break;
                        case "boolean":
                            rtList.add(Boolean.valueOf(val.toString()));
                            break;
                        default:
                            rtList.add(val.toString());
                    }
                } else {
                    Map<String, String> itData = new HashMap<>();
                    itData.put(parameter.getParameterKey(), val);
                    rtList.add(itData);
                }
            }
        }
        return rtList;
    }

    //递归组装树形数据
    private void assembleData(ParameterDTO node, Map<String, List<Parameter>> pidMaps) {
        if (pidMaps.get(node.getId()) != null) {
            List<ParameterDTO> list = new ArrayList<>();
            if (pidMaps.get(node.getId()).size() != 0) {
                for (Parameter parameter : pidMaps.get(node.getId())) {
                    ParameterDTO parameterDTO = new ParameterDTO(parameter);
                    assembleData(parameterDTO, pidMaps);
                    list.add(parameterDTO);
                }
            }
            node.setChildren(list);
        }
    }

    //外层组装树形数据
    private List<ParameterDTO> integraTreeData(List<Parameter> lists) {
        List<ParameterDTO> list = new ArrayList<>();
        Map<String, List<Parameter>> pidMaps = new HashMap<>();
        for (Parameter parameter : lists) {
            if ("0".equals(parameter.getPid())) {
                ParameterDTO parameterDTO = new ParameterDTO(parameter);
                list.add(parameterDTO);
            } else {
                if (pidMaps.get(parameter.getPid()) != null) {
                    pidMaps.get(parameter.getPid()).add(parameter);
                } else {
                    List<Parameter> parameters = new ArrayList<>();
                    parameters.add(parameter);
                    pidMaps.put(parameter.getPid(), parameters);
                }
            }
        }
        for (ParameterDTO dto : list) {
            assembleData(dto, pidMaps);
        }
        return list;
    }

    //递归遍历数据校验obj:数据，child:层级关系，curtNode当前层级数据结构，errList错误信息汇总，dataMap数据转换为一层结构
    private void dfsData(Object obj, List<ParameterDTO> child, ParameterDTO curtNode, List<String> errList, Map<String, Object> dataMap) {
        if ("map".equals(curtNode.getParameterType())) {
            Map map = (HashMap) obj;
            for (ParameterDTO dto : child) {
                Object val;
                if (map == null) {
                    val = null;
                } else {
                    val = map.get(dto.getParameterKey());
                }
                if ("是".equals(dto.getRequired()) && (val == null || StringUtils.isBlank(val.toString()))) {
                    errList.add("参数:" + dto.getParameterKey() + "是必填项没有值");
                    continue;
                }
                if ("map".equals(dto.getParameterType())) {
                    dfsData(val, dto.getChildren(), dto, errList, dataMap);
                } else if ("array".equals(dto.getParameterType())) {
                    dfsData(val, dto.getChildren(), dto, errList, dataMap);
                } else {
                    //存储值前判断为null转为""
                    if (val == null) {
                        val = "";
                    }
                    //开始校验参数值
                    authCheckUtil.checkData(val.toString(), dto.getParameterKey(), dto.getParameterType(), errList);
                    dataMap.put(dto.getParameterKey(), (dataMap.get(dto.getParameterKey()) == null ? "" : dataMap.get(dto.getParameterKey()) + regx) + val);
                    //结束校验参数值
                }
            }
        } else if ("array".equals(curtNode.getParameterType())) {
            List list = (List) obj;
            if ("是".equals(curtNode.getRequired()) && (list == null || list.size() == 0)) {
                errList.add("参数:" + curtNode.getParameterKey() + "是必填项没有值");
                return;
            }
            for (ParameterDTO dto : child) {
                String str = "";
                if (list == null) {
                    if ("map".equals(dto.getChildren().get(0).getParameterType())) {
                        dfsData(null, dto.getChildren().get(0).getChildren(), dto, errList, dataMap);
                    } else if ("array".equals(dto.getChildren().get(0).getParameterType())) {
                        dfsData(null, dto.getChildren().get(0).getChildren(), dto, errList, dataMap);
                    } else {
                        //开始校验参数值
                        authCheckUtil.checkData("", curtNode.getParameterKey(), dto.getParameterType(), errList);
                        //结束校验参数值
                    }
                } else {
                    for (Object it : list) {
                        if ("map".equals(dto.getChildren().get(0).getParameterType())) {
                            dfsData(it, dto.getChildren().get(0).getChildren(), dto, errList, dataMap);
                        } else if ("array".equals(dto.getChildren().get(0).getParameterType())) {
                            dfsData(it, dto.getChildren().get(0).getChildren(), dto, errList, dataMap);
                        } else {
                            //开始校验参数值
                            authCheckUtil.checkData(it.toString(), curtNode.getParameterKey(), dto.getParameterType(), errList);
                            str += it.toString() + regx;
                            //结束校验参数值
                        }
                    }
                }
                str = str.substring(0, str.length() - 1);
                dataMap.put(curtNode.getParameterKey(), (dataMap.get(curtNode.getParameterKey()) == null ? "" : dataMap.get(curtNode.getParameterKey()) + regx) + str);
            }
        } else {
            if (obj == null) {
                obj = "";
            }
            if ("是".equals(curtNode.getRequired()) && (obj == null || StringUtils.isBlank(obj.toString()))) {
                errList.add("参数:" + curtNode.getParameterKey() + "是必填项没有值");
            }
            //开始校验参数值
            authCheckUtil.checkData(obj.toString(), curtNode.getParameterKey(), curtNode.getParameterType(), errList);
            dataMap.put(curtNode.getParameterKey(), (dataMap.get(curtNode.getParameterKey()) == null ? "" : dataMap.get(curtNode.getParameterKey()) + regx) + obj.toString());
            //结束校验参数值
        }
    }

    //application/json读取数据
    private Map readBody(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String inputStr;
            String key = "";
            Boolean isVal = false;
            while ((inputStr = streamReader.readLine()) != null) {
                if (StringUtils.isNotBlank(inputStr) && isVal) {
                    isVal = false;
                    map.put(key, inputStr);
                }
                if (inputStr.indexOf("form-data; name=\"") != -1) {
                    key = inputStr.substring(inputStr.indexOf("form-data; name=\"") + 17, inputStr.length() - 1);
                    isVal = true;
                }
                response.append(inputStr);
            }
            map = JSONArray.parseObject(response.toString(), Map.class);
            return map;
        } catch (Exception e) {
            return map;
        }
    }
}

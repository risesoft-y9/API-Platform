package net.risesoft.util;

import com.alibaba.fastjson.JSONArray;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.util.InetAddressUtil;
import net.risesoft.y9public.entity.CallApiRequestLogInfo;
import net.risesoft.y9public.entity.InterfaceApply;
import net.risesoft.y9public.entity.InterfaceManage;
import net.risesoft.y9public.repository.CallApiLogRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class CallLogUtil {

    @Autowired
    private Environment environment;
    @Autowired
    private CallApiLogRepository callApiLogRepository;

    private KafkaTemplate<String, Object> y9KafkaTemplate;

    //finally进入的日志记录
    public void createLog(String logId, InterfaceApply apply, InterfaceManage entity, Map<String,String> msgData,HttpServletRequest request){
        try {
            if (this.y9KafkaTemplate == null && Boolean.valueOf(environment.getProperty("y9.app.log.kafkaEnabled"))) {
                this.y9KafkaTemplate = Y9Context.getBean("y9KafkaTemplate");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String serverIp = InetAddressUtil.getLocalAddress().getHostAddress();
        CallApiRequestLogInfo log = new CallApiRequestLogInfo();
        log.setId(logId);
        if (msgData.get("requestStartTime") != null) {
            try {
                log.setRequestStartTime(sdf.parse(msgData.get("requestStartTime")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String success = "成功";
        String hostIp = "";
        try {
            hostIp = Y9Context.getIpAddr(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.setRequestIP(hostIp);
        log.setServerIP(serverIp);
        log.setUserKey(request.getHeader("userKey"));
        log.setRequestUserId(request.getHeader("userId"));
        log.setRequestUserName(request.getHeader("userName"));
        if (msgData.get("userName") != null) {
            log.setRequestUserName(msgData.get("userName"));
        }
        if (msgData.get("userId") != null) {
            log.setRequestUserId(msgData.get("userId"));
        }
        log.setInterfaceType(request.getMethod());
        //获取请求参数
        Map<String, Object> body = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        if (!parameterNames.hasMoreElements()) {
            body = readBody(request);
        }
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);
            body.put(key, value);
        }
        if (body!=null && body.size()!=0) {
            log.setRequestParam(msgData.get("requestParam"));
        } else {
            log.setRequestParam(JSONArray.toJSONString(body));
        }


        if (apply != null) {
            log.setApplySystemName(apply.getApplySystemName());
            log.setApplySystemNameId(apply.getSystemIdentifier());
            log.setUserName(apply.getApplyPersonName());
            log.setDeptName(apply.getApplyPersonDeptName());
        }

        if (msgData.get("forwardStartTime") != null) {
            try {
                log.setForwardStartTime(sdf.parse(msgData.get("forwardStartTime")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //如果报错就进行记录
        if (!success.equals(msgData.get("status"))) {
            log.setErrorMessage(msgData.get("errMsg"));
            log.setResponseMsg("失败");
            log.setResponseCode(msgData.get("code"));
            if (msgData.get("requestEndTime") != null) {
                try {
                    log.setRequestEndTime(sdf.parse(msgData.get("requestEndTime")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log.setResponseMsg("成功");
            log.setResponseCode("200");
            if (msgData.get("requestEndTime") != null) {
                try {
                    log.setRequestEndTime(sdf.parse(msgData.get("requestEndTime")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        if (entity != null) {
            log.setApiName(entity.getInterfaceName());
            log.setInterfaceId(entity.getId());
            if ("是".equals(entity.getIsAuth())) {
                log.setAuthentic(1);
                log.setAuthenticationResult(msgData.get("authMsg"));
            }else {
                log.setAuthentic(0);
            }
            if ("是".equals(entity.getIsLimit())) {
                log.setIsLimit(1);
                log.setLimitResult(msgData.get("limitMsg"));
                log.setLimitTime(msgData.get("limitWaitTime"));
            }else {
                log.setIsLimit(0);
            }
        }

        log.setForwardRequestParam(msgData.get("forwardRequestParam"));
        log.setForwardResponseCode(msgData.get("forwardResponseCode"));
        log.setForwardResponseData(msgData.get("forwardResponseData"));
        log.setForwardErrMsg(msgData.get("forwardErrMsg"));
        log.setForwardResponseMsg(msgData.get("forwardResponseMsg"));
        if (this.y9KafkaTemplate == null && Boolean.valueOf(environment.getProperty("y9.app.log.kafkaEnabled"))) {
            y9KafkaTemplate.send("interface-management",JSONArray.toJSONString(log));
        }else {
            callApiLogRepository.save(log);
        }
    }

    //application/json读取数据
    private Map<String, Object> readBody(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String inputStr;
            String key = "";
            boolean isVal = false;
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

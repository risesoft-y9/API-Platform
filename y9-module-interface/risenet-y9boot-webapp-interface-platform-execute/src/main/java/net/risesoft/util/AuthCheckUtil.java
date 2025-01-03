package net.risesoft.util;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.alibaba.fastjson.JSONArray;
import net.risesoft.y9public.entity.InterfaceApply;
import net.risesoft.y9public.entity.InterfaceManage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static net.risesoft.service.impl.UseInterfaceServiceImpl.regx;

@Component
public class AuthCheckUtil {

    //sql注入正则
    private static final String badStrReg = "\\b(and|or)\\b.{1,6}?(=|>|<|\\bin\\b|\\blike\\b)|\\/\\*.+?\\*\\/|<\\s*script\\b|" +
            "\\bEXEC\\b|UNION.+?SELECT|UPDATE.+?SET|INSERT\\s+INFO.+?VALUES|(SELECT|DELETE).+?FROM|(CREATE|ALTER|DROP|TRUNCATE)\\s+(TABLE|DATABASE)";

    //xss脚本正则
    private final static Pattern[] scriptPatterns = {
            Pattern.compile("<script>(.*?)</script>",Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>",Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:",Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:",Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    public static String getIp(HttpServletRequest request){
        String ip=request.getHeader("X-Real-IP");
        if(ip == null || ip.length()==0|| "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("X-Forwarded-For");
        }
        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-Ip");
        }
        if(ip ==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
            ip =request.getHeader("WL-Proxy-Client-Ip");
        }
        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    /**
     * 过滤sql注入
     * @param src 单个参数值
     */
    public String checkSQLInject(String src,String key){
        if (StringUtils.isBlank(src)){
            return src;
        }

        //非法sql注入正则
        Pattern sqlPattern = Pattern.compile(badStrReg,Pattern.CASE_INSENSITIVE);
        if (sqlPattern.matcher(src.toLowerCase()).find()){
            throw new BizException("sql注入检查，参数"+key+"含有非法攻击字符，请检查");
        }
        return src;
    }

    /**
     * 清除xss
     * @param src 单个参数值
     */
    public String checkXSS(String src,String key){
        if (StringUtils.isBlank(src)){
            return src;
        }
        String temp = src;
        for (Pattern pattern : scriptPatterns){
            temp = pattern.matcher(temp).replaceAll("");
        }
        temp = temp.replaceAll("<","<").replaceAll(">",">");
        if (!temp.equals(src)){
            throw new BizException("xss攻击检查，参数"+key+"含有非法攻击字符");
        }
        return src;
    }

    /**
     * 参数类型校验
     * @param parameter 参数值
     * @param type 参数类型
     */
    private Boolean checkParameterType(String parameter,String type){
        switch (type){
            case "String": return true;
            case "integer":
                String regx = "\\d+";
                if (Pattern.matches(regx,parameter)){
                    try {
                        int num = Integer.parseInt(parameter);
                        return true;
                    }catch (NumberFormatException e){
                        return false;
                    }
                }else {
                    return false;
                }
            case "boolean":
                return "true".equalsIgnoreCase(parameter) || "false".equalsIgnoreCase(parameter);
            case "number":
                return StringUtils.isNumeric(parameter);
            default:return false;
        }
    }

    //数据基础校验
    public Boolean checkData(String val, String key,String type,List<String> errList){
        int len = errList.size();
        try {
            //检查xss攻击和sql注入
            checkXSS(checkSQLInject(val,key),key);
            //类型校验
            if (StringUtils.isNotBlank(val)){
                if (!checkParameterType(val,type)){
                    errList.add("参数类型检查，参数："+key+"数据类型不正确，不是"+type);
                }
            }
        }catch (BizException e){
            errList.add(e.getMessage());
        }
        return errList.size() != len;
    }

    //权限校验
    public Boolean checkAki(HttpServletRequest request, Map<String,Object> querymap,InterfaceManage interfaceManage,InterfaceApply apply, Map<String, Object> resultmap,Map<String,Object> valMap) {

        String accessKey = request.getHeader("userKey");

        if(apply==null ) {
            resultmap.put("status", 401);
            resultmap.put("msg", "无权限访问该接口,非法调用者或您的唯一标识不正确");
            return false;
        }
        List<String> ipList = Arrays.asList(apply.getIpWhitelist().split(","));
        if (!ipList.contains(getIp(request))) {
            resultmap.put("status", 401);
            resultmap.put("msg", "无权限访问该接口,未开通此ip");
            return false;
        }

        //检测参数范围是否正确
        if("是".equals(interfaceManage.getIsLimitData())){
            List<Boolean> booleanList = new ArrayList<>();
            if (StringUtils.isNotBlank(apply.getAuth())){
                Map<String,String> authMap = JSONArray.parseObject(apply.getAuth(),Map.class);
                for (String key : authMap.keySet()){
                    booleanList.add(checkParameter(valMap.get(key),authMap.get(key),regx));
                }
            }
            if (booleanList.contains(false)) {
                resultmap.put("status", 401);
                resultmap.put("msg", "无权限访问该接口,参数超出范围");
                return false;
            }
        }

        if ("是".equals(interfaceManage.getIsAuth())){
            if (querymap.get("userGetSign")!=null){
                String userGetSign = querymap.get("userGetSign").toString();
                querymap.remove("userGetSign");

                String sign2 = getSign(querymap, apply.getUserSecret());

                if(!sign2.equals(userGetSign)) {
                    resultmap.put("status", 401);
                    resultmap.put("msg", "无权限访问该接口,秘钥错误");
                    return false;
                }
            }else{
                resultmap.put("status", 401);
                resultmap.put("msg", "无权限访问该接口,未带权限校验值");
                return false;
            }
        }

        return true;
    }

    /**
     * 校验权限范围值
     */
    private Boolean checkParameter(Object obj,String parameterRange,String regx){
        if (obj==null){
            return true;
        }
        String parameter = obj.toString();
        if (StringUtils.isBlank(parameter)&&StringUtils.isBlank(parameterRange)){
            return true;
        }
        if(StringUtils.isBlank(parameterRange)){
            return false;
        }
        if(StringUtils.isBlank(parameter)){
            return true;
        }
        String[] parameters = parameter.split(regx);
        String val = ","+parameterRange+",";
        for (String str : parameters){
            if(StringUtils.isNotBlank(str)){
                if(val.indexOf(","+str+",")==-1){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取sign值
     * @param map
     * @param secretKry
     * @return
     */
    private static String getSign(Map<String, Object> map, String secretKry) {
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        String content = map.toString()+"."+secretKry;
        return md5.digestHex(content);
    }
}

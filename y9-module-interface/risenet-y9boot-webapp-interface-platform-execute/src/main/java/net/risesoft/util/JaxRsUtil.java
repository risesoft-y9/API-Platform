package net.risesoft.util;

import net.risesoft.y9public.entity.InterfaceManage;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JaxRsUtil {
    public Map<String,Object> JaxRsForward(InterfaceManage interfaceManage, List<Map<String,String>> list, Map<String,String> headers){
        Map<String,Object> map = new HashMap<>();
        try{
            Client client = ClientBuilder.newClient();
            String url = interfaceManage.getNetworkAgreement()+"://" + interfaceManage.getInterfaceUrl();
            WebTarget target;
            if (url.indexOf("/{")!=-1 && url.indexOf("}")!=-1){
                //替换restful风格url
                for (Map<String,String> map1 : list){
                    for(String key : map1.keySet()){
                        url = url.replace("{"+key+"}",map1.get(key));
                    }
                }
                target = client.target(url);
            }else {
                target = client.target(url);
                for (Map<String,String> map1 : list){
                    for(String key : map1.keySet()){
                        target = target.queryParam(key,map1.get(key));
                    }
                }
            }
            Invocation.Builder builder = target.request();
            if (headers.size()!=0){
                for (String key : headers.keySet()){
                    builder = builder.header(key,headers.get(key));
                }
            }
            if ("get".equals(interfaceManage.getInterfaceMethod())){
                Response response = builder.get();
                if (response.getStatus()!=200){
                    map.put("status",false);
                }else {
                    map.put("status",true);
                }
                map.put("code",response.getStatus());
                map.put("data",response.readEntity(String.class));
            }else if ("post".equals(interfaceManage.getInterfaceMethod())){
                Response response = builder.post(Entity.entity("param", MediaType.TEXT_PLAIN_TYPE));
                if (response.getStatus()!=200){
                    map.put("status",false);
                }else {
                    map.put("status",true);
                }
                map.put("code",response.getStatus());
                map.put("data",response.readEntity(String.class));
            }
        }catch (Exception e){
            map.put("status",false);
            map.put("msg",e.getMessage());
        }

        return map;
    }

}

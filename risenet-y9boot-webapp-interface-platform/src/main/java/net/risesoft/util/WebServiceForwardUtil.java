package net.risesoft.util;

import net.risesoft.y9public.entity.InterfaceManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WebServiceForwardUtil {
    @Autowired
    private JaxRsUtil jaxRsUtil;
    @Autowired
    private JaxWsUtil jaxWsUtil;

    public Map<String, Object> forward(InterfaceManage interfaceManage, List<Object> parameters, Map<String, String> headers) {
        Map<String, Object> map = new HashMap<>();
        if ("JAX-WS".equals(interfaceManage.getWebSpecification())) {
            map = jaxWsUtil.JaxWsForward(interfaceManage, parameters);
        }
        if ("JAX-RS".equals(interfaceManage.getWebSpecification())) {
            List<Map<String, String>> data = new ArrayList<>();
            for (Object obj : parameters) {
                if (obj instanceof Map) {
                    Map<String, String> map1 = (Map<String, String>) obj;
                    data.add(map1);
                }
            }
            map = jaxRsUtil.JaxRsForward(interfaceManage, data, headers);
        }
        return map;
    }
}

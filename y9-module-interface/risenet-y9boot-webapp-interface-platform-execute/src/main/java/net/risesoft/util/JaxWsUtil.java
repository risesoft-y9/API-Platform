package net.risesoft.util;

import net.risesoft.y9public.entity.InterfaceManage;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JaxWsUtil {
    public Map<String,Object> JaxWsForward(InterfaceManage interfaceManage, List<Object> list){
        Map<String,Object> map = new HashMap<>();
        try {
            JaxWsDynamicClientFactory clientFactory = JaxWsDynamicClientFactory.newInstance();
            Client client = clientFactory.createClient(interfaceManage.getNetworkAgreement()+"://"+interfaceManage.getInterfaceUrl());
            HTTPConduit conduit = (HTTPConduit)client.getConduit();
            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout(6000000);
            httpClientPolicy.setReceiveTimeout(6000000);
            conduit.setClient(httpClientPolicy);
            Object[] objects = new Object[0];
            if (StringUtils.isNotBlank(interfaceManage.getNameSpace())){
                QName operationName = new QName(interfaceManage.getNameSpace(),interfaceManage.getMethod());
                objects = getData(client,operationName,list);
            }else {
                QName operationName = new QName(interfaceManage.getMethod());
                objects = getData(client,operationName,list);
            }
            if (objects!=null){
                map.put("status",true);
                map.put("data",objects[0]);
            }else {
                map.put("status",false);
            }
        }catch (Exception e){
            map.put("status",false);
            map.put("msg","对方系统请求失败！");
            e.printStackTrace();
        }
        return map;
    }

    private Object[] getData(Client client,QName operationName,List<Object> list) throws Exception{
        switch (list.size()){
            case 1:
                return client.invoke(operationName,list.get(0));
            case 2:
                return client.invoke(operationName,list.get(0),list.get(1));
            case 3:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2));
            case 4:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3));
            case 5:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4));
            case 6:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4),list.get(5));
            case 7:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4),list.get(5),list.get(6));
            case 8:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4),list.get(5),list.get(6),list.get(7));
            case 9:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4),list.get(5),list.get(6),list.get(7),list.get(8));
            case 10:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9));
            case 11:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10));
            case 12:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11));
            case 13:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12));
            case 14:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13));
            case 15:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14));
            case 16:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15));
            case 17:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16));
            case 18:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17));
            case 19:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18));
            case 20:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19));
            case 21:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20));
            case 22:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21));
            case 23:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22));
            case 24:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23));
            case 25:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23),list.get(24));
            case 26:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23),list.get(24),list.get(25));
            case 27:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23),list.get(24),list.get(25),list.get(26));
            case 28:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23),list.get(24),list.get(25),list.get(26),list.get(27));
            case 29:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23),list.get(24),list.get(25),list.get(26),list.get(27),list.get(28));
            case 30:
                return client.invoke(operationName,list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)
                        ,list.get(5),list.get(6),list.get(7),list.get(8),list.get(9),list.get(10),list.get(11),list.get(12)
                        ,list.get(13),list.get(14),list.get(15),list.get(16),list.get(17),list.get(18),list.get(19),list.get(20)
                        ,list.get(21),list.get(22),list.get(23),list.get(24),list.get(25),list.get(26),list.get(27),list.get(28)
                        ,list.get(29));
            default:
                return null;
        }
    }
}

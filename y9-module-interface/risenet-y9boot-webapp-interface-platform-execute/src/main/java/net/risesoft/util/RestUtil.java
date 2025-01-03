package net.risesoft.util;

import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;



@Component
public class RestUtil {
    /**
     *
     * @param connectTimeout 连接服务器超时时间
     * @param readTimeout 读取数据超时时间
     * @return
     */
    public RestTemplate restTemplate(int connectTimeout,int readTimeout){
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // 设置连接管理器参数，例如连接超时、读取超时等
        factory.setHttpClient(HttpClients.custom()
                .setMaxConnTotal(20)        // 最大连接数
                .setMaxConnPerRoute(10)     // 每个路由的最大连接数
                .build());
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        factory.setConnectionRequestTimeout(3000);
        factory.setBufferRequestBody(false);
        return new RestTemplate(factory);
    }
}

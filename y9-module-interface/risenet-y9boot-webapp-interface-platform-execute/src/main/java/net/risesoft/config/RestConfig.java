package net.risesoft.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 采用连接池的配置方法
 */
@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate(httpRequestFactory());
    }
    @Bean
    public ClientHttpRequestFactory httpRequestFactory(){
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }
    @Bean
    public HttpClient httpClient(){
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(400);
        connectionManager.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)//返回数据的超时时间
                .setConnectTimeout(20000)//连接上服务器的超时时间
                .setConnectionRequestTimeout(1000)//从连接池中获取连接的超时时间
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }
}

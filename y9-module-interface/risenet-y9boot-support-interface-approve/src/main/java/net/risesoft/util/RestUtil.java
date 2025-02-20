package net.risesoft.util;

import net.risesoft.model.Effect;
import net.risesoft.model.ThresholdType;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9public.entity.InterfaceLimitInfo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;


@Component
public class RestUtil {
    /**
     *
     * @param connectTimeout 连接服务器超时时间
     * @param readTimeout 读取数据超时时间
     * @return
     */
    public RestTemplate restTemplate(int connectTimeout,int readTimeout){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return new RestTemplate(requestFactory);
    }
}

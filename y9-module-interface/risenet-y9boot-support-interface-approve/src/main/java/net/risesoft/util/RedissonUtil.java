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
import org.springframework.stereotype.Component;



import java.util.concurrent.TimeUnit;


@Component
public class RedissonUtil {
    private static RedissonClient redissonClient;
    static {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://"+ Y9Context.getProperty("spring.redis.host")+":"+Y9Context.getProperty("spring.redis.port"))
                .setPassword(Y9Context.getProperty("spring.redis.password"))
                .setDatabase(9);
        redissonClient = Redisson.create(config);
    }

    //返回redis中的限流器
    public RRateLimiter getRateLimiter(String key){
        return redissonClient.getRateLimiter(key);
    }
    //初始化一个限流器
    public void init(InterfaceLimitInfo interfaceLimitInfo,String personId){
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter("rateLimiter_"+interfaceLimitInfo.getInterfaceId());
        //生产令牌数
        Long count = 10l;
        //多长时间内，单位：秒
        Long time = 1l;

        if (ThresholdType.TYPE_ZDY.getEnName().equals(interfaceLimitInfo.getThresholdType())) {
            if (StringUtils.isNotBlank(interfaceLimitInfo.getLimitCount())){
                try {
                    count = Long.parseLong(interfaceLimitInfo.getLimitCount());
                    if (StringUtils.isNotBlank(interfaceLimitInfo.getLimitTime())){
                        time = Long.parseLong(interfaceLimitInfo.getLimitTime());
                    }
                }catch (Exception e){
                    count = 10l;
                    time = 1l;
                }
            }
            rRateLimiter.trySetRate(RateType.OVERALL,count,time, RateIntervalUnit.SECONDS);
        }else if (ThresholdType.QPS.getEnName().equals(interfaceLimitInfo.getThresholdType())){
            if (StringUtils.isNotBlank(interfaceLimitInfo.getThresholdVal())){
                try {
                    count = Long.parseLong(interfaceLimitInfo.getThresholdVal());
                    time=1l;
                }catch (Exception e){
                    count = 10l;
                    time = 1l;
                }
                rRateLimiter.trySetRate(RateType.OVERALL,count,time, RateIntervalUnit.SECONDS);
            }
        }
    }

    //调用方法判断当前请求是否允许通过(注意：这里约定调用此方法的应当是开启限流的接口)
    public boolean isPass(InterfaceLimitInfo interfaceLimitInfo,String personId){
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter("rateLimiter_"+interfaceLimitInfo.getInterfaceId());
        //判断限流器是否存在，不存在初始化限流器
        if (!rRateLimiter.isExists()){
            init(interfaceLimitInfo,personId);
            rRateLimiter = redissonClient.getRateLimiter("rateLimiter_"+interfaceLimitInfo.getInterfaceId());
        }

        //判断流控效果
        if (Effect.FAIL_FAST.getEnName().equals(interfaceLimitInfo.getEffect())){
            boolean effect = rRateLimiter.tryAcquire(1l);
            return effect;
        }else if (Effect.WAIT.getEnName().equals(interfaceLimitInfo.getEffect())){
            Long time = 100l;
            try {
                time = Long.parseLong(interfaceLimitInfo.getWaitTime());
            }catch (Exception e){
                time = 100l;
            }
            //等待时间为0，则一直等待直到获取到令牌
            if ("0".equals(interfaceLimitInfo.getWaitTime())){
                rRateLimiter.acquire(1l);
                return true;
            }
            //在指定时间是否可以取到令牌
            boolean effect = rRateLimiter.tryAcquire(1l,time, TimeUnit.MILLISECONDS);
            return effect;
        }
        return true;
    }
}

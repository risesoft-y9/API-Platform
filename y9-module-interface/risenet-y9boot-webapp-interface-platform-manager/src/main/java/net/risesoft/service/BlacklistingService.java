package net.risesoft.service;

import net.risesoft.y9public.entity.Blacklisting;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BlacklistingService {

    /**
     * 获取page数据
     * @param blacklisting
     * @return
     */
    Page<Blacklisting> getPage(Blacklisting blacklisting, int page, int limit);

    //删除信息
    Map<String,String> delInfo(String id);

    Map<String,Object> saveInfo(Blacklisting blacklisting);

    Blacklisting getInfoById(String id);

    Map<String,Object> updateEnable(Blacklisting blacklisting);

    List<Blacklisting> getListByInterfaceId(String interfaceId);
}

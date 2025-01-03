package net.risesoft.service;

import net.risesoft.y9public.entity.SystemIdentifier;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface SystemIdentifierService {

    /**
     * 获取page数据
     * @param systemIdentifier
     * @return
     */
    Page<SystemIdentifier> getSystemIdentifierPage(SystemIdentifier systemIdentifier, int page, int limit);

    /**
     * 获取对应数据类型数据
     * @param type
     * @return
     */
    List<SystemIdentifier> getSystemIdentifierList(String type);

    /**
     * 根据父级id获取数据列表
     * @param id
     * @return
     */
    List<SystemIdentifier> getSystemIdentifierListByPid(String id);

    //删除信息
    Map<String,String> delInfo(String id);

    Map<String,Object> saveInfo(SystemIdentifier systemIdentifier);

    SystemIdentifier getInfoById(String id);
}

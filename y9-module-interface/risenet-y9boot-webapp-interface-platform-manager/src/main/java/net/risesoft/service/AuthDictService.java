package net.risesoft.service;

import net.risesoft.y9public.dto.AuthDictDTO;
import net.risesoft.y9public.entity.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface AuthDictService {

    /**
     * 获取page数据
     * @param authDict
     * @return
     */
    Page<AuthDict> getAuthDictList(AuthDict authDict,int page ,int limit);

    /**
     * 获取树形构造数据
     * @param authDict
     * @return
     */
    List<AuthDictDTO> getAuthDictTreeList(AuthDict authDict);

    //删除信息
    Map<String,String> delInfo(String id);

    //删除信息个人
    Map<String,String> delInfoPerson(String id);

    //根据参数id查询服务器信息
    AuthDict getAuthDictInfoById(String id);

    Map<String,Object> saveAuthDictInfo(AuthDict authDict);

    /**
     * 获取page数据
     * @param authDict
     * @return
     */
    Page<AuthDict> getDictKeyList(AuthDict authDict,int page,int limit);

    /**
     * 版本维护升级时复制一份鉴权数据
     */
    Map<String,Object> copyAuthDictListByInterfaceId(String oldId,String newId);
}

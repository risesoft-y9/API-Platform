package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.AuthDict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface AuthDictRepository extends JpaRepository<AuthDict, String>, JpaSpecificationExecutor<AuthDict>{

    //获取字典key和字典主id
    @Query(value="select dia.parameterId,dia.fieldName,dia.parameterName from AuthDict dia where dia.parameterName like :parameterName and dia.fieldName like :fieldName")
    Page<AuthDict> findDictKeyPage(@Param("fieldName") String fieldName,@Param("parameterName") String parameterName, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "delete  from Y9_INTERFACE_AUTH_DICT where INTERFACE_ID = ?1 " ,nativeQuery = true)
    void deleteInfoByInterfaceId(String interfaceId);

    @Modifying
    @Transactional
    @Query(value = "update Y9_INTERFACE_AUTH_DICT set IS_DELETE = 'Y' where INTERFACE_ID = ?1 " ,nativeQuery = true)
    void deleteInfoByInterfaceIdLogic(String interfaceId);

    List<AuthDict> findByInterfaceId(String interfaceId);

    List<AuthDict> findByParameterId(String parameterId);

    List<AuthDict> findByParameterIdIn(List<String> parameterIds);

    List<AuthDict> findByInterfaceIdAndIsDelete(String interfaceId,String isDelete);
}

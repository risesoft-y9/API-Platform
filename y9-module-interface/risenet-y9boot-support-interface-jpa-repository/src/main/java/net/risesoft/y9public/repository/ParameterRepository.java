package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ParameterRepository extends JpaRepository<Parameter, String>, JpaSpecificationExecutor<Parameter>{
    @Query(value = "select pa from Parameter pa where pa.interfaceId = :interfaceId order by pa.sort,pa.pid")
    List<Parameter> findAllByInterfaceId(@Param("interfaceId") String interfaceId);
    @Modifying
    @Transactional
    @Query(value = "delete  from Y9_INTERFACE_PARAMETER where INTERFACE_ID = ?1 " ,nativeQuery = true)
    void deleteByInterfaceId(String interfaceId);

    List<Parameter> findAllByInterfaceIdAndRequired(String interfaceId,String required);

    List<Parameter> findAllByInterfaceIdIn(List<String> interfaceIds);
}

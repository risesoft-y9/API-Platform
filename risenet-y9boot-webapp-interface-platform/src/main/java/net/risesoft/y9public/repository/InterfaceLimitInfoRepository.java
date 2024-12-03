package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.InterfaceLimitInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

public interface InterfaceLimitInfoRepository extends JpaRepository<InterfaceLimitInfo, String>, JpaSpecificationExecutor<InterfaceLimitInfo> {
    @Modifying
    @Transactional
    @Query(value = "delete  from Y9_INTERFACE_LIMIT where INTERFACE_ID = ?1 ", nativeQuery = true)
    void deleteByInterfaceId(String interfaceId);

    @Query("SELECT e FROM InterfaceLimitInfo e WHERE e.interfaceId IN :ids")
    List<InterfaceLimitInfo> findByIds(@Param("ids") Collection<String> ids);

    InterfaceLimitInfo findAllByInterfaceId(String interfaceId);
}

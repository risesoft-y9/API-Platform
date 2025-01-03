package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.SystemIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface SystemIdentifierRepository extends JpaRepository<SystemIdentifier, String>, JpaSpecificationExecutor<SystemIdentifier>{

    @Modifying
    @Transactional
    @Query(value = "update Y9_INTERFACE_SYSTEM_IDENTIFIER SET IS_DELETE = 'Y' from  where ID = ?1 " ,nativeQuery = true)
    void deleteInfoByIdLogic(String Id);

    List<SystemIdentifier> findByParameterTypeAndIsDelete(String type,String isDelete);

    List<SystemIdentifier> findByParameterTypeAndPidAndIsDelete(String type,String pid,String isDelete);
}

package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.Blacklisting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface BlacklistingRepository extends JpaRepository<Blacklisting, String>, JpaSpecificationExecutor<Blacklisting>{

    @Modifying
    @Transactional
    @Query(value = "update Y9_INTERFACE_BLACKLISTING SET IS_DELETE = 'Y'  where ID = ?1 " ,nativeQuery = true)
    void deleteInfoByIdLogic(String Id);

    List<Blacklisting> findByInterfaceIdsLikeAndIsEnableAndIsDelete(String ids,String isEnable,String isDelete);

    @Query(value = "select * from Y9_INTERFACE_BLACKLISTING where INTERFACE_IDS like ?1 and IS_DELETE = 'N' and IS_ENABLE='true' " ,nativeQuery = true)
    List<Blacklisting> findByInterfaceId(String id);
}

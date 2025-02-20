package net.risesoft.y9public.repository;


import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.InterfaceManage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterfaceManageRepository extends JpaRepository<InterfaceManage, String>, JpaSpecificationExecutor<InterfaceManage>{

    @Query(value="select itf.*,apr.APPROVE_STATUS from Y9_INTERFACE_MANAGE_INFO itf left join Y9_INTERFACE_APPROVE apr on itf.id = apr.INTERFACE_ID and apr.IS_OVER='N' " +
            "where itf.IS_DELETE='N' and itf.IS_TEST='N' and itf.IS_OVERWRITE='N' and itf.INTERFACE_NAME like '%'+?1+'%' and itf.PERSON_ID = ?2",nativeQuery = true)
    Page<InterfaceManageDTO> findAllInterfaceAndApprove(String interfaceName, String personId,Pageable pageable);

    //获取审批通过且有申请调用的数据
    @Query(value="select itf.* from Y9_INTERFACE_MANAGE_INFO itf left join Y9_INTERFACE_Apply nia on itf.id = nia.INTERFACE_ID " +
            "where itf.IS_DELETE='N' and itf.IS_TEST='N' and itf.IS_OVERWRITE='N' and nia.APPROVE_STATUS = '审批通过' and itf.id = ?1 and nia.APPLY_PERSON_ID = ?2",nativeQuery = true)
    InterfaceManage findDataByIdAndApply(String id, String personId);

    List<InterfaceManage> findByIdIn(List<String> ids);

    List<InterfaceManage> findBySameInterfaceIdAndIsDelete(String sameId,String isDelete);

    @Query(value="select count(*) from Y9_INTERFACE_MANAGE_INFO itf where itf.INTERFACE_STATUS = ?1",nativeQuery = true)
    long getStatusCount(String status);

    @Query(value="select itf.ID from Y9_INTERFACE_MANAGE_INFO itf where itf.INTERFACE_STATUS = ?1",nativeQuery = true)
    List<String> getIdsByStatus(String status);

    @Query(value="select count(*) from Y9_INTERFACE_MANAGE_INFO",nativeQuery = true)
    long getAllCount();

    @Query(value="select id from InterfaceManage where interfaceStatus='发布' and isDelete='N' and executeInstanceId =?1")
    List<Long> getAllByExecuteInstanceId(String id);
}

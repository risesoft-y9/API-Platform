package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.Approve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApproveRepository extends JpaRepository<Approve, String>, JpaSpecificationExecutor<Approve>{
    List<Approve> findByInterfaceIdInAndIsNewAndApplyTypeIn(List<String> interfaceIds,String isNew,List<String> applyType);

    List<Approve> findByInterfaceIdInAndIsOver(List<String> interfaceIds,String isOver);

    @Query(value="select nia from Approve nia left join InterfaceManage nimi on nia.interfaceId = nimi.id where nimi.interfaceName like :interfaceName order by nia.isOver ,nia.createTime desc")
    Page<Approve> findListPage(@Param("interfaceName") String interfaceName, Pageable pageable);
    @Query(value="select nia.*  from y9_interface_approve nia left join y9_interface_manage_info nimi \n" +
            "on nia.INTERFACE_ID = nimi.ID order by IS_OVER ,CREATETIME desc",nativeQuery = true)
    List<Approve> findList();

    //根据接口id查询是否有正在审核的审核记录
    List<Approve> findByInterfaceIdAndIsOver(String interfaceId,String isOver);

    //根据接口id查询是否有最新的审核记录
    List<Approve> findByInterfaceIdAndIsNewAndApplyTypeIn(String interfaceId,String isNew,List<String> applyTypes);

    //根据申请id查询审批信息
    List<Approve> findByApplyIdInAndApplyType(List<String> applyId, String applyType);

    //根据申请id查询正在审批的信息
    List<Approve> findByApplyIdInAndApplyTypeAndIsOver(List<String> applyId, String applyType,String isOver);

    //根据申请id查询最新的审批信息
    List<Approve> findByApplyIdInAndApplyTypeAndIsNew(List<String> applyId, String applyType,String isNew);

}

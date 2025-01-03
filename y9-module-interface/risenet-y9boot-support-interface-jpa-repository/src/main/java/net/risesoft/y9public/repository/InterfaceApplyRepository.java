package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.InterfaceApply;
import net.risesoft.y9public.vo.ApplyVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterfaceApplyRepository extends JpaRepository<InterfaceApply, String>, JpaSpecificationExecutor<InterfaceApply>{
    //根据接口id和人员id，申请类型查询该接口的申请列表
    List<InterfaceApply> findByInterfaceIdAndApplyPersonIdAndApplyType(String interfaceId, String applyPersonId, String applyType);

    //根据人员id和申请类型查询已申请接口列表
    List<InterfaceApply> findByApplyPersonIdAndApplyType(String applyPersonId, String applyType);

    //根据接口id和人员id，申请类型查询该接口的申请列表,根据创建时间排序
    List<InterfaceApply> findByInterfaceIdAndApplyPersonIdAndApplyTypeOrderByCreateTimeDesc(String interfaceId, String applyPersonId, String applyType);

    //查询每个人的申请记录
    @Query(value="select new net.risesoft.y9public.vo.ApplyVo(nia.id,nia.applyPersonName,nimi.approveStatus,nia.createTime,nia.applyReason,nia.interfaceId,nia.sameId,nimi.id) from InterfaceApply nia left join Approve nimi on nia.id = nimi.applyId where nia.applyType='0' and nia.isEffective='Y' and nia.applyPersonId = :personId and nia.interfaceId = :interfaceId order by nia.createTime desc")
    Page<ApplyVo> findListPage(@Param("personId") String personId,@Param("interfaceId") String interfaceId, Pageable pageable);

    //根据userKey查询申请信息
    @Query(value="select nia from InterfaceApply nia left join Approve nimi on nia.id = nimi.applyId where nia.applyType='0' and nia.userKey = :userKey and nimi.isNew='N' order by nia.createTime desc")
    InterfaceApply findDataByUserKey(@Param("userKey") String userKey);

    //查询每个人的申请记录是否有通过的记录
    @Query(value="select EXISTS(select nia.id,nimi.approve_status from Y9_INTERFACE_APPLY nia left join Y9_INTERFACE_APPROVE nimi on nia.id = nimi.apply_id where nia.apply_type='0' and nia.is_effective='Y' and nimi.approve_status = '通过' and nia.apply_person_id = ?1 and nia.interface_id = ?2 order by nia.createtime desc) as test",nativeQuery = true)
    Integer findListIsPass(@Param("personId") String personId,@Param("interfaceId") String interfaceId);
}

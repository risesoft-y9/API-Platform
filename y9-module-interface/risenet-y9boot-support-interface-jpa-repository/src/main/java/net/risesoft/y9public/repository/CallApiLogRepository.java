package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.Blacklisting;
import net.risesoft.y9public.entity.CallApiRequestLogInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CallApiLogRepository extends JpaRepository<CallApiRequestLogInfo, String>, JpaSpecificationExecutor<CallApiRequestLogInfo> {
    @Query(value = "SELECT GROUP_CONCAT(DISTINCT INTERFACETYPE) as interfaceType," +
            "GROUP_CONCAT(DISTINCT LIMITRESULT) as limitResult,GROUP_CONCAT(DISTINCT AUTHENTIC) as authentic," +
            "GROUP_CONCAT(DISTINCT AUTHENTICATIONRESULT) as authenticationResult,GROUP_CONCAT(DISTINCT ISLIMIT) as isLimit," +
            "RESPONSEMSG as responseMsg" +
            "  from y9_call_api_request_log_info group by RESPONSEMSG" ,nativeQuery = true)
    List<Map<String,Object>> getDataGroup();

    @Query(value = "SELECT GROUP_CONCAT(DISTINCT APPLYSYSTEMNAME) as applySystemName,DEPTNAME as deptName" +
            "  from y9_call_api_request_log_info group by DEPTNAME" ,nativeQuery = true)
    List<Map<String,Object>> getDataGroupByDeptName();

    Long countByRequestStartTimeBetweenAndInterfaceIdIn(Date start, Date end, List<String> interfaceIds);
}

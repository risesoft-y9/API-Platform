package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.ViewApprove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ViewApproveRepository extends JpaRepository<ViewApprove, String>, JpaSpecificationExecutor<ViewApprove>{

}

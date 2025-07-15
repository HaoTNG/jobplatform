package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    List<Application> findByJobId(Long jobId);
    List<Application> findByJobSeekerId(Long jobSeeker);

}

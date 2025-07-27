package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.Application;
import com.haotng.jobplatform.entity.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    List<Application> findByJobId(Long jobId);
    List<Application> findByJobSeeker(JobSeeker jobSeeker);

}

package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;



public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {

    JobSeeker findByUserId(Long userId);
}

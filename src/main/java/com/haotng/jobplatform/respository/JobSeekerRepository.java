package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {

    Optional<JobSeeker> findByUserId(Long userId);

    Optional<JobSeeker> findByUserEmail(String userEmail);
}

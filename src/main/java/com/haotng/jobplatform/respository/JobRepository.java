package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployer(Employer employer);

    List<Job> findByEmployerId(Long employerId);

}

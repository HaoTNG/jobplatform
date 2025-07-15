package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRepository extends JpaRepository<Employer, Long> {

    Employer findByUserId(Long userId);
}

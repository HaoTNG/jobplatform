package com.haotng.jobplatform.respository;

import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Long> {

    Optional<Employer> findByUserId(Long userId);
    Optional<Employer> findByUserEmail(String email);
}

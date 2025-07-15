package com.haotng.jobplatform.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name="job_seekers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeeker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name="user_id",nullable=false, unique=true)
    private User user;

    private String fullName;
    private String phone;
    private String address;
    private String skills;
    private String experience;
}

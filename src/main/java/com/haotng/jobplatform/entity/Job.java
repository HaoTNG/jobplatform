package com.haotng.jobplatform.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private Double salary;

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;
}

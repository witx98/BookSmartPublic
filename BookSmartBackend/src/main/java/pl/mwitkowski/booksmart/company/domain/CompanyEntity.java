package pl.mwitkowski.booksmart.company.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.mwitkowski.booksmart.jpa.BaseEntity;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@Table(name = "companies")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = "workers")
public class CompanyEntity extends BaseEntity {

    @Column(unique = true)
    private String companyName;

    @Column(unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    private String street;

    private String city;

    private String zipCode;

    private Long pictureId;

    @Column(name = "description", length = 1024)
    private String description;


    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "company")
    @JsonIgnoreProperties("company")
    @Builder.Default
    private Set<WorkerEntity> workers = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    @Builder.Default
    private Set<ServiceEntity> services = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

package pl.mwitkowski.booksmart.service.domain;


import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.mwitkowski.booksmart.jpa.BaseEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Builder
@Setter
@Getter
@Entity
@Table(name = "services")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ServiceEntity extends BaseEntity {

    private String serviceName;
    private Duration duration;
    private BigDecimal price;

    @CollectionTable(
            name = "services_keywords",
            joinColumns = @JoinColumn(name = "service_id")
    )
    @Column(name = "keywords")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> keywords = new HashSet<>();

}

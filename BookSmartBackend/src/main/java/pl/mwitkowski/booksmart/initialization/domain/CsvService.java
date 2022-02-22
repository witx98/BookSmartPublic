package pl.mwitkowski.booksmart.initialization.domain;


import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsvService {

    @CsvBindByName
    private String serviceName;

    @CsvBindByName
    private String duration;

    @CsvBindByName
    private BigDecimal price;

    @CsvBindByName
    private String keywords;

    @CsvBindByName
    private String companyEmail;

}

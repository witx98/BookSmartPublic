package pl.mwitkowski.booksmart.initialization.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsvCompany {

    @CsvBindByName
    private String companyName;

    @CsvBindByName
    private String phone;

    @CsvBindByName
    private String email;

    @CsvBindByName
    private String street;

    @CsvBindByName
    private String city;

    @CsvBindByName
    private String zipCode;

    @CsvBindByName
    private String picture;

    @CsvBindByName
    private String description;

}

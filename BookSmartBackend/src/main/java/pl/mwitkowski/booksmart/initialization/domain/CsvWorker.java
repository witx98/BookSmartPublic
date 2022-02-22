package pl.mwitkowski.booksmart.initialization.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsvWorker {

    @CsvBindByName
    private String firstname;

    @CsvBindByName
    private String lastname;

    @CsvBindByName
    private String phone;

    @CsvBindByName
    private String email;

    @CsvBindByName
    private String companyEmail;

    @CsvBindByName
    private String picture;


}

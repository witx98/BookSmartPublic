package pl.mwitkowski.booksmart.initialization.domain;


import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsvSchedule {

    @CsvBindByName
    private String date;

    @CsvBindByName
    private String startTime;

    @CsvBindByName
    private String endTime;

    @CsvBindByName
    private String workerEmail;

}

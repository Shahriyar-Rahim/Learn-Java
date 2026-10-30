import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTime{
    public static void main(String[] args) {
        LocalDate lobj = LocalDate.now();
        System.out.println(lobj);

        LocalTime tobj = LocalTime.now();
        System.out.println(tobj);

        LocalDateTime lmobj = LocalDateTime.now();
        System.out.println(lmobj);

        System.out.println("Before formatting: " + lmobj);

        DateTimeFormatter dform = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        String formdt = lmobj.format(dform);
        System.out.println("After formatting: " + formdt);
    }
}
package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class Category {
    private static Integer counter = 0;

    private Integer id;
    private String perefix;
    private String name;

    public Category(String perefix, String name) {
        counter++;

        this.id = counter;
        this.perefix = perefix;
        this.name = name;
    }
}

package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.bot.util.BotConstants;

import static uz.pdp.bot.util.BotConstants.PATH_DEFAULT_IMAGE;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    private static Integer counter = 0;

    private Integer id;
    private Integer categoryId;
    private String name;
    private Double price = 0.0d;
    private String imageUrl = PATH_DEFAULT_IMAGE;

    public Product(String name, Double price, Integer categoryId, String imageUrl) {
        counter++;

        this.id = counter;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}

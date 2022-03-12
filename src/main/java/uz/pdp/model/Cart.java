package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class Cart {

    private Long userId; // id sifatida userId ni ishlatamiz

    public Cart(Long userId) {
        this.userId = userId;
    }
}

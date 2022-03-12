package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CartProduct {
  private static Long counter = 0L;

  private Long id;
  private Long cartId; // bu cart.userId degani
  private Integer productId;
  private Integer amount;

  public CartProduct(Long cartId, Integer productId, Integer amount) {
    counter++;

    this.id = counter;
    this.cartId = cartId;
    this.productId = productId;
    this.amount = amount;
  }
}

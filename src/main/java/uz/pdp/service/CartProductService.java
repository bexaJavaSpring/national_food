package uz.pdp.service;

import uz.pdp.model.CartProduct;
import uz.pdp.repository.Database;

public class CartProductService {
    public static void deleteCartProductById(Long cartProductId){
        CartProduct cartProduct = null;

        for (CartProduct cartProduct1 : Database.cartProducts) {
            if(cartProduct1.getId().equals(cartProductId)){
                cartProduct = cartProduct1;
                break;
            }
        }

        if(cartProduct != null){
            Database.cartProducts.remove(cartProduct);
            Database.writeDataToJsonFile("cartproducts");
        }
    }
}

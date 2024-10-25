package pages;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;

public class CartPage {
    private final RequestSpecification request;

    public CartPage(RequestSpecification request) {
        this.request = request;
    }

    public Response createCart() {
        return request.post("carts");
    }

    public Response addItemToCart(String cartId, JSONObject body) {
        return request
            .contentType("application/json")
            .body(body.toString())
            .post("carts/" + cartId +"/items");
    }

    public Response updateItemQuantity(String cartId, int itemId, JSONObject body) {
        return request
            .contentType("application/json")
            .body(body.toString())
            .patch("carts/" + cartId +"/items/" + itemId);
    }

    public Response replaceItemCart(String cartId, int itemID, JSONObject body) {
        return request
            .contentType("application/json")
            .body(body.toString())
            .put("carts/" + cartId +"/items/" + itemID);
    }

    public Response deleteItemFromCart(String cartId, int itemId) {
        return request
            .delete("carts/" + cartId +"/items/" + itemId);
    }

    public Response getItemsFromCart(String cartId) {
        return request
            .get("carts/" + cartId + "/items");
    }

}
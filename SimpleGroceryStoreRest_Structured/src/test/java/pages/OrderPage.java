package pages;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;

public class OrderPage {
    private final RequestSpecification request;

    public OrderPage(RequestSpecification request) {
        this.request = request;
    }

    public Response createOrder(JSONObject body) {
        return request
            .contentType("application/json")
            .body(body.toString())
            .post("orders");

    }

    public Response getOrder(String orderId) {
        return request
            .get("orders/" + orderId);
    }

    public Response deleteOrder(String orderId) {
        return request
            .delete("orders/" + orderId);
    }

    public Response getAllOrders(String accessToken) {
        return request
            .header("Authorization", "Bearer " + accessToken)
            .get("orders");
    }

    public Response getAllOrdersAfterCreation() {
        return request
            .get("orders");
    }

    public Response updateOrder(String orderID, JSONObject body){
        return request
            .contentType("application/json")
            .body(body.toString())
            .patch("orders/" + orderID);
    }
}

package pages;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

public class ProductPage {
    private final RequestSpecification request;

    public ProductPage(RequestSpecification request) {
        this.request = request;
    }

    public Response getAllProducts() {
        return request.get("products");
    }

    public Response getProductById(int productId) {
        return request.get("products/" + productId);
    }

    public Response filterAllProducts(int limit) {
        return request
            .queryParam("results", limit)
            .get("products");
    }

    public List<Map<String, Object>> getProducts() {
        Response response = getAllProducts();
        return response.jsonPath().getList("results");
    }
}

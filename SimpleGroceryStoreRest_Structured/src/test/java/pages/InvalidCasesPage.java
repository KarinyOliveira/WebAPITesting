package pages;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class InvalidCasesPage {
    private final RequestSpecification request;

    public InvalidCasesPage(RequestSpecification request) {
        this.request = request;
    }

    public Response getOrdersWithInvalidToken() {
        return request.header("Authorization", "Bearer invalid-token")
            .get("orders");
    }

    public Response invalidEndpoint() {
        return request.get("invalid-endpoint");
    }
}

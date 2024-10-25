package pages;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class StatusPage {
    private final RequestSpecification request;

    public StatusPage(RequestSpecification request) {
        this.request = request;
    }

    public Response getStatus() {
        return request.get("status");
    }

    public Response getWelcomeMessage() {
        return request.get("");
    }
}

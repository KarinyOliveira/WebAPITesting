package pages;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;

public class AuthenticationPage {
    private final RequestSpecification request;

    public AuthenticationPage(RequestSpecification request) {
        this.request = request;
    }

    public Response createToken(JSONObject body) {
        return request
            .contentType("application/json")
            .body(body.toString())
            .post("api-clients/");
    }
}

package factories;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class ApiFactory {
    private static final String BASE_URL = "https://simple-grocery-store-api.glitch.me";

    public static RequestSpecification createRequest() {
        return given().baseUri(BASE_URL);
    }
}

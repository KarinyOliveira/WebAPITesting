//Student Name: Kariny Oliveira
//Student Number: CT1004878

import com.github.javafaker.Faker;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SimpleGroceryStore {

    private static final String BASE_URL = "https://simple-grocery-store-api.glitch.me";
    private static final String STATUS_ENDPOINT = "status";
    private static final String PRODUCTS_ENDPOINT = "products";
    private static final String API_CLIENTS_ENDPOINT = "api-clients/";
    private static final String CART_ENDPOINT = "carts";

    private RequestSpecification request = given().baseUri(BASE_URL);
    private Integer firstProductID;
    private Integer secondProductID;
    private int totalProducts;
    private String accessToken = null;
    private String cartID = null;
    private int countProductsInCart = 0;
    private int itemID = 0;


    @BeforeMethod
    public void setup() {
        // Recreate the request specification to avoid shared state between tests
        request = given().baseUri(BASE_URL);
    }

    @Test
    public void verifyWelcomeMessage() {
        request
            .get(BASE_URL)
            .then()
            .statusCode(200)
            .body("$", hasKey("message"))
            .body("message", isA(String.class))
            .body("message", equalTo("Simple Grocery Store API."));
    }

    @Test
    public void verifyStatusPage() {
        validateStatusPage(request.get(STATUS_ENDPOINT));
    }

    private void validateStatusPage(Response response) {
        response
            .then()
            .statusCode(200)
            .statusLine("HTTP/1.1 200 OK")
            .body("status", isA(String.class));
        String status = response.path("status");
        assert (status.equals("UP"));
    }

    @Test
    public void verifyAllProductsReturns200() {
        validateProductsResponse(request.get(PRODUCTS_ENDPOINT));
    }

    private void validateProductsResponse(Response response) {
        response
            .then()
            .statusCode(200)
            .body("results", hasSize(greaterThan(0)));

        List<Map<String, Object>> products = response.as(new TypeRef<List<Map<String, Object>>>() {
        });
        products.stream()
            .filter(product -> (Boolean) product.get("inStock"))
            .limit(2)
            .forEach(product -> {
                if (firstProductID == null) {
                    firstProductID = ((Number) product.get("id")).intValue();
                } else if (secondProductID == null) {
                    secondProductID = ((Number) product.get("id")).intValue();
                }
            });

        totalProducts = products.size();
    }

    @Test(dependsOnMethods = "verifyAllProductsReturns200")
    public void getAllProductsLimit() {
        request.queryParam("results", totalProducts)
            .get(PRODUCTS_ENDPOINT)
            .then()
            .statusCode(200);
    }

    @Test(dependsOnMethods = "verifyAllProductsReturns200")
    public void getAllProductsOverlimit() {
        request.queryParam("results", totalProducts + 1)
            .get(PRODUCTS_ENDPOINT)
            .then()
            .statusCode(400)
            .statusLine("HTTP/1.1 400 Bad Request");
    }

    @Test
    public void getAllProductsCategory() {
        request.queryParam("category", "fresh-produce")
            .get(PRODUCTS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("results", hasSize(greaterThan(0)));
    }

    @Test(dependsOnMethods = "verifyAllProductsReturns200")
    public void getSpecificProduct() {
        Response response = request
            .get(PRODUCTS_ENDPOINT + "/" + firstProductID)
            .then()
            .statusCode(200)
            .body("id", equalTo(firstProductID))
            .extract()
            .response();  // Extract the response for further validation

        // Validate that current-stock is greater than 0
        var currentStock = response.jsonPath()
            .getInt("current-stock");
        Assert.assertTrue(currentStock > 0, "The current-stock should be greater than 0");
    }

    @Test(dependsOnMethods = "verifyAllProductsReturns200")
    public void getSpecificProductValidateFields() {
        Response response = request.get(PRODUCTS_ENDPOINT + "/" + firstProductID);
        response.then()
            .statusCode(200);

        String[] requiredFields = {"id", "price", "category", "name", "manufacturer", "current-stock", "inStock"};
        for (String field : requiredFields) {
            Assert.assertTrue(response.body()
                .asString()
                .contains(field), "Field '" + field + "' is missing");
        }

        validateFieldTypes(response.body()
            .asString());
    }

    private void validateFieldTypes(String responseBody) {
        JsonPath jsonPath = new JsonPath(responseBody);
        Assert.assertTrue(jsonPath.get("id") instanceof Integer, "Field 'id' should be of type Integer");
        Assert.assertTrue(jsonPath.get("price") instanceof Float, "Field 'price' should be of type Float");
        Assert.assertTrue(jsonPath.get("category") instanceof String, "Field 'category' should be of type String");
        Assert.assertTrue(jsonPath.get("name") instanceof String, "Field 'name' should be of type String");
        Assert.assertTrue(jsonPath.get("manufacturer") instanceof String, "Field 'manufacturer' should be of type String");
        Assert.assertTrue(jsonPath.get("current-stock") instanceof Integer, "Field 'current-stock' should be of type Integer");
        Assert.assertTrue(jsonPath.get("inStock") instanceof Boolean, "Field 'inStock' should be of type Boolean");
    }

    @Test
    public void getInvalidProduct() {
        request
            .get(PRODUCTS_ENDPOINT + "/" + 9999)
            .then()
            .statusCode(404)
            .body("error", isA(String.class))
            .body("error", equalTo("No product with id 9999."));
    }

    @Test(dependsOnMethods = "verifyAllProductsReturns200")
    public void validateProductLabelfromProduct() {
        Response response = request.queryParam("product-label", "true")
            .get(PRODUCTS_ENDPOINT + "/" + firstProductID);
        response.then()
            .statusCode(200);

        String[] requiredFields = {"id", "price", "category", "name", "manufacturer", "current-stock", "inStock", "product-label"};
        for (String field : requiredFields) {
            Assert.assertTrue(response.body()
                .asString()
                .contains(field), "Field '" + field + "' is missing");
        }
        String productLabel = response.jsonPath()
            .getString("product-label");
        assert (productLabel.contains("file/pdf"));
    }

    @Test(dependsOnMethods = "verifyAllProductsReturns200")
    public void validateNoParametersfromInvalidProduct() {
        Response response = request.queryParam("product-label", "true")
            .get(PRODUCTS_ENDPOINT + "/" + 9999);
        response.then()
            .statusCode(404);

        String[] requiredFields = {"price", "category", "name", "manufacturer", "current-stock", "inStock", "product-label"};
        for (String field : requiredFields) {
            Assert.assertFalse(response.body()
                .asString()
                .contains(field), "Field '" + field + "' is present");
        }
    }

    @Test
    public void createNewCart201() {
        request
            .when()
            .post(CART_ENDPOINT)
            .then()
            .statusCode(201);
    }

    @Test
    public void createNewCartCreatedMessage() {
        request
            .when()
            .post(CART_ENDPOINT)
            .then()
            .statusLine("HTTP/1.1 201 Created");
    }

    @Test
    public void createNewCartPropertiesValidation() {

        Response response = request
            .contentType(ContentType.JSON)
            .when()
            .post(CART_ENDPOINT);

        response
            .then()
            .body("$", hasKey("cartId"));

        boolean flag = Boolean.parseBoolean(response.jsonPath()
            .getString("created"));
        if (flag) {
            cartID = response.jsonPath()
                .getString("cartId");
        }

    }

    @Test(dependsOnMethods = "createNewCartPropertiesValidation")
    public void getValidCart() {
        request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}")
            .then()
            .statusCode(200);

    }

    @Test(dependsOnMethods = "createNewCartPropertiesValidation")
    public void getValidCartValidateDate() {
        Response response = request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}");

        response
            .then()
            .statusCode(200)  // Check for successful response
            .body("items", hasSize(0))
            .body("created", notNullValue())  // Check that "created" field exists
            .body("created", matchesPattern(
                "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"));  // Validate ISO 8601 format
    }

    @Test(dependsOnMethods = "createNewCartPropertiesValidation")
    public void getItemsFromEmptyCart() {
        Response response = request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}/items");

        response
            .then()
            .statusCode(200)
            .body("", hasSize(0));

        // Extract the items from the response
        List<Map<String, Object>> products = response.jsonPath()
            .getList("$");

        // Count the number of products
        countProductsInCart = products.size();

    }

    @Test(dependsOnMethods = {"getItemsFromEmptyCart","createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void addItemToCart() {
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("productId", firstProductID);
        bodyJSON.put("quantity", 1);

        Response response = request
            .pathParams("cartid", cartID)
            .contentType(ContentType.JSON)
            .body(bodyJSON.toString())
            .when()
            .post(CART_ENDPOINT + "/{cartid}/items");

        response
            .then()
            .statusCode(201);

        boolean created = response.jsonPath()
            .getBoolean("created");
        Assert.assertTrue(created, "Item was not created in the cart");

        itemID = response.jsonPath()
            .getInt("itemId");

    }

    @Test(dependsOnMethods = "createNewCartPropertiesValidation")
    public void addItemToCart_InvalidProductID() {
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("productId", 999999);  // Use a non-existent product ID
        bodyJSON.put("quantity", 1);

        Response response = request
            .pathParam("cartid", cartID)
            .contentType(ContentType.JSON)
            .body(bodyJSON.toString())
            .when()
            .post(CART_ENDPOINT + "/{cartid}/items");

        // Assert that the response status code is 400
        response
            .then()
            .statusCode(400)
            .body("error", equalTo("Invalid or missing productId."));
    }

    @Test(dependsOnMethods = {"addItemToCart", "createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void getCartAfterAddingItemsAndValidate() {
        Response response = request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}/items");

        response
            .then()
            .statusCode(200)
            .body("", hasSize(greaterThan(0)));

        List<Map<String, Object>> cartItems = response.jsonPath()
            .getList("");

        Assert.assertTrue(cartItems.size() > countProductsInCart, "The cart should have some item");

        boolean itemFound = cartItems.stream()
            .anyMatch(item -> {
                Integer id = (Integer) item.get("id");
                Integer productId = (Integer) item.get("productId");
                Integer quantity = (Integer) item.get("quantity");

                return id != null && id.equals(itemID) &&
                    productId != null && productId.equals(firstProductID) &&
                    quantity != null && quantity.equals(1);
            });
        Assert.assertTrue(itemFound, "The added item should be present in the cart");
    }

    @Test(dependsOnMethods = {"addItemToCart", "createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void updateQuantityItemsFromCart() {
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("quantity", 2);
        Response response = request
            .pathParams("cartid", cartID)
            .pathParams("itemid", itemID)
            .contentType(ContentType.JSON)
            .body(bodyJSON.toString())
            .patch(CART_ENDPOINT + "/{cartid}/items/{itemid}");

        response
            .then()
            .statusCode(204)
            .statusLine("HTTP/1.1 204 No Content");
    }

    @Test(dependsOnMethods = {"getItemsFromEmptyCart","updateQuantityItemsFromCart","addItemToCart", "createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void getItemsFromCartAfterUpdatingQuantity(){
        Response response = request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}/items");

        response
            .then()
            .statusCode(200)
            .body("", hasSize(greaterThan(0)));

        List<Map<String, Object>> cartItems = response.jsonPath()
            .getList("");

        Assert.assertTrue(cartItems.size() > countProductsInCart, "The cart should have some item");

        boolean itemFound = cartItems.stream()
            .anyMatch(item -> {
                Integer id = (Integer) item.get("id");
                Integer productId = (Integer) item.get("productId");
                Integer quantity = (Integer) item.get("quantity");

                return id != null && id.equals(itemID) &&
                    productId != null && productId.equals(firstProductID) &&
                    quantity != null && quantity.equals(2);
            });
        Assert.assertTrue(itemFound, "The added item should be present in the cart");
    }

    @Test(dependsOnMethods = {"getItemsFromCartAfterUpdatingQuantity","getItemsFromEmptyCart","addItemToCart", "createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void replaceItemsFromCart() {
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("productId", secondProductID);
        bodyJSON.put("quantity", 2);
        Response response = request
            .pathParams("cartid", cartID)
            .pathParams("itemid", itemID)
            .contentType(ContentType.JSON)
            .body(bodyJSON.toString())
            .put(CART_ENDPOINT + "/{cartid}/items/{itemid}");

        response
            .then()
            .statusCode(204)
            .statusLine("HTTP/1.1 204 No Content");
    }

    @Test(dependsOnMethods = {"getItemsFromCartAfterUpdatingQuantity","replaceItemsFromCart","getItemsFromEmptyCart","updateQuantityItemsFromCart","addItemToCart", "createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void getItemsFromCartAfterReplace(){
        Response response = request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}/items");

        response
            .then()
            .statusCode(200)
            .body("", hasSize(greaterThan(0)));

        List<Map<String, Object>> cartItems = response.jsonPath()
            .getList("");

        Assert.assertTrue(cartItems.size() > countProductsInCart, "The cart should have some item");

        boolean itemFound = cartItems.stream()
            .anyMatch(item -> {
                Integer id = (Integer) item.get("id");
                Integer productId = (Integer) item.get("productId");
                Integer quantity = (Integer) item.get("quantity");

                return id != null && id.equals(itemID) &&
                    productId != null && productId.equals(secondProductID) &&
                    quantity != null && quantity.equals(2);
            });
        Assert.assertTrue(itemFound, "The added item should be present in the cart");
    }

    @Test(priority = 1)
    public void deleteItemFromCart(){
        Response response = request
            .pathParams("cartid", cartID)
            .pathParams("itemid", itemID)
            .delete(CART_ENDPOINT + "/{cartid}/items/{itemid}");

        response
            .then()
            .statusCode(204);
    }

    @Test(dependsOnMethods = {"deleteItemFromCart","replaceItemsFromCart","getItemsFromEmptyCart","updateQuantityItemsFromCart","addItemToCart", "createNewCartPropertiesValidation", "verifyAllProductsReturns200"})
    public void getItemsFromCartAfterDelete(){
        Response response = request
            .pathParams("cartid", cartID)
            .get(CART_ENDPOINT + "/{cartid}/items");

        response
            .then()
            .statusCode(200)
            .body("", hasSize(equalTo(0)));
    }

    @Test
    public void getAccessToken() {
        Faker faker = new Faker();
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("clientName", faker.name()
            .firstName());
        bodyJSON.put("clientEmail", faker.internet()
            .emailAddress());

        Response response = request
            .contentType(ContentType.JSON)
            .body(bodyJSON.toString())
            .when()
            .post(API_CLIENTS_ENDPOINT);

        response
            .then()
            .body("$", hasKey("accessToken"));

        accessToken = response.jsonPath()
            .getString("accessToken");
    }

    @Test
    public void getAccessToken_InvalidCase() {
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("clientName", "");
        bodyJSON.put("clientEmail", "");

        Response response = request
            .contentType(ContentType.JSON)
            .body(bodyJSON.toString())
            .when()
            .post(API_CLIENTS_ENDPOINT);

        response
            .then()
            .statusCode(400);
    }


}
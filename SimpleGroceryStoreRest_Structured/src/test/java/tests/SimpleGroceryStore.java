package tests;

//Student Name: Kariny Oliveira
//Student Number: CT1004878

import com.github.javafaker.Faker;
import factories.ApiFactory;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class SimpleGroceryStore {

    private Integer firstProductID;
    private Integer secondProductID;
    private int totalProducts;
    private String accessToken;
    private String cartID;
    private Integer itemID;
    private String customerName;
    private String orderID;
    private int totalOrders;
    private CartPage cartPage;
    private OrderPage orderPage;
    private ProductPage productPage;
    private StatusPage statusPage;
    private InvalidCasesPage invalidCasesPage;
    private AuthenticationPage authenticationPage;

    @BeforeClass
    public void setup() {
        RequestSpecification request = ApiFactory.createRequest();
        statusPage = new StatusPage(request);
        productPage = new ProductPage(request);
        cartPage = new CartPage(request);
        orderPage = new OrderPage(request);
        invalidCasesPage = new InvalidCasesPage(request);
        authenticationPage = new AuthenticationPage(request);

    }

    @Test(groups = {"auth"})
    public void getAccessToken() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        body.put("clientName", faker.name().firstName());
        body.put("clientEmail", faker.internet().emailAddress());
        Response response = authenticationPage.createToken(body);

        response
            .then()
            .statusCode(201)
            .body("$", hasKey("accessToken"));

        accessToken = response.jsonPath().getString("accessToken");
    }


    // Group 1: API Status and General Tests
    @Test(groups = {"status", "part1"})
    public void verifyWelcomeMessage() {
        statusPage
            .getWelcomeMessage()
            .then()
            .statusCode(200)
            .body("message", equalTo("Simple Grocery Store API."));
    }

    @Test(groups = {"status", "part1"})
    public void verifyStatusPage() {
        Response response = statusPage.getStatus();

        response
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    // Group 2: Product-related Tests
    @Test(groups = {"products", "part1"})
    public void verifyAllProductsReturns200() {
        Response response = productPage.getAllProducts();

        response
            .then()
            .statusCode(200)
            .body("results", hasSize(greaterThan(0)));

        List<Map<String, Object>> products = response.as(new TypeRef<List<Map<String, Object>>>() {
        });

        products.stream().filter(product -> (Boolean) product.get("inStock")).limit(2).forEach(product -> {
            if (firstProductID == null) {
                firstProductID = ((Number) product.get("id")).intValue();
            } else if (secondProductID == null) {
                secondProductID = ((Number) product.get("id")).intValue();
            }
        });

        totalProducts = products.size();
    }

    @Test(dependsOnMethods = {"verifyAllProductsReturns200"}, groups = {"products", "part1"})
    public void getAllProductsLimit() {
        Response response = productPage.filterAllProducts(totalProducts);

        response
            .then()
            .statusCode(200);
    }

    @Test(dependsOnMethods = {"verifyAllProductsReturns200"}, groups = {"products", "part1"})
    public void getSpecificProduct() {
        Response response = productPage.getProductById(firstProductID);

        response
            .then()
            .statusCode(200)
            .body("id", equalTo(firstProductID));

        Assert.assertTrue(response.jsonPath().getInt("current-stock") > 0, "The current-stock should be greater than 0");
    }

    // Group 3: Cart-related Tests
    @Test(groups = {"cart", "part1"})
    public void createNewCart() {
        Response response = cartPage.createCart();

        response
            .then()
            .statusCode(201)
            .body("$", hasKey("cartId"));

        cartID = response.jsonPath().getString("cartId");
    }

    @Test(dependsOnMethods = {"createNewCart", "verifyAllProductsReturns200"}, groups = {"cart", "part1"})
    public void addItemToCart() {
        JSONObject body = new JSONObject();
        body.put("productId", firstProductID);
        body.put("quantity", 1);
        Response response = cartPage.addItemToCart(cartID, body);

        response
            .then()
            .statusCode(201);

        itemID = response.jsonPath().getInt("itemId");
    }

    @Test(dependsOnMethods = {"addItemToCart", "createNewCart", "verifyAllProductsReturns200"}, groups = {"cart", "part1"})
    public void getItemsInCart() {
        Response response = cartPage.getItemsFromCart(cartID);

        response
            .then()
            .statusCode(200)
            .body("[0].id", equalTo(itemID));

    }

    @Test(dependsOnMethods = {"addItemToCart", "createNewCart", "verifyAllProductsReturns200"}, groups = {"cart", "part1"})
    public void updateQuantityItemsFromCart() {
        JSONObject body = new JSONObject();
        body.put("quantity", 2);
        Response response = cartPage.updateItemQuantity(cartID, itemID, body);

        response
            .then()
            .statusCode(204);
    }

    @Test(dependsOnMethods = {"addItemToCart", "updateQuantityItemsFromCart", "createNewCart", "verifyAllProductsReturns200"}, groups = {"cart", "part1"})
    public void replaceItemsFromCart() {
        JSONObject body = new JSONObject();
        body.put("productId", secondProductID);
        body.put("quantity", 2);
        Response response = cartPage.replaceItemCart(cartID, itemID, body);

        response
            .then()
            .statusCode(204);
    }

    @Test(dependsOnMethods = {"replaceItemsFromCart", "addItemToCart", "updateQuantityItemsFromCart", "createNewCart", "verifyAllProductsReturns200"}, groups = {"cart", "part1"})
    public void deleteItemFromCart() {
        Response response = cartPage.deleteItemFromCart(cartID, itemID);

        response
            .then()
            .statusCode(204);
    }

    @Test(dependsOnMethods = {"deleteItemFromCart", "replaceItemsFromCart", "addItemToCart", "updateQuantityItemsFromCart", "createNewCart", "verifyAllProductsReturns200"}, groups = {"cart", "part1"})
    public void getItemsFromCartAfterDelete() {
        Response response = cartPage.getItemsFromCart(cartID);

        response.then()
            .statusCode(200)
            .body("", hasSize(0));
    }


    @Test(groups = {"cart", "part2"}, dependsOnGroups = {"part1"})
    public void createNewCartToOrder() {
        Response response = cartPage.createCart();

        response.then()
            .statusCode(201)
            .body("$", hasKey("cartId"));

        cartID = response.jsonPath().getString("cartId");
    }

    @Test(dependsOnGroups = {"part1"}, dependsOnMethods = {"createNewCartToOrder"}, groups = {"cart", "part2"})
    public void addItemToCartToOrder() {
        JSONObject body = new JSONObject();
        body.put("productId", firstProductID);
        body.put("quantity", 1);
        Response response = cartPage.addItemToCart(cartID, body);

        response
            .then()
            .statusCode(201);

        itemID = response.jsonPath().getInt("itemId");
    }

    @Test(dependsOnMethods = {"createNewCartToOrder", "addItemToCartToOrder"}, dependsOnGroups = {"part1"}, groups = {"cart", "part2"})
    public void updateQuantityItemsFromCartToOrder() {
        JSONObject body = new JSONObject();
        body.put("quantity", 2);
        Response response = cartPage.updateItemQuantity(cartID, itemID, body);

        response
            .then()
            .statusCode(204);
    }

    @Test(dependsOnGroups = {"part1"}, dependsOnMethods = {"createNewCartToOrder", "addItemToCartToOrder", "updateQuantityItemsFromCartToOrder"}, groups = {"cart", "part2"})
    public void replaceItemsFromCartToOrder() {
        JSONObject body = new JSONObject();
        body.put("productId", secondProductID);
        body.put("quantity", 2);
        Response response = cartPage.replaceItemCart(cartID, itemID, body);

        response
            .then()
            .statusCode(204);

    }

    @Test(groups = {"orders", "part1"}, priority = 1, dependsOnMethods = {"getAccessToken"})
    public void getAllOrders() {
        Response response = orderPage.getAllOrders(accessToken);

        response
            .then()
            .statusCode(200)
            .body("", hasSize(0));
    }

    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewCartToOrder", "addItemToCartToOrder", "updateQuantityItemsFromCartToOrder", "replaceItemsFromCartToOrder"})
    public void createNewOrder() {
        Faker faker = new Faker();
        customerName = faker.name().firstName();
        JSONObject body = new JSONObject();
        body.put("cartId", cartID);
        body.put("customerName", customerName);
        Response response = orderPage.createOrder(body);

        response
            .then()
            .statusCode(201)
            .body("created", equalTo(true))
            .body("$", hasKey("orderId"));

        if (response.jsonPath().getString("orderId") != null)
            orderID = response.jsonPath().getString("orderId");

    }

    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewOrder", "createNewCartToOrder", "addItemToCartToOrder", "updateQuantityItemsFromCartToOrder", "replaceItemsFromCartToOrder"})
    public void getSingleOrder() {
        Response response = orderPage.getOrder(orderID);

        response
            .then()
            .statusCode(200)
            .body("id", equalTo(orderID));

        List<Map<String, Object>> items = response.jsonPath().getList("items");
        Assert.assertNotNull(items, "Items list is null");
        Assert.assertFalse(items.isEmpty(), "Items list is empty");


        for (Map<String, Object> item : items) {
            Integer itemId = (Integer) item.get("id");
            Assert.assertEquals(itemId, itemID, "itemID is wrong");

            Integer productId = (Integer) item.get("productId");
            Assert.assertEquals(productId, secondProductID, "productID is wrong");

            Integer quantity = (Integer) item.get("quantity");
            Assert.assertEquals(quantity, 2, "Quantity is wrong");

        }

        String customer = response.jsonPath().getString("customerName");
        Assert.assertEquals(customer, customerName, "Customer name is wrong");
    }

    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewOrder", "createNewCartToOrder", "addItemToCartToOrder", "updateQuantityItemsFromCartToOrder", "replaceItemsFromCartToOrder"})
    public void getAllOrdersAfterCreation() {
        Response response = orderPage.getAllOrdersAfterCreation();

        response
            .then()
            .statusCode(200)
            .body("results", hasSize(greaterThanOrEqualTo(1)));

        List<Map<String, Object>> jsonData = response.jsonPath().getList("");
        totalOrders = jsonData.size();

        List<Map<String, Object>> matchingOrders = new ArrayList<>();
        for (Map<String, Object> order : jsonData) {
            if (order.get("id").equals(orderID)) {
                matchingOrders.add(order);
            }
        }
        assert !matchingOrders.isEmpty() : "Order ID not found in the returned orders!";

        Map<String, Object> matchingOrder = matchingOrders.get(0);
        List<Map<String, Object>> matchingItems = (List<Map<String, Object>>) matchingOrder.get("items");

        // Verify that the items list is not empty
        assert !matchingItems.isEmpty() : "No items found in the order!";

        // Verify returned order metadata
        assert matchingOrder.get("id").equals(orderID) : "Order ID does not match!";
        assert matchingItems.getFirst().get("id").equals(itemID) : "Item ID does not match!";
        assert matchingItems.getFirst().get("productId").equals(secondProductID) : "Product ID does not match!";
    }


    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewOrder", "getSingleOrder", "getAllOrdersAfterCreation"})
    public void changeCustomerNameOrder() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        body.put("customerName", faker.name().fullName());

        Response response = orderPage
            .updateOrder(orderID, body);

        response
            .then()
            .statusCode(204);
    }

    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewOrder", "getSingleOrder", "getAllOrdersAfterCreation"})
    public void changeCommentOrder() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        body.put("comment", faker.zelda().game());

        Response response = orderPage
            .updateOrder(orderID, body);

        response
            .then()
            .statusCode(204);
    }

    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewOrder", "getSingleOrder", "getAllOrdersAfterCreation","changeCommentOrder","changeCustomerNameOrder"})
    public void checkOrderAfterChangingFields() {
        Response response = orderPage.getOrder(orderID);

        response
            .then()
            .statusCode(200)
            .body("id", equalTo(orderID));

        List<Map<String, Object>> items = response.jsonPath().getList("items");
        Assert.assertNotNull(items, "Items list is null");
        Assert.assertFalse(items.isEmpty(), "Items list is empty");

        for (Map<String, Object> item : items) {
            Integer itemId = (Integer) item.get("id");
            Assert.assertEquals(itemId, itemID, "ItemID is wrong");

            Integer productId = (Integer) item.get("productId");
            Assert.assertEquals(productId, secondProductID, "productID is wrong");

            Integer quantity = (Integer) item.get("quantity");
            Assert.assertEquals(quantity, 2,"Quantity is wrong");

        }

        String customer = response.jsonPath().getString("customerName");
        Assert.assertNotEquals(customer, customerName, "Customer name is not changed");
        customerName = response.jsonPath().getString("customerName");

        String comment = response.jsonPath().getString("comment");
        Assert.assertNotEquals(comment, "", "Comment is not changed");
    }

    @Test(groups = {"orders", "part2"}, dependsOnGroups = {"part1"}, dependsOnMethods = {"getAccessToken","createNewOrder", "getSingleOrder", "getAllOrdersAfterCreation","changeCommentOrder","changeCustomerNameOrder", "checkOrderAfterChangingFields"})
    public void deleteOrder() {
        Response response = orderPage.deleteOrder(orderID);

        response
            .then()
            .statusCode(204);
    }

    @Test(groups = {"orders", "part3"}, dependsOnGroups = {"part2"}, dependsOnMethods = {"createNewOrder", "getSingleOrder", "getAllOrdersAfterCreation","changeCommentOrder","changeCustomerNameOrder", "checkOrderAfterChangingFields", "deleteOrder"})
    public void checkDeletedOrderinAllOrders(){
        Response response = orderPage.getAllOrdersAfterCreation();

        response
            .then()
            .statusCode(200);

        response
            .then()
            .body(equalTo("[]"));
    }

    @Test(groups = {"orders", "part3"}, dependsOnGroups = {"part2"}, dependsOnMethods = {"createNewOrder", "getSingleOrder", "getAllOrdersAfterCreation","changeCommentOrder","changeCustomerNameOrder", "checkOrderAfterChangingFields", "deleteOrder"})
    public void retrieveDeletedOrder(){
        Response response = orderPage.getOrder(orderID);

        response
            .then()
            .statusCode(404);

        String expectedError = "No order with id " + orderID + ".";
        response.then().body("error", equalTo(expectedError));
    }

    //Invalid Cases
    @Test(groups = {"error"},priority = 3)
    public void getInvalidEndpoint() {
       Response response = invalidCasesPage.invalidEndpoint();

       response
            .then()
            .statusCode(404)
            .body("error", equalTo("The resource could not be found. " +
                "Check your endpoint and request method."));
    }

    @Test(groups = {"error"},priority = 3)
    public void addItemToInvalidCart() {
        JSONObject body = new JSONObject();
        body.put("productId", firstProductID);
        body.put("quantity", 1);
        Response response = cartPage.addItemToCart("invalid-cart-id",body);

        response
            .then()
            .statusCode(404)
            .body("error", equalTo("No cart with id invalid-cart-id."));
    }

    @Test(groups = {"error"},priority = 3)
    public void getInvalidProduct() {
        Response response = productPage.getProductById(9999);

        response
            .then()
            .statusCode(404)
            .body("error", equalTo("No product with id 9999."));
    }

}

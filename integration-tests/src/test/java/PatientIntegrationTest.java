import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientIntegrationTest {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    private String getToken() {
        String loginPayload = """
                    {
                        "email": "testuser@test.com",
                        "password": "MySecret123!"
                    }
                """;

        String token = RestAssured.given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        return token;
    }

    @Test
    public void shouldReturnPatientsWithValidToken() {
        String token = getToken();

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", Matchers.notNullValue());
    }
}

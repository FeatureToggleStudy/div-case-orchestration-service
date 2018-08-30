package uk.gov.hmcts.reform.divorce.util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;

import java.util.Base64;

public class IdamUtils {

    private static final String TOKEN = "token";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    static final String CLIENT_ID = "divorce";
    static final String CODE = "code";

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${auth.idam.client.secret}")
    private String idamSecret;


    public void createDivorceCaseworkerUserInIdam(String username, String password) {
        String body = "{\n" +
                "  \"email\": \"" + username + "\",\n" +
                "  \"forename\": \"test\",\n" +
                "  \"password\": \"" + password + "\",\n" +
                "  \"roles\": [\n" +
                "    {\n" +
                "      \"code\": \"" + "caseworker-divorce-courtadmin" + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"surname\": \"test\",\n" +
                "  \"userGroup\": {\n" +
                "    \"code\": \"caseworker\"\n" +
                "  }\n" +
                "}";

        RestAssured.given()
                .header("Content-Type", "application/json")
                .relaxedHTTPSValidation()
                .body(body)
                .post(idamCreateUrl());
    }

    public void createUserInIdam(String username, String password) {
        String body = "{\n" +
                "  \"email\": \"" + username + "\",\n" +
                "  \"forename\": \"test\",\n" +
                "  \"id\": \"test\",\n" +
                "  \"password\": \"" + password + "\",\n" +
                "  \"roles\": [\n" +
                "    {\n" +
                "      \"code\": \"" + "citizen" + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"surname\": \"test\",\n" +
                "  \"userGroup\": {\n" +
                "    \"code\": \"divorce-private-beta\"\n" +
                "  }\n" +
                "}";

        RestAssured.given()
                .header("Content-Type", "application/json")
                .relaxedHTTPSValidation()
                .body(body)
                .post(idamCreateUrl());
    }

    public final void createUserInIdam(RegisterUserRequest registerUserRequest) {
        RestAssured.given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .body(ResourceLoader.objectToJson(registerUserRequest))
                .post(idamCreateUrl());
    }

    public String getUserId(String jwt) {
        Response response = RestAssured.given()
                .header("Authorization", jwt)
                .relaxedHTTPSValidation()
                .get(idamUserBaseUrl + "/details");

        return response.getBody().path("id").toString();
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        Response response = RestAssured.given()
                .header("Authorization", authHeader)
                .relaxedHTTPSValidation()
                .post(idamCodeUrl());

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode() + " body: " + response.getBody().prettyPrint());
        }

        response = RestAssured.given()
                .relaxedHTTPSValidation()
                .post(idamTokenUrl(response.getBody().path("code")));

        String token = response.getBody().path("access_token");
        return "Bearer " + token;
    }

    public final String authenticateUser(String emailAddress, String password) {
        final String authHeader = getBasicAuthHeader(emailAddress, password);
        return "Bearer " + getAuthToken(authHeader);
    }

    final String getBasicAuthHeader(String emailAddress, String password) {
        String userLoginDetails = String.join(":", emailAddress, password);
        return "Basic " + new String(Base64.getEncoder().encode(userLoginDetails.getBytes()));
    }

    private String getAuthCode(String authHeader) {
        return RestAssured.given()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .queryParam("response_type", CODE)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", idamRedirectUri)
                .post(idamUserBaseUrl + "/oauth2/authorize")
                .body()
                .jsonPath().get("code");
    }

    final String getAuthTokenByCode(String code) {
        return RestAssured.given()
                .queryParam("code", code)
                .queryParam("grant_type", AUTHORIZATION_CODE)
                .queryParam("redirect_uri", idamRedirectUri)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", idamSecret)
                .post(idamUserBaseUrl + "/oauth2/token")
                .body()
                .jsonPath().get("access_" + TOKEN);
    }

    final String getAuthToken(String authHeader) {
        return getAuthTokenByCode(getAuthCode(authHeader));
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String idamCodeUrl() {
        return idamUserBaseUrl + "/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=divorce"
                + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {
        return idamUserBaseUrl + "/oauth2/token"
                + "?code=" + code
                + "&client_id=divorce"
                + "&client_secret=" + idamSecret
                + "&redirect_uri=" + idamRedirectUri
                + "&grant_type=authorization_code";
    }
}

package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

public class AdulteryCaseTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit/";

    @Value("${case.orchestration.retrieve-case.context-path}")
    private String retrieveCaseContextPath;

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String submitCasecontextPath;


    @Autowired
    private ObjectMapper customObjectMapper;

    @Test
    public void canSubmitAndRetrieveAnAdulteryDetails() throws Exception {

        final UserDetails userDetails = createCitizenUser();
        final String input = ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + "adultery-divorce-session.json");
        final Response response = submitCase(userDetails.getAuthToken(), input);

        JsonNode inputJson = customObjectMapper.readTree(input);

        final String responseBody = retrieveCase(userDetails.getAuthToken()).getBody().asString();

        final String responseData = customObjectMapper.readTree(responseBody).get("data").toString();

        JSONAssert.assertEquals(input, responseData, false);
    }


    private Response retrieveCase(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.getFromRestService(
            serverUrl + retrieveCaseContextPath,
            headers,
            null
        );
    }

    private Response submitCase(String userToken, String body) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(serverUrl + submitCasecontextPath, headers, body
        );
    }
}

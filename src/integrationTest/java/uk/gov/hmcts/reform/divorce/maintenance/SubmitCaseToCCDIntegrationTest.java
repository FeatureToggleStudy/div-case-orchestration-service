package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.category.ExtendedTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class SubmitCaseToCCDIntegrationTest extends RetrieveCaseSupport {

    private static final String CASE_ID_KEY = "caseId";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit/";
    private static final String ALLOCATED_COURT_ID_KEY = "allocatedCourt.courtId";

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String caseCreationContextPath;

    @Test
    public void givenDivorceSession_WithNoCourt_whenSubmitIsCalled_CaseIsCreated() throws Exception {
        UserDetails userDetails = createCitizenUser();
        Response submissionResponse = submitCase(userDetails, "divorce-session-with-court-selected.json");

        ResponseBody caseCreationResponseBody = submissionResponse.getBody();
        assertThat(submissionResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(caseCreationResponseBody.path(CASE_ID_KEY), is(not("0")));
        String allocatedCourt = caseCreationResponseBody.path(ALLOCATED_COURT_ID_KEY);
        assertThat(allocatedCourt, is(notNullValue()));

        ResponseBody retrieveCaseResponseBody = retrieveCase(userDetails.getAuthToken()).body();
        assertThat(retrieveCaseResponseBody.path(RETRIEVED_DATA_COURT_ID_KEY), is(allocatedCourt));
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenAnExistingCase_whenSubmitIsCalled_aNewCaseIsNotCreated() throws Exception {
        UserDetails userDetails = createCitizenUser();
        Response submissionResponse = submitCase(userDetails, "divorce-session-with-court-selected.json");

        ResponseBody caseCreationResponseBody = submissionResponse.getBody();
        assertThat(submissionResponse.getStatusCode(), is(HttpStatus.OK.value()));
        String existingCaseId = caseCreationResponseBody.path(CASE_ID_KEY);
        assertThat(existingCaseId, is(not("0")));
        String allocatedCourt = caseCreationResponseBody.path(ALLOCATED_COURT_ID_KEY);
        assertThat(allocatedCourt, is(notNullValue()));

        ResponseBody retrieveCaseResponseBody = retrieveCase(userDetails.getAuthToken()).body();
        assertThat(retrieveCaseResponseBody.path(RETRIEVED_DATA_COURT_ID_KEY), is(allocatedCourt));

        submissionResponse = submitCase(userDetails, "divorce-session-with-court-selected.json");
        caseCreationResponseBody = submissionResponse.getBody();
        assertThat(caseCreationResponseBody.path(CASE_ID_KEY), is(existingCaseId));
        allocatedCourt = caseCreationResponseBody.path(ALLOCATED_COURT_ID_KEY);
        assertThat(allocatedCourt, is(notNullValue()));
    }

    private Response submitCase(UserDetails userDetails, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userDetails.getAuthToken());


        String body = null;
        if (fileName != null) {
            body = ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName)
            .replaceAll(USER_DEFAULT_EMAIL, userDetails.getEmailAddress());

        }

        return RestUtil.postToRestService(
                serverUrl + caseCreationContextPath,
                headers,
                body
        );
    }

}
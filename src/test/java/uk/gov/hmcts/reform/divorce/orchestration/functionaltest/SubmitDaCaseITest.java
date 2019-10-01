package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_REQUESTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SubmitDaCaseITest extends MockedFunctionalTest {
    private static final String API_URL = String.format("/submit-da/%s", TEST_CASE_ID);
    private static final String UPDATE_CONTEXT_PATH = "/casemaintenance/version/1/updateCase/" + TEST_CASE_ID + "/";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenNoAuthToken_whenSubmitDa_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(convertObjectToJsonString(getDivorceCaseData()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoPayload_whenSubmitDa_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseUpdateFails_whenSubmitDa_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = getCcdCaseData();
        final Map<String, Object> caseDetails = new HashMap<>();

        caseDetails.put(CASE_STATE_JSON_KEY, DN_PRONOUNCED);
        caseDetails.put(CCD_CASE_DATA_FIELD, caseData);

        stubMaintenanceServerEndpointForUpdate(BAD_REQUEST, DECREE_ABSOLUTE_REQUESTED_EVENT_ID, caseData, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(getDivorceCaseData()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseUpdateIsSuccessful_whenSubmitDa_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = getCcdCaseData();
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> caseDetails = new HashMap<>();

        caseDetails.put(CASE_STATE_JSON_KEY, DN_PRONOUNCED);
        caseDetails.put(CCD_CASE_DATA_FIELD, caseData);

        stubMaintenanceServerEndpointForUpdate(OK, DECREE_ABSOLUTE_REQUESTED_EVENT_ID, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(getDivorceCaseData()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private void stubMaintenanceServerEndpointForUpdate(HttpStatus status, String caseEventId,
                                                        Map<String, Object> caseData, String response) {
        maintenanceServiceServer.stubFor(post(UPDATE_CONTEXT_PATH + caseEventId)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private Map<String, Object> getDivorceCaseData() {
        return ImmutableMap.of("applyForDecreeAbsolute", "Yes");
    }

    private Map<String, Object> getCcdCaseData() {
        return ImmutableMap.of("ApplyForDecreeAbsolute", "YES");
    }
}

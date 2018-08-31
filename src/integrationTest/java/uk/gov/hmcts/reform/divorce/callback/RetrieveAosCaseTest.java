package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RetrieveAosCaseTest extends PetitionSupport {

    private static final String TEST_AOS_COMPLETED_EVENT = "testAosCompleted";

    @Value("${case.orchestration.retrieve-aos-case.context-path}")
    private String retrieveCasePath;

    @Test
    public void givenAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheFirstCase() throws Exception {
        final String userToken = getUserToken(true);

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        Response cosResponse = getFormattedCaseFromCos(userToken, true);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cosResponse.path("id"));
    }

    private Response getFormattedCaseFromCos(String userToken, boolean checkCcd) {
        return
                RestUtil.getFromRestService(
                        getRequestUrl(),
                        getHeaders(userToken),
                        Collections.singletonMap(CHECK_CCD, checkCcd)
                );
    }

    private Response createACaseUpdateStateAndReturnTheCase(String userToken, String eventName) throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(userToken);

        return updateCase(null, caseId, eventName, userToken);
    }

    protected String getRequestUrl() {
        return serverUrl + retrieveCasePath;
    }
}

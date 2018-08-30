package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class RetrieveAosCaseTest extends PetitionSupport {
    private static final String TEST_AOS_COMPLETED_EVENT = "testAosCompleted";

    @Test
    public void givenAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheFirstCase() throws Exception {
        final String userToken = getUserToken(true);

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    private Response createACaseUpdateStateAndReturnTheCase(String userToken, String eventName) throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(userToken);

        return updateCase(null, caseId, eventName, userToken);
    }
}

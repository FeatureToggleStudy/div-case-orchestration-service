package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchDNPronouncedCases;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDNPronouncedCase;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEARCH_PAGE_KEY;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCasesWorklowTest {

    private static int DAYS_BEFORE_ELIGBLE_FOR_DA = 43;

    @Mock
    private SearchDNPronouncedCases searchDNPronouncedCases;

    @Mock
    private UpdateDNPronouncedCase updateDNPronouncedCase;

    @InjectMocks
    private UpdateDNPronouncedCasesWorkflow classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "awaitingDAPeriod", DAYS_BEFORE_ELIGBLE_FOR_DA);
    }

    @Test
    public void whenProcessAwaitingPronouncement_thenProcessAsExpected() throws WorkflowException {
        final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        final ImmutablePair<String, Integer> searchPageKeyPair = new ImmutablePair<>(SEARCH_PAGE_KEY, 0);
        final ImmutablePair<String, Integer> awaitingDAPeriodPair = new ImmutablePair<>(AWAITING_DA_PERIOD_KEY, DAYS_BEFORE_ELIGBLE_FOR_DA);


        final Task[] tasks = new Task[]{
            searchDNPronouncedCases,
            updateDNPronouncedCase
        };

        Map<String, Object> expected = Collections.emptyMap();

        when(classUnderTest.execute(tasks, null, authTokenPair, searchPageKeyPair, awaitingDAPeriodPair)).thenReturn(expected);

        Map<String, Object> actual = classUnderTest.run(AUTH_TOKEN);

        assertEquals(expected, actual);
    }
}

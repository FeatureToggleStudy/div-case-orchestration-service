package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToCaseDataTest {

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Rule
    public ExpectedException expectedException = none();

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = singletonMap("Hello", "World");
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void shouldAddSelectedCourtToPayload_AndCallCaseFormatterClientTransformToCCDFormat() throws TaskException {
        when(caseFormatterClient.transformToCCDFormat(eq(AUTH_TOKEN), any())).thenReturn(testData);
        context.setTransientObject("selectedCourt", "randomlySelectedCourt");

        assertEquals(testData, formatDivorceSessionToCaseData.execute(context, testData));

        verify(caseFormatterClient).transformToCCDFormat(eq(AUTH_TOKEN), argThat(payload -> {
            String selectedCourt = (String) payload.get("Hello");
            return "World".equals(selectedCourt);
        }));
        verify(caseFormatterClient).transformToCCDFormat(eq(AUTH_TOKEN), argThat(payload -> {
            String selectedCourt = (String) payload.get("courts");
            return "randomlySelectedCourt".equals(selectedCourt);
        }));
    }

    @Test
    public void shouldThrowExceptionIfCourtWasNotSelected() throws TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not find selected court.");

        assertEquals(testData, formatDivorceSessionToCaseData.execute(context, testData));
    }

}
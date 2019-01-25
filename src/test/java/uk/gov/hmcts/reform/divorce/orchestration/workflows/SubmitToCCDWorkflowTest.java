package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SubmitToCCDWorkflowTest {

    @Mock
    private CourtAllocationTask courtAllocationTask;//TODO - probably delete this class

    @Mock
    private CourtAllocator courtAllocator;

    @Mock
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Mock
    private ValidateCaseData validateCaseData;

    @Mock
    private SubmitCaseToCCD submitCaseToCCD;

    @Mock
    private DeleteDraft deleteDraft;

    @InjectMocks
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        ArgumentMatcher<TaskContext> contextCourtInfoMatcher = cxt -> {
            String selectedCourt = (String) cxt.getTransientObject("selectedCourt");
            return "randomlySelectedCourt".equals(selectedCourt);
        };//TODO - should this be a field?

        Map<String, Object> resultData = Collections.singletonMap("Hello", "World");
        Map<String, Object> incomingPayload = Collections.singletonMap("reasonForDivorce", "adultery");
        when(courtAllocator.selectCourtForGivenDivorceReason(eq("adultery"))).thenReturn("randomlySelectedCourt");
        when(courtAllocationTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(formatDivorceSessionToCaseData.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(validateCaseData.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(submitCaseToCCD.execute(any(), eq(incomingPayload))).thenReturn(resultData);
        when(deleteDraft.execute(any(), eq(resultData))).thenReturn(resultData);

        Map<String, Object> actual = submitToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        assertThat(actual, allOf(
                hasEntry("Hello", "World"),
                hasEntry("allocatedCourt", "randomlySelectedCourt")
        ));
        verify(courtAllocationTask).execute(argThat(contextCourtInfoMatcher), eq(incomingPayload));
        verify(formatDivorceSessionToCaseData).execute(argThat(contextCourtInfoMatcher), eq(incomingPayload));
        verify(validateCaseData).execute(argThat(contextCourtInfoMatcher), eq(incomingPayload));
        verify(submitCaseToCCD).execute(argThat(contextCourtInfoMatcher), eq(incomingPayload));
    }

}
package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDNPronouncedCasesWorkflow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class DecreeAbsoluteServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private DecreeAbsoluteServiceImpl classUnderTest;

    @Mock
    private UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow;

    @Mock
    private AuthUtil authUtil;


    @Test
    public void run_10CasesEligibleForDA_10CasesProcessed() throws WorkflowException {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        when(updateDNPronouncedCasesWorkflow.run(AUTH_TOKEN)).thenReturn(10);

        int casesProcessed = classUnderTest.enableCaseEligibleForDecreeAbsolute();

        assertEquals(10, casesProcessed);
        verify(updateDNPronouncedCasesWorkflow, times(1)).run(AUTH_TOKEN);
    }

    @Test
    public void run_throwsWorkflowException_workflowExceptionThrown() throws WorkflowException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage("a WorkflowException message");
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        when(updateDNPronouncedCasesWorkflow.run(AUTH_TOKEN)).thenThrow(new WorkflowException(" a WorkflowException message"));

        classUnderTest.enableCaseEligibleForDecreeAbsolute();
    }


}
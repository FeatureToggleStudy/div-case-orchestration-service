package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCaseJobTest {

    @Mock
    private CaseOrchestrationService orchestrationServiceMock;

    @Mock
    private JobExecutionContext jobExecutionContextMock;

    @InjectMocks
    private UpdateDNPronouncedCaseJob classToTest;

    @Test
    public void execute_updateToAwaitingDA_updateExecuted() throws JobExecutionException, WorkflowException {
        classToTest.execute(jobExecutionContextMock);
        verify(orchestrationServiceMock, times(1)).updateDNPronouncedCases();
    }

    @Test(expected = JobExecutionException.class)
    public void execute_updateToAwaitingDA_JobExceptionThrown() throws JobExecutionException, WorkflowException {
        doThrow(new WorkflowException("Workflow error")).when(orchestrationServiceMock).updateDNPronouncedCases();

        classToTest.execute(jobExecutionContextMock);
    }
}

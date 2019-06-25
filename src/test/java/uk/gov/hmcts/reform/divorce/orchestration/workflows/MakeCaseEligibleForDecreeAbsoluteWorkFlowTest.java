package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MakeCaseEligibleForDecreeAbsoluteWorkFlowTest {



    @Test
    public void testTasksAreCalledCorrectly() throws WorkflowException {
        UpdateCaseInCCD updateCaseMock = mock(UpdateCaseInCCD.class);
        MakeCaseEligibleForDecreeAbsoluteWorkFlow makeCaseEligibleForDecreeAbsoluteWorkFlow = new MakeCaseEligibleForDecreeAbsoluteWorkFlow(updateCaseMock);
        makeCaseEligibleForDecreeAbsoluteWorkFlow.run(null, null);

        verify(updateCaseMock).execute(any(), any());
    }

}
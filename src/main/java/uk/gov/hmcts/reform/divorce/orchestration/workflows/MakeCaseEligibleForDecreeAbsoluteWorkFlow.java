package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

@Component
public class MakeCaseEligibleForDecreeAbsoluteWorkFlow extends DefaultWorkflow<Map<String, Object>> {

    private UpdateCaseInCCD updateCaseInCCD;

    public MakeCaseEligibleForDecreeAbsoluteWorkFlow(UpdateCaseInCCD updateCaseInCCD) {

        this.updateCaseInCCD = updateCaseInCCD;
    }

    public Map<String, Object> run(String caseId, String authToken) throws WorkflowException {
        this.execute(
                new Task[]{
                        updateCaseInCCD
                },null
        );

        return null;
    }
}
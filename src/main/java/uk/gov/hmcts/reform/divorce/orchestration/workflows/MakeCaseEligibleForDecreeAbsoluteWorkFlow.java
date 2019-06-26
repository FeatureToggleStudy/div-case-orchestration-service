package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;

@Component
public class MakeCaseEligibleForDecreeAbsoluteWorkFlow extends DefaultWorkflow<Map<String, Object>> {

    private static final String MAKE_CASE_ELIGIBLE_FOR_DA_EVENT_ID = "makeCaseEligibleForDA";
    private UpdateCaseInCCD updateCaseInCCD;

    public MakeCaseEligibleForDecreeAbsoluteWorkFlow(UpdateCaseInCCD updateCaseInCCD) {
        this.updateCaseInCCD = updateCaseInCCD;
    }

    public Map<String, Object> run(String caseId, String authToken) throws WorkflowException {
        return this.execute(
            new Task[] {
                updateCaseInCCD
            },
            emptyMap(),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, MAKE_CASE_ELIGIBLE_FOR_DA_EVENT_ID)
        );
    }

}
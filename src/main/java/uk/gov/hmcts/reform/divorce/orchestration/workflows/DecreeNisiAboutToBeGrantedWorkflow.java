package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiGrantedDateToPayloadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class DecreeNisiAboutToBeGrantedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private AddDecreeNisiGrantedDateToPayloadTask addDecreeNisiGrantedDateToPayloadTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        List<Task> tasksToRun = new ArrayList<>();

        String newCaseEndState = AWAITING_CLARIFICATION;
        Object decreeNisiGranted = caseDetails.getCaseData().get(DECREE_NISI_GRANTED_CCD_FIELD);
        if (decreeNisiGranted.equals(YES_VALUE)) {
            tasksToRun.add(addDecreeNisiGrantedDateToPayloadTask);
            newCaseEndState = AWAITING_PRONOUNCEMENT;
        }

        Map<String, Object> payloadToReturn = this.execute(tasksToRun.stream().toArray(Task[]::new), caseDetails.getCaseData());
        payloadToReturn.put(STATE_CCD_FIELD, newCaseEndState);

        return payloadToReturn;
    }

}
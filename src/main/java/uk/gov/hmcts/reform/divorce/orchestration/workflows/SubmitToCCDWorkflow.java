package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class SubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private CourtAllocator courtAllocator;

    @Autowired
    private CourtAllocationTask courtAllocationTask;

    @Autowired
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Autowired
    private ValidateCaseData validateCaseData;

    @Autowired
    private SubmitCaseToCCD submitCaseToCCD;

    @Autowired
    private DeleteDraft deleteDraft;

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {
        String reasonForDivorce = (String) payload.get("reasonForDivorce");//TODO move to task...
        if (reasonForDivorce == null){
            throw new WorkflowException("Reason for divorce was not provided");
        }

        String selectedCourtId = courtAllocator.selectCourtForGivenDivorceReason(reasonForDivorce);//TODO - bring tests written for court allocation task
        //TODO - what happens if I don't find a court??

        Map<String, Object> returnFromExecution = this.execute(
                new Task[]{
                        courtAllocationTask,//TODO - is this order is ever changed, the solution will stop working - MVC test makes sure this never happens
                        formatDivorceSessionToCaseData,
                        validateCaseData,
                        submitCaseToCCD,
                        deleteDraft
                },
                payload,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of("selectedCourt", selectedCourtId)
        );

        HashMap<String, Object> response = new HashMap<>(returnFromExecution);
        response.put("allocatedCourt", selectedCourtId);

        return response;//TODO - maybe this is the place to get the data from the context and add it to the response
    }
}
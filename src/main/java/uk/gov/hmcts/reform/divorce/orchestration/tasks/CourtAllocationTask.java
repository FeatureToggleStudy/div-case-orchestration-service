package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocation;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CourtAllocationTask implements Task<Map<String, Object>> {

    //TODO - look into warning issued when running the whole test suite

    private static final String SELECTED_COURT_KEY = "courts";

    @Autowired
    private CourtAllocation courtAllocation;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        //TODO - good idea from Qiang - if a court is passed, then just use that court
        Optional<String> reasonForDivorce = Optional.ofNullable((String) payload.get("reasonForDivorce"));
        String selectedCourt = reasonForDivorce
                .map(courtAllocation::selectCourtRandomly)
                .orElseGet(courtAllocation::selectCourtRandomly);

        HashMap<String, Object> mapToReturn = new HashMap<>(payload);
        mapToReturn.put(SELECTED_COURT_KEY, selectedCourt);

        return mapToReturn;
    }

}
package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CourtAllocationTask implements Task<Map<String, Object>> {

    //TODO - look into warning issued when running the whole test suite

    private static final String SELECTED_COURT_KEY = "courts";//TODO - is this already somewhere else?

    @Autowired
    private DefaultCourtAllocator courtAllocation;//TODO - use interface here

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        //TODO - have logs here saying which court was allocated so that we can know that this has worked before removing code from PFE
        Optional<String> reasonForDivorce = Optional.ofNullable((String) payload.get("reasonForDivorce"));
        String selectedCourt = reasonForDivorce
                .map(courtAllocation::selectCourtForGivenDivorceReason)
                .orElseGet(courtAllocation::selectCourtRandomly);

        HashMap<String, Object> mapToReturn = new HashMap<>(payload);
        mapToReturn.put(SELECTED_COURT_KEY, selectedCourt);

        return mapToReturn;
    }

}
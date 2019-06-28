package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchDNPronouncedCases;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDNPronouncedCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;

@Component
@AllArgsConstructor
public class UpdateDNPronouncedCasesWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SearchDNPronouncedCases searchDNPronouncedCases;
    private final UpdateDNPronouncedCase updateDNPronouncedCase;

    @Value("${case.event.awaiting_da_period}")
    private final int awaitingDAPeriod;

    public Map<String, Object> run(String authToken) throws WorkflowException {

        return this.execute(
                new Task[]{
                    searchDNPronouncedCases,
                    updateDNPronouncedCase
                },
                null,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(SEARCH_PAGE_KEY, 0),
                ImmutablePair.of(AWAITING_DA_PERIOD_KEY, awaitingDAPeriod)
        );

    }
}

package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.UpdateDNPronouncedCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;

@Component
public class UpdateDNPronouncedCase extends AsyncTask<Map<String, Object>> {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> payload) {
        List<String> caseIds = (List<String>) payload.getOrDefault(BULK_CASE_LIST_KEY, Collections.emptyList());
        return  caseIds.stream()
            .map(caseId -> publishNewUpdateEvent(context, caseId))
            .collect(toList());
    }

    public UpdateDNPronouncedCaseEvent publishNewUpdateEvent(TaskContext context, String caseId) {
        UpdateDNPronouncedCaseEvent updateCaseEvent = new UpdateDNPronouncedCaseEvent(context, caseId);
        applicationEventPublisher.publishEvent(updateCaseEvent);
        return updateCaseEvent;
    }
}

package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.UpdateDNPronouncedCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

@Component
@Slf4j
public class UpdateDNPronouncedCaseEventListener implements ApplicationListener<UpdateDNPronouncedCaseEvent> {
    @Autowired
    private CaseOrchestrationService caseOrchestrationService;

    @Override
    public void onApplicationEvent(UpdateDNPronouncedCaseEvent event) {
        log.info("reached listener for UpdateDNPronouncedCaseEvent");
        try {
            caseOrchestrationService.makeCaseEligibleForDA(event.getAuthToken(), event.getCaseId());
        } catch (CaseOrchestrationServiceException e) {
            log.info("make case eligible for DA failed");
        }
    }
}

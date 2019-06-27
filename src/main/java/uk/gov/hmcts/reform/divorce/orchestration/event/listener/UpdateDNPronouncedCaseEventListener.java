package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.UpdateDNPronouncedCaseEvent;

@Component
@Slf4j
public class UpdateDNPronouncedCaseEventListener implements ApplicationListener<UpdateDNPronouncedCaseEvent> {
    @Override
    public void onApplicationEvent(UpdateDNPronouncedCaseEvent event) {
        log.info("reached listener for UpdateDNPronouncedCaseEvent");
    }
}

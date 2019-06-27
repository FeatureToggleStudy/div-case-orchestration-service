package uk.gov.hmcts.reform.divorce.orchestration.event;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class UpdateDNPronouncedCaseEvent extends ApplicationEvent {
    private final transient String caseId;

    public UpdateDNPronouncedCaseEvent(Object source, String caseId) {
        super(source);

        this.caseId = caseId;
    }
}

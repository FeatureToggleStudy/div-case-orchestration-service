package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCaseTest {
    @Mock
    private ApplicationEventPublisher applicationEventPublisherMock;

    @InjectMocks
    private UpdateDNPronouncedCase classToTest;

    @Test
    public void givenEmptyMap_whenGetEvents_thenReturnEmptyList() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> payload  = Collections.emptyMap();

        assertEquals(Collections.emptyList(), classToTest.getApplicationEvent(context, payload));
    }

    @Test
    public void givenListCase_thenReturnEventList() {
        TaskContext context = new DefaultTaskContext();
        String[] caseIds = {"someId1", "someId2"};

        Map<String, Object> payload  = ImmutableMap.of(BULK_CASE_LIST_KEY, Arrays.asList(caseIds));
        List<ApplicationEvent> result = classToTest.getApplicationEvent(context, payload);

        assertEquals(2, result.size());
    }

    @Test
    public void givenListCase_thenPublishEvents() {
        TaskContext context = new DefaultTaskContext();
        String[] caseIds = {"someId1", "someId2"};

        Map<String, Object> payload  = ImmutableMap.of(BULK_CASE_LIST_KEY, Arrays.asList(caseIds));
        classToTest.getApplicationEvent(context, payload);

        verify(applicationEventPublisherMock, times(2)).publishEvent(any());
    }
}

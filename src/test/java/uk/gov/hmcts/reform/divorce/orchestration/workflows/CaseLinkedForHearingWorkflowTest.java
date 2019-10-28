package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCertificateOfEntitlementNotificationEmailTask;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow.CASE_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseLinkedForHearingWorkflowTest {

    @Mock
    private SendPetitionerCertificateOfEntitlementNotificationEmailTask sendPetitionerCertificateOfEntitlementNotificationEmailTask;

    @Mock
    private SendRespondentCertificateOfEntitlementNotificationEmailTask sendRespondentCertificateOfEntitlementNotificationEmailTask;

    @Mock
    private SendCoRespondentCertificateOfEntitlementNotificationEmailTask sendCoRespondentCertificateOfEntitlementNotificationEmailTask;

    @InjectMocks
    private CaseLinkedForHearingWorkflow caseLinkedForHearingWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> contextCaptor;

    private Map<String, Object> testPayload = singletonMap("testKey", "testValue");

    @Before
    public void setUp() throws TaskException {
        when(sendPetitionerCertificateOfEntitlementNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendRespondentCertificateOfEntitlementNotificationEmailTask.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendCoRespondentCertificateOfEntitlementNotificationEmailTask.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);
    }

    @Test
    public void testRightTasksAreCalledWithTheRightParameters() throws TaskException, WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId("testCaseId")
            .caseData(testPayload)
            .build();

        Map<String, Object> returnedPayload = caseLinkedForHearingWorkflow.run(caseDetails);

        assertThat(returnedPayload, is(equalTo(testPayload)));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask)
                .execute(contextCaptor.capture(), eq(caseDetails.getCaseData()));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask)
                .execute(contextCaptor.capture(), eq(caseDetails.getCaseData()));
        verify(sendCoRespondentCertificateOfEntitlementNotificationEmailTask)
                .execute(contextCaptor.capture(), eq(caseDetails.getCaseData()));

        assertThat(contextCaptor.getValue().getTransientObject(CASE_ID_KEY), is(equalTo("testCaseId")));

    }
}

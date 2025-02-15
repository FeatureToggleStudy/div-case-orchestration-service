package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.AosPackOfflineAnswersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.IssueAosPackOfflineWorkflow;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class AosPackOfflineServiceImplTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private IssueAosPackOfflineWorkflow issueAosPackOfflineWorkflow;

    @Mock
    private AosPackOfflineAnswersWorkflow aosPackOfflineAnswersWorkflow;

    @InjectMocks
    private AosPackOfflineServiceImpl classUnderTest;

    private String testAuthToken;
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        testAuthToken = "testAuthToken";
        caseDetails = CaseDetails.builder().caseData(emptyMap()).build();
    }

    //issueAosPackOffline
    @Test
    public void testWorkflowIsCalledWithRightParams() throws WorkflowException, CaseOrchestrationServiceException {
        Map<String, Object> returnValue = singletonMap("returnedKey", "returnedValue");
        when(issueAosPackOfflineWorkflow.run(any(), any(), any())).thenReturn(returnValue);

        Map<String, Object> result = classUnderTest.issueAosPackOffline(testAuthToken, caseDetails, RESPONDENT);

        assertThat(result, equalTo(returnValue));
        verify(issueAosPackOfflineWorkflow).run(eq(testAuthToken), eq(caseDetails), eq(RESPONDENT));
    }

    @Test
    public void shouldCallWorkflowForCoRespondentIfReasonIsAdultery() throws WorkflowException, CaseOrchestrationServiceException {
        Map<String, Object> caseData = singletonMap(D_8_REASON_FOR_DIVORCE, ADULTERY);
        caseDetails.setCaseData(caseData);
        when(issueAosPackOfflineWorkflow.run(any(), any(), any())).thenReturn(caseData);

        Map<String, Object> result = classUnderTest.issueAosPackOffline(testAuthToken, caseDetails, CO_RESPONDENT);

        assertThat(result, equalTo(caseData));
        verify(issueAosPackOfflineWorkflow).run(eq(testAuthToken), eq(caseDetails), eq(CO_RESPONDENT));
    }

    @Test
    public void shouldNotCallWorkflowForCoRespondentIfReasonIsNotAdultery() throws CaseOrchestrationServiceException {
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(format("Co-respondent AOS pack (offline) cannot be issued for reason \"%s\"", SEPARATION_TWO_YEARS));

        caseDetails.setCaseData(singletonMap(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS));

        classUnderTest.issueAosPackOffline(testAuthToken, caseDetails, CO_RESPONDENT);
    }

    @Test
    public void shouldThrowServiceException() throws WorkflowException, CaseOrchestrationServiceException {
        when(issueAosPackOfflineWorkflow.run(any(), any(), any())).thenThrow(WorkflowException.class);
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectCause(instanceOf(WorkflowException.class));

        CaseDetails caseDetails = CaseDetails.builder().caseId("123456789").build();
        classUnderTest.issueAosPackOffline(null, caseDetails, RESPONDENT);
    }

    //processAosPackOfflineAnswers
    @Test
    public void shouldCallWorkflowAndReturnPayload() throws WorkflowException, CaseOrchestrationServiceException {
        when(aosPackOfflineAnswersWorkflow.run(any(), any())).thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> incomingPayload = singletonMap("incomingKey", "incomingValue");
        Map<String, Object> returnedPayload = classUnderTest.processAosPackOfflineAnswers(incomingPayload, RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
        verify(aosPackOfflineAnswersWorkflow).run(eq(incomingPayload), eq(RESPONDENT));
    }

    @Test
    public void shouldThrowServiceException_WhenWorkflowExceptionIsThrown() throws WorkflowException, CaseOrchestrationServiceException {
        when(aosPackOfflineAnswersWorkflow.run(any(), any())).thenThrow(WorkflowException.class);
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectCause(instanceOf(WorkflowException.class));

        classUnderTest.processAosPackOfflineAnswers(emptyMap(), RESPONDENT);
    }

}
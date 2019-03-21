package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_ANSWERS_REPORT_FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MINI_PETITION_FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWERS_REPORT_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_ADMITS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_AGREES_TO_COSTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_COSTS_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS_REPORT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;


@RunWith(MockitoJUnitRunner.class)
public class CoRespondentAnswersReportGeneratorTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private CoRespondentAnswersReportGenerator classUnderTest;

    @Test
    public void exceptionThrownIfCaseNotFound() throws TaskException {
        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, true)).thenReturn(null);

        expectedException.expect(TaskException.class);
        expectedException.expectCause(allOf(
            instanceOf(CaseNotFoundException.class),
            hasProperty("message", is("No case found for user."))));

        classUnderTest.execute(taskContext, null);
    }

    @Test
    public void documentGeneratorIsCalledWithCorrectValues() throws TaskException {
        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(D_8_CASE_REFERENCE, "a");
        existingCaseData.put(D_8_PETITIONER_FIRST_NAME, "b");
        existingCaseData.put(D_8_PETITIONER_LAST_NAME, "c");
        existingCaseData.put(RESP_FIRST_NAME_CCD_FIELD, "d");
        existingCaseData.put(RESP_LAST_NAME_CCD_FIELD, "e");
        existingCaseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, "f");
        existingCaseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, "g");

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(existingCaseData)
            .build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, true)).thenReturn(caseDetails);

        final GeneratedDocumentInfo coRespondentAnswersReport = GeneratedDocumentInfo.builder().build();

        final Map<String, Object> coRespondentAnswers = new HashMap<>();
        coRespondentAnswers.put(CO_RESPONDENT_DEFENDS_DIVORCE, "h");
        coRespondentAnswers.put(CO_RESP_ADMITS_ADULTERY, "i");
        coRespondentAnswers.put(CO_RESP_AGREES_TO_COSTS, "j");
        coRespondentAnswers.put(CO_RESP_COSTS_REASON, "k");

        final Map<String, Object> combinedParameters = new HashMap<>();
        combinedParameters.putAll(existingCaseData);
        combinedParameters.putAll(coRespondentAnswers);

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                .template(CO_RESPONDENT_ANSWERS_REPORT_TEMPLATE_NAME)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, combinedParameters))
                .build();

        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN)).thenReturn(coRespondentAnswersReport);

        classUnderTest.execute(taskContext, coRespondentAnswers);

        GeneratedDocumentInfo response = (GeneratedDocumentInfo)taskContext.getTransientObject(CO_RESPONDENT_ANSWERS_REPORT_TEMPLATE_NAME);

        assertThat(response.getDocumentType(), is(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS_REPORT));
        assertThat(response.getFileName(), is(TEST_CO_RESPONDENT_ANSWERS_REPORT_FILE_NAME));

        verify(documentGeneratorClient).generatePDF(generateDocumentRequest, AUTH_TOKEN);
    }
}

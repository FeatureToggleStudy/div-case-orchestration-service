package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWERS_REPORT_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWERS_REPORT_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_ADMITS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_AGREES_TO_COSTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_COSTS_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS_REPORT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@Component
public class CoRespondentAnswersReportGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;
    private final CaseMaintenanceClient cmsClient;

    @Autowired
    public CoRespondentAnswersReportGenerator(final DocumentGeneratorClient documentGeneratorClient, final CaseMaintenanceClient cmsClient) {
        this.documentGeneratorClient = documentGeneratorClient;
        this.cmsClient = cmsClient;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> coRespondentAnswers) throws TaskException {
        final String authToken =  (String)context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        final CaseDetails currentCaseDetails = cmsClient.retrieveAosCase(authToken, true);

        if (currentCaseDetails == null) {
            throw new TaskException(new CaseNotFoundException("No case found for user."));
        }

        final Map<String, Object> documentValues = getDocumentTemplateData(currentCaseDetails.getCaseData(), coRespondentAnswers);

        final GeneratedDocumentInfo coRespondentAnswersReport =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(CO_RESPONDENT_ANSWERS_REPORT_TEMPLATE_NAME)
                    .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, documentValues))
                    .build(), authToken);

        coRespondentAnswersReport.setDocumentType(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS_REPORT);
        coRespondentAnswersReport.setFileName(String.format(CO_RESPONDENT_ANSWERS_REPORT_FILE_NAME_FORMAT, currentCaseDetails.getCaseId()));

        context.setTransientObject(CO_RESPONDENT_ANSWERS_REPORT_TEMPLATE_NAME, coRespondentAnswersReport);

        return coRespondentAnswers;
    }

    private Map<String, Object> getDocumentTemplateData(final Map<String, Object> caseData, final Map<String, Object> coRespondentAnswers) {
        final Map<String, Object> documentValues = new HashMap<>();

        // Get some dat from the case
        documentValues.put(D_8_CASE_REFERENCE, caseData.get(D_8_CASE_REFERENCE));
        documentValues.put(D_8_PETITIONER_FIRST_NAME, caseData.get(D_8_PETITIONER_FIRST_NAME));
        documentValues.put(D_8_PETITIONER_LAST_NAME, caseData.get(D_8_PETITIONER_LAST_NAME));
        documentValues.put(RESP_FIRST_NAME_CCD_FIELD, caseData.get(RESP_FIRST_NAME_CCD_FIELD));
        documentValues.put(RESP_LAST_NAME_CCD_FIELD, caseData.get(RESP_LAST_NAME_CCD_FIELD));
        documentValues.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME));
        documentValues.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME));

        // Get some data from teh co-respondents submission (not on the case yet).
        documentValues.put(CO_RESPONDENT_DEFENDS_DIVORCE, coRespondentAnswers.get(CO_RESPONDENT_DEFENDS_DIVORCE));
        documentValues.put(CO_RESP_ADMITS_ADULTERY, coRespondentAnswers.get(CO_RESP_ADMITS_ADULTERY));
        documentValues.put(CO_RESP_AGREES_TO_COSTS, coRespondentAnswers.get(CO_RESP_AGREES_TO_COSTS));
        documentValues.put(CO_RESP_COSTS_REASON, coRespondentAnswers.get(CO_RESP_COSTS_REASON));

        return documentValues;
    }
}

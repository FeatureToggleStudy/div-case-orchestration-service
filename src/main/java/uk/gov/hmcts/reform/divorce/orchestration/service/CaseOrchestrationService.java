package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseOrchestrationService {

    Map<String, Object> ccdCallbackHandler(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException;

    Boolean authenticateRespondent(String authToken) throws WorkflowException;

    Map<String, Object> submit(Map<String, Object> divorceSession, String authToken) throws WorkflowException;

    Map<String, Object> update(Map<String, Object> divorceEventSession,
                               String authToken, String caseId) throws WorkflowException;

    Map<String,Object> ccdRetrieveCaseDetailsHandler(boolean checkCcd, String authorizationToken);
}

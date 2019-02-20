package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;

public abstract class RetrievePinUserDetails implements Task<UserDetails> {
    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payLoad) throws TaskException {
        String pinCode = authenticatePinUser(
            String.valueOf(context.getTransientObject(PIN)),
            authClientId,
            authRedirectUrl);

        String pinAuthToken = authUtil.getBearToken(
            getIdamClient().exchangeCode(
                pinCode,
                AUTHORIZATION_CODE,
                authRedirectUrl,
                authClientId,
                authClientSecret
            ).getAccessToken()
        );

        UserDetails pinUserDetails = getIdamClient().retrieveUserDetails(pinAuthToken);

        if (pinUserDetails == null) {
            throw new TaskException(new AuthenticationError("Invalid pin"));
        }

        final String letterHolderId = pinUserDetails.getId();
        final Map<String, Object> caseData = (HashMap) context.getTransientObject(CCD_CASE_DATA);
        final String coRespondentLetterHolderId = (String) caseData.get(CO_RESPONDENT_LETTER_HOLDER_ID);
        final boolean isRespondent = !letterHolderId.equals(coRespondentLetterHolderId);
        context.setTransientObject(IS_RESPONDENT, isRespondent);

        if (isRespondent) {
            context.setTransientObject(RESPONDENT_LETTER_HOLDER_ID, pinUserDetails.getId());
        } else {
            context.setTransientObject(CO_RESPONDENT_LETTER_HOLDER_ID, pinUserDetails.getId());
        }

        return pinUserDetails;
    }

    protected abstract String authenticatePinUser(String pin, String authClientId, String authRedirectUrl)
        throws TaskException;

    protected abstract IdamClient getIdamClient();
}

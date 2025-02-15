package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Component
public class PopulateDocLink implements Task<Map<String, Object>>  {

    private static final String VALUE = "value";
    private static final String DOCUMENT_TYPE_KEY = "DocumentType";
    private static final String DOCUMENT_LINK_KEY = "DocumentLink";

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        List<Map> documentList =
                ofNullable(payload.get(D8DOCUMENTS_GENERATED)).map(i -> (List<Map>) i).orElse(new ArrayList<>());

        String documentType = context.getTransientObject(DOCUMENT_TYPE);
        String docLinkFieldName = context.getTransientObject(DOCUMENT_DRAFT_LINK_FIELD);
        if (StringUtils.isNotBlank(documentType)) {
            Map<String, Object> petitionDocument = documentList
                .stream()
                .filter(map -> documentType.equals(((Map) map.get(VALUE)).get(DOCUMENT_TYPE_KEY)))
                .findFirst()
                .orElseThrow(() -> new TaskException(documentType + " document not found"));

            Map<String, Object> documentLink = (Map<String, Object>) ((Map) petitionDocument.get(VALUE)).get(DOCUMENT_LINK_KEY);
            payload.put(docLinkFieldName, documentLink);
        }
        return payload;
    }
}

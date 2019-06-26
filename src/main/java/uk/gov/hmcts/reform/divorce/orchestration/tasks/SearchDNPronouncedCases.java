package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_APPROVAL_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;

@Component
public class SearchDNPronouncedCases implements Task<Map<String, Object>> {

    // TODO dateField reference needs to match CCD - most likely DN_APPROVAL_DATE_CCD_FIELD
    private static String DN_APPROVAL_DATE = String.format("data.%s", DN_APPROVAL_DATE_CCD_FIELD);

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SearchDNPronouncedCases(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {

        List<String> searchResultCaseIds = new ArrayList<>();
        int from = 0;
        int totalNumberSearchResult;
        String coolOffPeriodInDN = "43d";

        do {
            // TODO dateField reference needs to match CCD - most likely DN_APPROVAL_DATE_CCD_FIELD
            QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED);
            QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_APPROVAL_DATE).lte("now/d-" + coolOffPeriodInDN);

            QueryBuilder query = QueryBuilders
                .boolQuery()
                .filter(stateQuery)
                .filter(dateFilter);

            SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                .searchSource()
                .query(query)
                .from(from);

            SearchResult result = caseMaintenanceClient.searchCases(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                sourceBuilder.toString()
            );

            from += result.getCases().size();

            totalNumberSearchResult = result.getTotal();
            if (!result.getCases().isEmpty()) {
                searchResultCaseIds = result.getCases().stream()
                    .map(CaseDetails::getCaseId)
                    .collect(Collectors.toList());
            }
        }
        while (from < totalNumberSearchResult);
        // TODO Do we use SERCH_RESULT_KEY if it refers to Bulk case ?
        context.setTransientObject(SEARCH_RESULT_KEY, searchResultCaseIds);
        return payload;
    }
}

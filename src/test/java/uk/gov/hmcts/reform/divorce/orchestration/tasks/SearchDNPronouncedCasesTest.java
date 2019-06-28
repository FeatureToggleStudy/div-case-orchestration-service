package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SearchDNPronouncedCasesTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SearchDNPronouncedCases classUnderTest;


    // dynamic then return
    // List https://static.javadoc.io/org.mockito/mockito-core/2.6.8/org/mockito/AdditionalAnswers.html#returnsElementsOf(java.util.Collection)
    // List<> someListOFReturnVals = Arrays.asList(..)
    // when(sosmething.method()).thenAnswer(AdditionalAnswers.returnsElementsOf(someListOfReturnVals));
    // i-th invocation of method() returns i-th element of someListOfReturnVals

    @Test
    public void execute_pageSize10_totalResults5() throws TaskException {

        final int pageSize = 10;
        final int start = 0;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {{
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
                put(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
            }};

        final DefaultTaskContext contextBeingModified = DefaultTaskContext.builder().transientObjects(searchSettings).build();

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5");

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(5)
                .cases(buildCases(0,5))
                .build());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
    }

    @Test
    public void execute_pageSize10_totalResults10() throws TaskException {

        final int pageSize = 10;
        final int start = 0;
        final int totalSearchResults = 10;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {{
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
                put(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
            }};

        final DefaultTaskContext contextBeingModified = DefaultTaskContext.builder().transientObjects(searchSettings).build();

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,10))
                .build());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
    }

    @Test
    public void execute_pageSize10_totalResults20() throws TaskException {

        final int pageSize = 10;
        final int start = 0;
        final int totalSearchResults = 20;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {{
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
                put(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
            }};

        final DefaultTaskContext contextBeingModified = DefaultTaskContext.builder().transientObjects(searchSettings).build();

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20");

        List<SearchResult> testVals = Arrays.asList(
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(10,10))
                .build()
            );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(testVals));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
    }

    @Test
    public void execute_pageSize10_totalResults30() throws TaskException {

        final int pageSize = 10;
        final int start = 0;
        final int totalSearchResults = 30;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {{
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
                put(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
            }};

        final DefaultTaskContext contextBeingModified = DefaultTaskContext.builder().transientObjects(searchSettings).build();

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30");

        List<SearchResult> testVals = Arrays.asList(
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(10,10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(20,10))
                .build()
        );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(testVals));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
    }

    private List<CaseDetails> buildCases(int startId, int caseCount) {
        final List<CaseDetails> cases = new ArrayList<>();

        for (int i = 0; i < caseCount; i++) {
            cases.add(buildCase(startId + 1));
            startId++;
        }
        return cases;
    }

    private CaseDetails buildCase(int caseId) {
        return CaseDetails.builder()
            .caseId(String.valueOf(caseId))
            .build();
    }
}

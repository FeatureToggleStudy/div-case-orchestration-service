package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;

@RunWith(MockitoJUnitRunner.class)
public class SearchDNPronouncedCasesTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SearchDNPronouncedCases classUnderTest;
}

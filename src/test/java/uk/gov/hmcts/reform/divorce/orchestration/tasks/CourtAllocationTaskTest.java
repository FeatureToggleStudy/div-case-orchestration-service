package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtAllocationTaskTest {

    private static final String SELECTED_COURT_KEY = "courts";
    private static final String REASON_FOR_DIVORCE_KEY = "reasonForDivorce";

    @Mock
    private DefaultCourtAllocator courtAllocation;//TODO - maybe use interface here?

    @InjectMocks
    private CourtAllocationTask courtAllocationTask;

    //TODO - test configuration from environment variable

    @Before
    public void setUp(){
        when(courtAllocation.selectCourtForGivenDivorceReason(eq("testReason"))).thenReturn("selectedCourtForReason");
        when(courtAllocation.selectCourtRandomly()).thenReturn("randomlySelectedCourt");
    }

    @Test
    public void shouldReturnSelectedCourtAsPartOfIncomingMap() {
        HashMap incomingMap = new HashMap<>();
        incomingMap.put("firstKey", "firstValue");
        incomingMap.put(REASON_FOR_DIVORCE_KEY, "testReason");

        Map<String, Object> outgoingMap = courtAllocationTask.execute(null, incomingMap);

        assertThat(outgoingMap, allOf(
                hasEntry(is("firstKey"), is("firstValue")),
                hasEntry(is("reasonForDivorce"), is("testReason")),
                hasEntry(is(SELECTED_COURT_KEY), is("selectedCourtForReason"))
        ));
    }

    @Test
    public void shouldOverwriteSelectedCourtFromIncomingMap() {
        HashMap incomingMap = new HashMap<>();
        incomingMap.put("firstKey", "firstValue");
        incomingMap.put(REASON_FOR_DIVORCE_KEY, "testReason");
        incomingMap.put(SELECTED_COURT_KEY, "previouslySelectedCourt");

        Map<String, Object> outgoingMap = courtAllocationTask.execute(null, incomingMap);

        assertThat(outgoingMap, allOf(
                hasEntry(is("firstKey"), is("firstValue")),
                hasEntry(is(SELECTED_COURT_KEY), is("selectedCourtForReason"))
        ));
    }

    @Test
    public void shouldRandomlySelectCourtEvenWithoutReasonForDivorce() {
        HashMap incomingMap = new HashMap<>();
        incomingMap.put("firstKey", "firstValue");

        Map<String, Object> outgoingMap = courtAllocationTask.execute(null, incomingMap);

        assertThat(outgoingMap, allOf(
                hasEntry(is("firstKey"), is("firstValue")),
                hasEntry(is(SELECTED_COURT_KEY), is("randomlySelectedCourt"))
        ));
    }

}
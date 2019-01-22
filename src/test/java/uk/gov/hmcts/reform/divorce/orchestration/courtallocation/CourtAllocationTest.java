package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CourtAllocationTest {

    private final BigDecimal errorMargin = new BigDecimal("0.005");
    private final String[] courts = new String[]{"eastMidlands", "westMidlands", "southWest", "northWest", "serviceCentre"};
    private final CourtAllocation courtAllocation = new CourtAllocation(courts);

    //TODO - test that weighted randomisation works with a given set of courts
    @Test
    public void shouldApplyRandomWeightedSelectionToCourts() {
        int numberOfAttempts = 1000000;
        BigDecimal expectedTimesCourtWasChosen = new BigDecimal(numberOfAttempts / courts.length);
        BigDecimal acceptableError = errorMargin.multiply(expectedTimesCourtWasChosen);

        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < numberOfAttempts; i++) {
            String selectedCourt = courtAllocation.selectCourtRandomly();
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        assertThat(courtsDistribution.getOrDefault("eastMidlands", ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("westMidlands", ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("southWest", ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("northWest", ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("serviceCentre", ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
    }

}
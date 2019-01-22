package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CourtAllocationTest {

    //TODO - test that weighted randomisation works with a given set of courts
    @Test
    public void shouldApplyRandomWeightedSelectionToCourts() {
        String[] courts = new String[]{"eastMidlands", "westMidlands", "southWest", "northWest", "serviceCentre"};
        CourtAllocation courtAllocation = new CourtAllocation(courts);

        //TODO - Run this 1M times and check that statistics are as expected
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();//TODO - could I use a better data structure
        int numberOfAttempts = 1000000;
        for (int i = 0; i < numberOfAttempts; i++) {
            String selectedCourt = courtAllocation.selectCourtRandomly();//TODO - is this the best name for this method?

            BigDecimal counter = courtsDistribution.getOrDefault(selectedCourt, BigDecimal.ZERO);
            courtsDistribution.put(selectedCourt, counter.add(BigDecimal.ONE));
        }

        BigDecimal errorMargin = new BigDecimal("0.005");

        BigDecimal expectedTimesCourtWasChosen = new BigDecimal(numberOfAttempts / courts.length);
        BigDecimal acceptableError = errorMargin.multiply(expectedTimesCourtWasChosen);

        assertThat(courtsDistribution.getOrDefault("eastMidlands", BigDecimal.ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("westMidlands", BigDecimal.ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("southWest", BigDecimal.ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("northWest", BigDecimal.ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
        assertThat(courtsDistribution.getOrDefault("serviceCentre", BigDecimal.ZERO), closeTo(expectedTimesCourtWasChosen, acceptableError));
    }

}
package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CourtAllocationTest {

    private final BigDecimal errorMargin = new BigDecimal("0.005");
    private final CourtWeight[] courts = new CourtWeight[]{
            new CourtWeight("eastMidlands", 1),
            new CourtWeight("westMidlands", 1),
            new CourtWeight("southWest", 1),
            new CourtWeight("northWest", 1),
            new CourtWeight("serviceCentre", 2)
    };
    private final CourtAllocation courtAllocation = new CourtAllocation(courts);

    @Test
    public void shouldApplyRandomWeightedSelectionToCourts() {
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);
        BigDecimal sumOfWeightPoints = new BigDecimal(Arrays.stream(courts).mapToInt(CourtWeight::getWeight).sum());//TODO - look for more sophisticated way to sum bigdecimals. maybe implement this in production class

        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = courtAllocation.selectCourtRandomly();
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert
        //TODO - try extracting this to method, but I think it won't look good
        for (CourtWeight courtWeight : courts) {
            BigDecimal individualCourtWeight = BigDecimal.valueOf(courtWeight.getWeight());
            BigDecimal expectedTimesCourtWasChosen = totalNumberOfAttempts.divide(sumOfWeightPoints, RoundingMode.CEILING).multiply(individualCourtWeight);

            BigDecimal acceptableError = errorMargin.multiply(expectedTimesCourtWasChosen);

            BigDecimal timesCourtWasChosen = courtsDistribution.get(courtWeight.getCourtName());//TODO - separate method?
            assertThat(String.format("Court %s was not selected near enough times to how much it was expected to have been.", courtWeight.getCourtName()),
                    timesCourtWasChosen,
                    closeTo(expectedTimesCourtWasChosen, acceptableError));
        }
    }

}
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
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.generateDivorceCaseJsonWithReason;

public class CourtAllocationTest {

    private final BigDecimal acceptedDeviation = new BigDecimal("0.005");

    @Test
    public void shouldApplyRandomWeightedSelectionToCourts() {
        CourtWeight[] courts = new CourtWeight[]{
                new CourtWeight("eastMidlands", 1),
                new CourtWeight("westMidlands", 1),
                new CourtWeight("southWest", 1),
                new CourtWeight("northWest", 1),
                new CourtWeight("serviceCentre", 2)
        };
        CourtAllocation courtAllocation = new CourtAllocation(courts);
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);

        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = courtAllocation.selectCourtRandomly();
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert randomisation works as expected
        BigDecimal sumOfWeightPoints = Arrays.stream(courts).map(CourtWeight::getWeight)//TODO - is there any good reason to have this as array, not List? - could make streams easier to use if it's a List
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (CourtWeight courtWeight : courts) {
            BigDecimal individualCourtWeight = BigDecimal.valueOf(courtWeight.getWeight());
            BigDecimal expectedTimesCourtWasChosen = totalNumberOfAttempts.divide(sumOfWeightPoints, RoundingMode.CEILING).multiply(individualCourtWeight);
            BigDecimal acceptableError = acceptedDeviation.multiply(expectedTimesCourtWasChosen);

            BigDecimal timesCourtWasChosen = courtsDistribution.get(courtWeight.getCourtName());//TODO - separate method?
            assertThat(String.format("Court %s was not selected near enough times to how much it was expected to have been.", courtWeight.getCourtName()),
                    timesCourtWasChosen,
                    closeTo(expectedTimesCourtWasChosen, acceptableError));
        }
    }

    @Test
    public void shouldAssignToSpecificCourtIfReasonForDivorceIsSpecified_OrRandomlyChoseCourtsForUnspecifiedReasons() {
        CourtAllocation courtAllocation = new CourtAllocation(
                new CourtAllocationPerReason[]{
                        new CourtAllocationPerReason("northWest", "adultery"),
                        new CourtAllocationPerReason("serviceCentre", "desertion")//TODO - should I check for no court repetition?
                },
                new CourtWeight[]{
                        new CourtWeight("eastMidlands", 1),
                        new CourtWeight("westMidlands", 1),
                        new CourtWeight("southWest", 1)
                }
        );

        String adulteryCase = generateDivorceCaseJsonWithReason("adultery");
        String desertionCase = generateDivorceCaseJsonWithReason("desertion");
        String unreasonableBehaviourCase = generateDivorceCaseJsonWithReason("unreasonable-behaviour");

        assertThat(courtAllocation.selectCourtRandomly(adulteryCase), is("northWest"));
        assertThat(courtAllocation.selectCourtRandomly(desertionCase), is("serviceCentre"));
        assertThat(courtAllocation.selectCourtRandomly(unreasonableBehaviourCase), isOneOf("eastMidlands",
                "westMidlands",
                "southWest")
        );
    }

}
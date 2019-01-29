package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;

public class DefaultCourtAllocatorTest {

    private final BigDecimal acceptedDeviation = new BigDecimal("0.005");

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldApplyRandomWeightedSelectionToCourts() {
        List<CourtWeight> courts = asList(
                new CourtWeight("eastMidlands", 1),
                new CourtWeight("westMidlands", 1),
                new CourtWeight("southWest", 1),
                new CourtWeight("northWest", 1),
                new CourtWeight("serviceCentre", 2)
        );
        CourtAllocator courtAllocator = new DefaultCourtAllocator(courts);

        //Select court 1 million times
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = courtAllocator.selectCourtForGivenDivorceReason(Optional.empty());
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert randomisation works as expected
        BigDecimal sumOfWeightPoints = courts.stream()
                .map(CourtWeight::getWeight)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (CourtWeight courtWeight : courts) {
            BigDecimal individualCourtWeight = BigDecimal.valueOf(courtWeight.getWeight());
            BigDecimal expectedTimesCourtWasChosen = totalNumberOfAttempts.divide(sumOfWeightPoints, RoundingMode.CEILING).multiply(individualCourtWeight);
            BigDecimal acceptableError = acceptedDeviation.multiply(expectedTimesCourtWasChosen);

            BigDecimal timesCourtWasChosen = courtsDistribution.get(courtWeight.getCourtId());
            assertThat(String.format("Court %s was not selected near enough times to how much it was expected to have been.", courtWeight.getCourtId()),
                    timesCourtWasChosen,
                    closeTo(expectedTimesCourtWasChosen, acceptableError));
        }
    }

    @Test
    public void shouldAssignToSpecificCourtIfReasonForDivorceIsSpecified_OrRandomlyChoseCourtsForUnspecifiedReasons() {
        CourtAllocator courtAllocator = new DefaultCourtAllocator(
                asList(
                        new CourtAllocationPerReason("northWest", "adultery"),
                        new CourtAllocationPerReason("serviceCentre", "desertion")
                ),
                asList(
                        new CourtWeight("eastMidlands", 1),
                        new CourtWeight("westMidlands", 1),
                        new CourtWeight("southWest", 1)
                )
        );

        String courtForAdulteryReason = courtAllocator.selectCourtForGivenDivorceReason(Optional.of("adultery"));
        String courtForDesertionReason = courtAllocator.selectCourtForGivenDivorceReason(Optional.of("desertion"));
        String courtForUnreasonableBehaviourReason = courtAllocator.selectCourtForGivenDivorceReason(Optional.of("unreasonable-behaviour"));

        assertThat(courtForAdulteryReason, is("northWest"));
        assertThat(courtForDesertionReason, is("serviceCentre"));
        assertThat(courtForUnreasonableBehaviourReason, isOneOf("eastMidlands", "westMidlands", "southWest"));
    }

    @Test
    public void shouldThrowExceptionIfReasonIsDuplicate_InAllocationPerReason() {
        expectedException.expect(IllegalStateException.class);

        new DefaultCourtAllocator(
                asList(
                        new CourtAllocationPerReason("northWest", "adultery"),
                        new CourtAllocationPerReason("southWest", "adultery")
                ),
                asList(
                        new CourtWeight("eastMidlands", 1),
                        new CourtWeight("westMidlands", 1),
                        new CourtWeight("southWest", 1)
                )
        );
    }

}
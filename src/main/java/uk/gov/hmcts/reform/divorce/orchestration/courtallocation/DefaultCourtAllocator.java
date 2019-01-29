package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class DefaultCourtAllocator implements CourtAllocator {

    /*
     * Each court will have a raffle ticket based on its attributed weight
     */
    private final String[] raffleTicketsPerCourt;

    private Map<String, String> courtPerReasonForDivorce = new HashMap<>();

    private static final Random random = new Random();

    public DefaultCourtAllocator(List<CourtWeight> courts) {
        this.raffleTicketsPerCourt = courts.stream()
                .flatMap(this::returnAdequateAmountOfRaffleTicketsPerCourt)
                .map(CourtWeight::getCourtId)
                .toArray(String[]::new);
    }

    public DefaultCourtAllocator(List<CourtAllocationPerReason> courtAllocationsPerReason, List<CourtWeight> courts) {
        this(courts);

        courtPerReasonForDivorce = courtAllocationsPerReason.stream().collect(toMap(
            CourtAllocationPerReason::getDivorceReason,
            CourtAllocationPerReason::getCourtId
        ));
    }

    @Override
    public String selectCourtForGivenDivorceReason(Optional<String> reasonForDivorce) {
        return reasonForDivorce
                .map(courtPerReasonForDivorce::get)
                .orElseGet(this::selectCourtRandomly);
    }

    private String selectCourtRandomly() {
        int randomIndex = random.nextInt(raffleTicketsPerCourt.length);
        return raffleTicketsPerCourt[randomIndex];
    }

    private Stream<? extends CourtWeight> returnAdequateAmountOfRaffleTicketsPerCourt(CourtWeight courtWeight) {
        ArrayList<CourtWeight> courtsList = new ArrayList<>();

        for (int i = 0; i < courtWeight.getWeight(); i++) {
            courtsList.add(courtWeight);
        }

        return courtsList.stream();
    }

}
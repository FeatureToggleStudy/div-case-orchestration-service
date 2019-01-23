package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CourtAllocation {

    /*
     * Each court will have a raffle ticket based on its attributed weight
     */
    private final String[] raffleTicketsPerCourt;

    private Map<String, String> courtPerReasonForDivorce;

    private static final Random random = new Random();

    public CourtAllocation(CourtWeight[] courts) {//TODO - varargs?
        //TODO - any good reasons for using Array not List?
        this.raffleTicketsPerCourt = Arrays.stream(courts)
                .flatMap(this::returnAdequateAmountOfRaffleTicketsPerCourt)
                .map(CourtWeight::getCourtName)
                .toArray(String[]::new);
    }

    public CourtAllocation(CourtAllocationPerReason[] courtAllocationsPerReason, CourtWeight[] courts) {
        this(courts);

        //TODO - any good reasons for using Array not List?
        courtPerReasonForDivorce = Arrays.stream(courtAllocationsPerReason).collect(Collectors.toMap(CourtAllocationPerReason::getDivorceReason, CourtAllocationPerReason::getCourtName));
    }

    public String selectCourtRandomly(String reasonForDivorce) {//TODO - test with null reason
        String selectedCourt = courtPerReasonForDivorce.getOrDefault(reasonForDivorce, null);//TODO - test calling this method without setting up the map (courtPerReasonForDivorce)
        if (selectedCourt == null) {
            selectedCourt = selectCourtRandomly();
        }

        return selectedCourt;
    }

    public String selectCourtRandomly() {
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
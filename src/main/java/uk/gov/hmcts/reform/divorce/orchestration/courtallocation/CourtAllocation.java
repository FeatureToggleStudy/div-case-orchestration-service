package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CourtAllocation {

    private static final Random random = new Random();

    private final String[] weightedArrayOfCourts;

    public CourtAllocation(CourtWeight[] courts) {
        List<String> weightedListOfCourts = new ArrayList<>();//TODO - refactor this
        for (CourtWeight courtWeight : courts) {
            for (int i = 0; i < courtWeight.getWeight(); i++) {
                weightedListOfCourts.add(courtWeight.getCourtName());
            }
        }
        this.weightedArrayOfCourts = weightedListOfCourts.stream().toArray(String[]::new);
    }

    public String selectCourtRandomly() {
        int randomIndex = random.nextInt(weightedArrayOfCourts.length);
        return weightedArrayOfCourts[randomIndex];
    }

}
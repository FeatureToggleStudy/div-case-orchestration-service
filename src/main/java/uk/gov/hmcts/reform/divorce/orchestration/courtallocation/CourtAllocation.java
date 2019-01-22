package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.Random;

public class CourtAllocation {

    private final String[] courts;

    public CourtAllocation(String[] courts) {
        this.courts = courts;
    }

    public String selectCourtRandomly() {
        Random random = new Random();
        int randomIndex = random.nextInt(courts.length);

        return courts[randomIndex];
    }
}

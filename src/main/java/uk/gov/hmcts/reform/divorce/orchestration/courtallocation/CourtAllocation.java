package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class CourtAllocation {

    private static final Random random = new Random();

    private final String[] weightedArrayOfCourts;

    private Map<String, String> courtPerReasonForDivorce;

    public CourtAllocation(CourtWeight[] courts) {//TODO - varargs?
        List<String> weightedListOfCourts = new ArrayList<>();//TODO - refactor this
        for (CourtWeight courtWeight : courts) {
            for (int i = 0; i < courtWeight.getWeight(); i++) {
                weightedListOfCourts.add(courtWeight.getCourtName());
            }
        }
        this.weightedArrayOfCourts = weightedListOfCourts.stream().toArray(String[]::new);
    }

    public CourtAllocation(CourtAllocationPerReason[] courtAllocationsPerReason, CourtWeight[] courts) {
        this(courts);

        //TODO - any good reasons for using Array not List?
        courtPerReasonForDivorce = Arrays.stream(courtAllocationsPerReason).collect(Collectors.toMap(CourtAllocationPerReason::getDivorceReason, CourtAllocationPerReason::getCourtName));
    }

    public String selectCourtRandomly() {
        int randomIndex = random.nextInt(weightedArrayOfCourts.length);
        return weightedArrayOfCourts[randomIndex];
    }

    public String selectCourtRandomly(String divorceCaseJson) {
        String reasonForDivorce = JsonPath.read(divorceCaseJson, "$.reasonForDivorce");

        String selectedCourt = courtPerReasonForDivorce.getOrDefault(reasonForDivorce, null);//TODO - test calling this method without setting up the map (courtPerReasonForDivorce)
        if (selectedCourt == null) {
            selectedCourt = selectCourtRandomly();
        }

        return selectedCourt;
    }
}
package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import lombok.Getter;

@Getter
public class CourtAllocationPerReason {

    private String courtName;
    private String divorceReason;

    public CourtAllocationPerReason(String courtName, String divorceReason) {
        this.courtName = courtName;
        this.divorceReason = divorceReason;
    }

}
package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationPerReason;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtWeight;

@Data
public class EnvironmentCourtAllocationConfiguration {

    private CourtWeight[] courtsWeightedDistribution;
    private CourtAllocationPerReason[] courtsForSpecificReasons;

}

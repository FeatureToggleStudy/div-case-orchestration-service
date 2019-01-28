package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationPerReason;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtWeight;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
public class EnvironmentCourtAllocationConfiguration {

    private List<CourtWeight> courtsWeightedDistribution = new ArrayList<>();
    private List<CourtAllocationPerReason> courtsForSpecificReasons = new ArrayList<>();

}
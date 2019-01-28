package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CourtAllocationConfigurationTest {

    private final CourtAllocationConfiguration courtAllocationConfiguration = new CourtAllocationConfiguration();

    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly() throws IOException {
        EnvironmentCourtAllocationConfiguration environmentConfigurationJson = getJsonFromResourceFile(
                "/courtAllocation/config-with-court-weight-only.json", EnvironmentCourtAllocationConfiguration.class);

        CourtAllocator courtAllocator = courtAllocationConfiguration.configureCourtAllocationFromEnvironmentVariable(environmentConfigurationJson);

        HashSet<String> courtIds = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            courtIds.add(courtAllocator.selectCourtForGivenDivorceReason(Optional.empty()));
        }

        assertThat(courtIds, containsInAnyOrder("courtNumber1", "courtNumber2"));
    }

    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly_AndReasonSpecificCourt() throws IOException {
        EnvironmentCourtAllocationConfiguration environmentConfigurationJson = getJsonFromResourceFile(
                "/courtAllocation/config-with-court-weight-and-reasons.json", EnvironmentCourtAllocationConfiguration.class);

        CourtAllocator courtAllocator = courtAllocationConfiguration.configureCourtAllocationFromEnvironmentVariable(environmentConfigurationJson);

        HashSet<String> courtIds = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            courtIds.add(courtAllocator.selectCourtForGivenDivorceReason(Optional.empty()));
        }
        String courtIdForSpecificReason = courtAllocator.selectCourtForGivenDivorceReason(Optional.of("specificReason"));

        assertThat(courtIds, containsInAnyOrder("courtNumber1", "courtNumber2"));
        assertThat(courtIdForSpecificReason, is("courtNumber3"));
    }

    //TODO - should it work with only reason specific? I think so, if it's not hard to do

    //TODO - should it work with an empty file? probably not

}
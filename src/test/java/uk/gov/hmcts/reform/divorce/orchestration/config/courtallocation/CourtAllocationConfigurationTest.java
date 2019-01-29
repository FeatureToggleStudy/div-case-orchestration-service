package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CourtAllocationConfigurationTest {

    private final CourtAllocationConfiguration courtAllocationConfiguration = new CourtAllocationConfiguration();

    @Test
    public void shouldBuildObjectFromJsonFile() throws IOException, URISyntaxException {//TODO - should it be a separate test class?
        URI uri = CourtAllocationConfigurationTest.class.getResource("/courtAllocation/config-with-court-weight-and-reasons.json").toURI();
        String json = Files.lines(Paths.get(uri)).collect(Collectors.joining());
        //TODO - should this be a spring boot test? This way, I can test only the result
        //TODO - should I be testing this at all?
        EnvironmentCourtAllocationConfiguration environmentConfigurationJson = courtAllocationConfiguration.setUpEnvironmentCourtAllocationConfiguration(json, new ObjectMapper());//TODO - smelly...

        EnvironmentCourtAllocationConfiguration expectedEnvironmentConfigurationJson = getJsonFromResourceFile("/courtAllocation/config-with-court-weight-and-reasons.json", EnvironmentCourtAllocationConfiguration.class);

        assertThat(environmentConfigurationJson, equalTo(expectedEnvironmentConfigurationJson));
        //TODO - do I really need the test yaml file to have this as well, I think I don't...

        //TODO - should I have a spring test that will use an ACTUAL environment variable (like the one that will be used in the servers)?
    }

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
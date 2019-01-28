package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationPerReason;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtWeight;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;

import java.io.IOException;
import java.util.List;

@Configuration
public class CourtAllocationConfiguration {

    @Bean
    public EnvironmentCourtAllocationConfiguration setUpEnvironmentCourtAllocationConfiguration(
            @Value("${court.allocation}") String configurationJson,
            @Autowired ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(configurationJson, EnvironmentCourtAllocationConfiguration.class);
    }

    @Bean
    public CourtAllocator configureCourtAllocationFromEnvironmentVariable(
            @Autowired EnvironmentCourtAllocationConfiguration environmentCourtAllocationJson) {

        List<CourtWeight> courtWeights = environmentCourtAllocationJson.getCourtsWeightedDistribution();
        List<CourtAllocationPerReason> courtsAllocationPerReasons = environmentCourtAllocationJson.getCourtsForSpecificReasons();

        return new DefaultCourtAllocator(courtsAllocationPerReasons, courtWeights);
    }

}
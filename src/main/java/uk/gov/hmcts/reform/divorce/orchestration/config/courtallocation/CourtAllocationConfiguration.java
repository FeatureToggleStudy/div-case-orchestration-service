package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationPerReason;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtWeight;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;

@Configuration
public class CourtAllocationConfiguration {

    @Bean
    public CourtAllocator configureCourtAllocationFromEnvironmentVariable(EnvironmentCourtAllocationConfiguration environmentCourtAllocationConfigurationJson) {

        CourtWeight[] courtWeights = environmentCourtAllocationConfigurationJson.getCourtsWeightedDistribution();
        CourtAllocationPerReason[] courtsAllocationPerReasons = environmentCourtAllocationConfigurationJson.getCourtsForSpecificReasons();

        //TODO - replace this with env variable config
        return new DefaultCourtAllocator(courtsAllocationPerReasons, courtWeights);//TODO - make sure I test configuration from env variables work
    }

}
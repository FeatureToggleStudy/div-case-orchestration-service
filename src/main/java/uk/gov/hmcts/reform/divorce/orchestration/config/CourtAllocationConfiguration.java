package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtWeight;

@Configuration
public class CourtAllocationConfiguration {

    @Bean
    public DefaultCourtAllocator getCourtAllocation() {
        //TODO - replace this with env variable config
        return new DefaultCourtAllocator(new CourtWeight[]{
                new CourtWeight("eastMidlands", 1),//TODO - do we have these constants anywhere else?
                new CourtWeight("southWest", 1)
        });//TODO - make sure I test configuration from env variables work
    }

}

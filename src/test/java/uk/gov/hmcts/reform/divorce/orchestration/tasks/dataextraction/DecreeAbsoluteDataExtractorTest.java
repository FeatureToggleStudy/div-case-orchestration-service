package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DecreeAbsoluteDataExtractorTest {

    @Test
    public void testBasicCsvExtractorValues() {
        CSVExtractor csvExtractor = new DecreeAbsoluteDataExtractor("dest-email@divorce.gov.uk");
        assertThat(csvExtractor.getHeaderLine(), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(csvExtractor.getDestinationEmailAddress(), is("dest-email@divorce.gov.uk"));
        assertThat(csvExtractor.getFileNamePrefix(), is("DA"));
    }

}
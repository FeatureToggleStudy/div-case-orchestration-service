package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionFileCreatorTest {

    private static final String TEST_AUTHORISATION_TOKEN = "testToken";
    private static final String DATE_TO_EXTRACT_KEY = "dateToExtract";

    private final String testLastModifiedDate = "2019-04-12";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private CMSElasticSearchHelper cmsElasticSearchHelper;

    private DataExtractionFileCreator classUnderTest;

    @Before
    public void setUp() {
        DecreeAbsoluteDataExtractor caseDetailsMapper = new DecreeAbsoluteDataExtractor();
        classUnderTest = new DataExtractionFileCreator(caseDetailsMapper, cmsElasticSearchHelper);//TODO - should I mock helper - should it be called helper, or support?
    }

    @Test
    public void shouldAddFileToContext() throws TaskException, IOException {
        Map<String, Object> firstCaseData = new HashMap<>();
        firstCaseData.put("D8caseReference", "TEST1");
        firstCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-12T16:49:00.015");
        firstCaseData.put("DecreeNisiGrantedDate", "2017-08-17");
        Map<String, Object> secondCaseData = new HashMap<>();
        secondCaseData.put("D8caseReference", "TEST2");
        secondCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-24T16:49:00.015");
        secondCaseData.put("DecreeNisiGrantedDate", "2017-08-26");
        CaseDetails firstCaseDetails = CaseDetails.builder().caseData(firstCaseData).build();
        CaseDetails secondCaseDetails = CaseDetails.builder().caseData(secondCaseData).build();
        when(cmsElasticSearchHelper.searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            QueryBuilders.termQuery("last_modified", testLastModifiedDate),//TODO - Mockito not responding to this
            any()
//            QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase())
        )).thenReturn(Stream.of(firstCaseDetails, secondCaseDetails));

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_AUTHORISATION_TOKEN);
        taskContext.setTransientObject(DATE_TO_EXTRACT_KEY, LocalDate.parse(testLastModifiedDate));
        classUnderTest.execute(taskContext, null);

        File createdFile = taskContext.getTransientObject(FILE_TO_PUBLISH);
        assertThat(createdFile, is(notNullValue()));
        List<String> fileLines = Files.readAllLines(createdFile.toPath());
        assertThat(fileLines.get(0), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(fileLines.get(1), is("TEST1,12/06/2018,17/08/2017,petitioner"));
        assertThat(fileLines.get(2), is("TEST2,24/06/2018,26/08/2017,petitioner"));
        verify(cmsElasticSearchHelper).searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            eq(QueryBuilders.termQuery("last_modified", testLastModifiedDate)),//TODO - Mockito not responding to this - do this last
            any()
//            eq(QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase()))
        );
    }

    @Test
    public void shouldUseDecreeAbsoluteGrantedDate_WhenDecreeAbsoluteApplicationDate_IsNotProvided() throws TaskException, IOException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "TEST1");
        caseData.put("DecreeAbsoluteGrantedDate", "2017-08-17T16:49:00.015");
        caseData.put("DecreeNisiGrantedDate", "2017-08-26");

        when(cmsElasticSearchHelper.searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            QueryBuilders.termQuery("last_modified", testLastModifiedDate),//TODO - Mockito not responding to this
            any()
//            QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase())))
        )).thenReturn(Stream.of(CaseDetails.builder().caseData(caseData).build()));

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_AUTHORISATION_TOKEN);
        taskContext.setTransientObject(DATE_TO_EXTRACT_KEY, LocalDate.parse(testLastModifiedDate));
        classUnderTest.execute(taskContext, null);

        File createdFile = taskContext.getTransientObject(FILE_TO_PUBLISH);
        assertThat(createdFile, is(notNullValue()));
        List<String> fileLines = Files.readAllLines(createdFile.toPath());
        assertThat(fileLines.get(0), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(fileLines.get(1), is("TEST1,17/08/2017,26/08/2017,petitioner"));
        verify(cmsElasticSearchHelper).searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            eq(QueryBuilders.termQuery("last_modified", testLastModifiedDate)),//TODO - Mockito not responding to this - do this last
            any()
//            eq(QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase()))
        );
    }

    @Test
    public void shouldNotInsertDate_WhenDecreeNisiGrantedDate_IsNotProvided() throws TaskException, IOException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "TEST1");
        caseData.put("DecreeAbsoluteGrantedDate", "2017-08-17T16:49:00.015");

        when(cmsElasticSearchHelper.searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            QueryBuilders.termQuery("last_modified", testLastModifiedDate),//TODO - Mockito not responding to this
            any()
//            QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase())))
        )).thenReturn(Stream.of(CaseDetails.builder().caseData(caseData).build()));

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_AUTHORISATION_TOKEN);
        taskContext.setTransientObject(DATE_TO_EXTRACT_KEY, LocalDate.parse(testLastModifiedDate));
        classUnderTest.execute(taskContext, null);

        File createdFile = taskContext.getTransientObject(FILE_TO_PUBLISH);
        assertThat(createdFile, is(notNullValue()));
        List<String> fileLines = Files.readAllLines(createdFile.toPath());
        assertThat(fileLines.get(0), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(fileLines.get(1), is("TEST1,17/08/2017,,petitioner"));
        verify(cmsElasticSearchHelper).searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            eq(QueryBuilders.termQuery("last_modified", testLastModifiedDate)),//TODO - Mockito not responding to this - do this last
            any()
//            eq(QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase()))
        );
    }

    @Test
    public void shouldNotAddCaseToFileWhenMandatoryFieldsAreNotPresent() throws TaskException, IOException {
        Map<String, Object> firstCaseData = new HashMap<>();
        firstCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-12T16:49:00.015");
        firstCaseData.put("DecreeNisiGrantedDate", "2017-08-17");
        Map<String, Object> secondCaseData = new HashMap<>();
        secondCaseData.put("D8caseReference", "TEST2");
        secondCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-24T16:49:00.015");
        secondCaseData.put("DecreeNisiGrantedDate", "2017-08-26");

        when(cmsElasticSearchHelper.searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            QueryBuilders.termQuery("last_modified", testLastModifiedDate),//TODO - Mockito not responding to this
            any()
//            QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase())))
        )).thenReturn(Stream.of(
            CaseDetails.builder().caseData(firstCaseData).build(),
            CaseDetails.builder().caseData(secondCaseData).build()
        ));

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_AUTHORISATION_TOKEN);
        taskContext.setTransientObject(DATE_TO_EXTRACT_KEY, LocalDate.parse(testLastModifiedDate));
        classUnderTest.execute(taskContext, null);

        File createdFile = taskContext.getTransientObject(FILE_TO_PUBLISH);
        assertThat(createdFile, is(notNullValue()));
        List<String> fileLines = Files.readAllLines(createdFile.toPath());
        assertThat(fileLines.get(0), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(fileLines.get(1), is("TEST2,24/06/2018,26/08/2017,petitioner"));
        verify(cmsElasticSearchHelper).searchCMSCases(eq(0), eq(50), eq(TEST_AUTHORISATION_TOKEN),
            any(),
//            eq(QueryBuilders.termQuery("last_modified", testLastModifiedDate)),//TODO - Mockito not responding to this - do this last - rebase with Chris and Dale's work?
            any()
//            eq(QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase()))
        );
    }

}
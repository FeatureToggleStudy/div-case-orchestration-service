package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.DATE_TO_EXTRACT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;

@Component
@Slf4j
public class DataExtractionFileCreator implements Task<Void> {

    private final CMSElasticSearchHelper cmsElasticSearchHelper;
    private final DecreeAbsoluteDataExtractor caseDetailsMapper;

    @Autowired
    public DataExtractionFileCreator(DecreeAbsoluteDataExtractor caseDetailsMapper, CMSElasticSearchHelper cmsElasticSearchHelper) {//TODO - reorder these parameters
        this.cmsElasticSearchHelper = cmsElasticSearchHelper;
        this.caseDetailsMapper = caseDetailsMapper;
    }

    @Override
    public Void execute(TaskContext context, Void payload) throws TaskException {
        LocalDate lastModifiedDate = context.getTransientObject(DATE_TO_EXTRACT_KEY);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        QueryBuilder[] queryBuilders = {
            QueryBuilders.termQuery("last_modified", lastModifiedDate),
            QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase())
        };
//        log.info("Created csv file with {} lines of case data", csvBodyLines.size());//TODO - this won't work now

        StringBuilder csvFileContent = new StringBuilder();
        csvFileContent.append("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA");

        cmsElasticSearchHelper.searchCMSCases(0, 50, authToken, queryBuilders)
            .map(caseDetailsMapper::mapCaseData)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(csvFileContent::append);

        File csvFile = createFile(csvFileContent.toString());
        context.setTransientObject(FILE_TO_PUBLISH, csvFile);

        return payload;
    }

    private File createFile(String csvFileContent) throws TaskException {
        File csvFile;

        try {
            csvFile = Files.createTempFile("DA_family_man_data_extraction", ".csv").toFile();
            Files.write(csvFile.toPath(), csvFileContent.getBytes());
            csvFile.deleteOnExit();
        } catch (IOException e) {
            throw new TaskException("Problems creating CSV file.", e);
        }

        return csvFile;
    }

}
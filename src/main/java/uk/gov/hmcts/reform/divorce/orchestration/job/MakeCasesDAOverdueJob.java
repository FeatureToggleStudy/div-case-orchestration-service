package uk.gov.hmcts.reform.divorce.orchestration.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@Slf4j
public class MakeCasesDAOverdueJob implements Job {

    @Autowired
    private DecreeAbsoluteService decreeAbsoluteService;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            log.info("Starting MakeCasesDAOverdue Job...");
            decreeAbsoluteService.processCaseOverdueForDecreeAbsolute(authUtil.getCaseworkerToken());
            log.info("MakeCasesDAOverdue Job executed");
        } catch (WorkflowException e) {
            throw new JobExecutionException("Cases overdue for DA failed", e);
        }
    }

}

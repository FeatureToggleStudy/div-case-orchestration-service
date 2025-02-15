package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;

@RestController
@Slf4j
@AllArgsConstructor
public class DecreeAbsoluteCaseInternalController {

    private final DecreeAbsoluteService decreeAbsoluteService;

    //TODO - ideally, we should be calling the job, not the service - otherwise we can't guarantee the job works.
    // Note: if we call the job here and an assumed JobExecutionContext is required to be injected which might not be
    // as useful as we thought

    @PostMapping(path = "/cases/da/make-eligible")
    @ApiOperation(value = "Make cases eligible for Decree Absolute")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cases are made eligible for Decree Absolute"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<String> makeCasesEligibleForDA(
        @RequestHeader(value = "Authorization") String authorizationToken) throws WorkflowException {
        int casesProcessed = decreeAbsoluteService.enableCaseEligibleForDecreeAbsolute(authorizationToken);
        return ResponseEntity.ok("Cases made eligible for DA: " + casesProcessed);
    }

    @PostMapping(path = "/cases/da/make-overdue")
    @ApiOperation(value = "Change the status of cases that are overdue for Decree Absolute")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cases set to Decree Absolute Overdue"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<String> makeCasesOverdueForDA(
        @RequestHeader(value = "Authorization") String authorizationToken) throws WorkflowException {
        int casesProcessed = decreeAbsoluteService.processCaseOverdueForDecreeAbsolute(authorizationToken);
        return ResponseEntity.ok("Cases made DAOverdue: " + casesProcessed);
    }

}

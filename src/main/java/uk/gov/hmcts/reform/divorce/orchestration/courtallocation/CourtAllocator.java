package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

public interface CourtAllocator {

    String selectCourtRandomly();//TODO - this should probably be private to subclass
    String selectCourtForGivenDivorceReason(String reasonForDivorce);

}
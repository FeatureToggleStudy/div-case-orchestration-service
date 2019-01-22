package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import lombok.Getter;

@Getter
/**
 * Court weight uses positive integer weight as the court weight as opposed to decimal percentual representation.
 *
 * For example: to configure two courts, one of them being twice as likely to be chosen,
 * just configure one to have weight "1" and the other to have weight "2".
 */
public class CourtWeight {

    private final String courtName;
    private final int weight;

    public CourtWeight(String courtName, int weight) {//TODO weight needs to be positive or zero
        this.courtName = courtName;
        this.weight = weight;
    }

}
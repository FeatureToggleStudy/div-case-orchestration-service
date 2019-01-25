package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "Court allocated to handle a case")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode
public class AllocatedCourt {//TODO - should this be in the ccd package?

    @ApiModelProperty(value = "Unique identifier for court")
    private String courtId;

    public AllocatedCourt(String courtId) {
        this.courtId = courtId;
    }

}
package org.personal.dinoshare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadForm {

    @NotNull(message =  "만료 시간을 선택해주세요.")
    @Min(value = 1, message = "만료 시간은 1분 이상이어야 합니다.")
    private Integer expMinutes;
}

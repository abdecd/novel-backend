package com.abdecd.novelbackend.business.pojo.dto.reader;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateReaderDetailDTO {
    @NotNull
    @Schema(description = "用户id")
    private Integer userId;

    @NotNull
    @Length(min = DTOConstant.PERSON_NAME_LENGTH_MIN, max = DTOConstant.PERSON_NAME_LENGTH_MAX)
    @Schema(description = "用户昵称")
    private String nickname;

    @NotNull
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "用户头像")
    private String avatar;

    @NotNull
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "用户签名")
    private String signature;
}

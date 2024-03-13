package com.abdecd.novelbackend.business.pojo.dto.reader;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateReaderDetailDTO {
    @NotBlank
    @Length(min = DTOConstant.PERSON_NAME_LENGTH_MIN, max = DTOConstant.PERSON_NAME_LENGTH_MAX)
    @Schema(description = "用户昵称")
    private String nickname;

    @NotNull
    @Schema(description = "用户头像")
    private MultipartFile avatar;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "用户签名")
    private String signature;
}

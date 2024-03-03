package com.abdecd.novelbackend.business.pojo.vo.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CaptchaVO {
    private String verifyCodeId;
    private String captcha;
}

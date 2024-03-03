package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.properties.TtlProperties;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CommonService {
    @Autowired
    DefaultKaptcha captchaProducer;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Resource
    TtlProperties ttlProperties;

    private static final String VERIFY_CODE_PREFIX = "verifyCode:";

    public Pair<String, byte[]> generateCaptcha() {
        byte[] captchaOutputStream;
        var verifyCodeId = UUID.randomUUID().toString();
        try (
            ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream()
        ) {
            String verifyCode = captchaProducer.createText();
            // 存下答案
            redisTemplate.opsForValue().set(VERIFY_CODE_PREFIX + verifyCodeId, verifyCode, ttlProperties.getCaptchaTtlSeconds(), TimeUnit.SECONDS);

            // 生成图片
            BufferedImage challenge = captchaProducer.createImage(verifyCode);
            ImageIO.write(challenge, "jpg", imgOutputStream);
            captchaOutputStream = imgOutputStream.toByteArray();
        } catch (IllegalArgumentException | IOException e) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
        return Pair.of(verifyCodeId, captchaOutputStream);
    }

    public void verifyCaptcha(String uuid, String captcha) {
        log.info("uuid: {}, captcha: {}", uuid, captcha);
        var key = VERIFY_CODE_PREFIX + uuid;
        if (!StringUtils.hasText(uuid) || Boolean.FALSE.equals(redisTemplate.hasKey(key)))
            throw new BaseException(MessageConstant.CAPTCHA_EXPIRED);
        if (!Objects.equals(redisTemplate.opsForValue().get(key), captcha))
            throw new BaseException(MessageConstant.CAPTCHA_ERROR);
    }
}

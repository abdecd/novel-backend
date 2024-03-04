package com.abdecd.novelbackend.business;

import com.abdecd.novelbackend.business.service.CommonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

@SpringBootTest
class BusinessApplicationTests {
    @Autowired
    CommonService commonService;
    @Autowired
    JavaMailSender mailSender;

    @Test
    void contextLoads() {
        System.out.println(UUID.randomUUID());
    }

    @Test
    void testCaptcha() {
        commonService.sendCodeToVerifyEmail("");
    }

}

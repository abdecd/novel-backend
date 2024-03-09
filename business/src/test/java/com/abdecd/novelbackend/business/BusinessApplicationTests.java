package com.abdecd.novelbackend.business;

import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.tokenlogin.service.UserBaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class BusinessApplicationTests {
    @Autowired
    CommonService commonService;
    @Autowired
    UserBaseService userBaseService;

    @Test
    void contextLoads() {
        System.out.println(UUID.randomUUID());
    }

    @Test
    void testPublicKey() {
        System.out.println(userBaseService.getPublicKey());
    }

}

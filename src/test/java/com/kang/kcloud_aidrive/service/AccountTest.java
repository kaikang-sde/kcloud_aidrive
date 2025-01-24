package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.KcloudAidriveApplication;
import com.kang.kcloud_aidrive.controller.req.AccountLoginReq;
import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.dto.AccountDTO;
import com.kang.kcloud_aidrive.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KcloudAidriveApplication.class)
@Slf4j
public class AccountTest {

    @Autowired
    private AccountService accountService;

    /*
     * Unit test for register
     */
    @Test
    public void testRegister() {
        AccountRegisterReq req = AccountRegisterReq.builder().phone("123").password("123").username("kai").avatarUrl("https://example.com/avatar.jpg").build();
        accountService.register(req);
    }

    @Test
    public void testLogin() {
        AccountLoginReq req = AccountLoginReq.builder().phone("123").password("123").build();
        AccountDTO accountDTO = accountService.login(req);
        String loginToken = JwtUtil.geneLoginJWT(accountDTO);
        log.info("loginToken:{}", loginToken);
    }

    @Test
    public void testDetail() {
        AccountDTO accountDTO = accountService.queryDetail(482835125172834304L);
    }


}

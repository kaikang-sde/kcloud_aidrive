package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.config.AccountConfig;
import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.entity.AccountDAO;
import com.kang.kcloud_aidrive.enums.AccountRoleEnum;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.mapper.AccountMapper;
import com.kang.kcloud_aidrive.model.AccountDO;
import com.kang.kcloud_aidrive.repository.AccountRepository;
import com.kang.kcloud_aidrive.service.AccountService;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountMapper accountMapper, AccountRepository accountRepository) {
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;

    }


    @Override
    public void register(AccountRegisterReq req) {
        // check if phone number exists - via JPA
        List<AccountDAO> accountDAOs = accountRepository.findByPhone(req.getPhone());
        if (!accountDAOs.isEmpty()) {
            throw new BizException(BizCodeEnum.ACCOUNT_REPEAT);
        }

        AccountDAO accountDAO = SpringBeanUtil.copyProperties(req, AccountDAO.class);

        // encrypt password
        String digestAsHex = DigestUtils.md5DigestAsHex((AccountConfig.ACCOUNT_SALT + req.getPassword()).getBytes());
        accountDAO.setPassword(digestAsHex);
        accountDAO.setRole(AccountRoleEnum.COMMON.name());

        // insert account to db table
        accountRepository.save(accountDAO);

        // TODO: operations


    }
}

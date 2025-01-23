package com.kang.kcloud_aidrive.util;

import com.kang.kcloud_aidrive.dto.AccountDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
public class JwtUtil {

    private static final String LOGIN_SUBJECT = "KCLOUD-AIDRIVE";

    private final static String SECRET_KEY = "com.kang.kcloud_aidrive.this.is.a.secret.key";
    // 签名算法
    private final static SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;
    // 使用密钥
    private final static SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    // token过期时间，30 days
    private static final long EXPIRED = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成JWT
     *
     * @param accountDTO account login info
     * @return generate JWT token
     * @throws NullPointerException if accountDTO is null
     */
    public static String geneLoginJWT(AccountDTO accountDTO) {
        if (accountDTO == null) {
            throw new NullPointerException("对象为空");
        }

        String token = Jwts.builder()
                .subject(LOGIN_SUBJECT)
                .claim("accountId", accountDTO.getId())
                .claim("username", accountDTO.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRED))
                .signWith(KEY, ALGORITHM)  // 直接使用KEY即可
                .compact();

        // 添加自定义前缀
        return addPrefix(LOGIN_SUBJECT, token);
    }

    /**
     * 校验JWT
     *
     * @param token JWT token
     * @return Claims
     * @throws IllegalArgumentException token is null or empty
     * @throws RuntimeException         If the JWT signature verification fails, the JWT has expired, or JWT decryption fails.
     */
    public static Claims checkLoginJWT(String token) {
        try {
            log.debug("Starting to check JWT: {}", token);

            if (token == null || token.trim().isEmpty()) {
                log.error("Token cannot be null or empty");
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            token = token.trim();

            token = removePrefix(LOGIN_SUBJECT, token);
            log.debug("After remove prefix - Token: {}", token);
            // 解析 JWT - 非对称加密 ，返回Claims map 结构
            Claims payload = Jwts.parser()
                    .verifyWith(KEY)  //设置签名的密钥, 使用相同的 KEY
                    .build()
                    .parseSignedClaims(token).getPayload();

            log.info("JWT decryption succeeded，Claims: {}", payload);
            return payload;
        } catch (IllegalArgumentException e) {
            log.error("JWT verification failed: {}", e.getMessage(), e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT signature verification failed: {}", e.getMessage(), e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT expired: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("JWT decryption failed: {}", e.getMessage(), e);
        }
        return null;
    }


    private static String addPrefix(String prefix, String token) {
        return prefix + token;
    }

    private static String removePrefix(String prefix, String token) {
        if (token.startsWith(prefix)) {
            return token.replace(prefix, "").trim();
        }
        return token;
    }


}

package org.target.dndbackend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class VerificationCodeService {

    private static final String CODE_PREFIX = "verification:code:";
    private static final Duration CODE_EXPIRE_TIME = Duration.ofMinutes(5);

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String generateCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));

        String key = CODE_PREFIX + email;

        redisTemplate.opsForValue().set(
                key,
                code,
                CODE_EXPIRE_TIME
        );

        return code;
    }

    public boolean verifyCode(String email, String inputCode) {
        String key = CODE_PREFIX + email;

        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            return false;
        }

        boolean matched = savedCode.equals(inputCode);

        if (matched) {
            redisTemplate.delete(key);
        }

        return matched;
    }
}
package backend.taskweaver.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void setTokenValues(String key, UserTokens tokenValue) throws JsonProcessingException {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String jsonValue = objectMapper.writeValueAsString(tokenValue);
        values.set(key, jsonValue);
    }

    @Transactional(readOnly = true)
    public UserTokens getTokenValues(String key) throws JsonProcessingException {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String jsonValue = values.get(key);
        if (jsonValue == null) {
            return null;
        }
        return objectMapper.readValue(jsonValue, UserTokens.class);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

//    public boolean checkExistsValue(String value) {
//        return !value.equals("false");
//    }
}

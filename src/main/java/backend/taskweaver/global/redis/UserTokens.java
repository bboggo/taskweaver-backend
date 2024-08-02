package backend.taskweaver.global.redis;

import lombok.Getter;

@Getter
public class UserTokens {
    private final String accessToken;
    private final String deviceToken;

    public UserTokens(String accessToken, String deviceToken) {
        this.accessToken = accessToken;
        this.deviceToken = deviceToken;
    }
}

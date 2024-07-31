package backend.taskweaver.domain.member.entity.enums;

import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;

import java.util.Arrays;

public enum LoginType {
    DEFAULT, OAUTH;

    public static LoginType findLoginType(String loginType) {
        return Arrays.stream(LoginType.values())
                .filter(stateName -> stateName.toString().equals(loginType))
                .findFirst()
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.LOGIN_TYPE_NOT_FOUND));
    }
}

package backend.taskweaver.domain.member.dto;

import backend.taskweaver.domain.member.entity.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;

public record SignInResponse(
        @Schema(description = "회원 아이디", example = "1")
        Long id,
        @Schema(description = "회원 이메일", example = "xxx@naver.com")
        String email,
        @Schema(description = "로그인 유형", example = "DEFAULT")
        LoginType type,

        @Schema(description = "닉네임", example = "conan")
        String nickname,

        @Schema(description = "알림 읽음 여부", example = "YES")
        backend.taskweaver.domain.notification.entity.enums.isRead isRead,

        @Schema(description = "프로필 사진 url", example = "aaa.com")
        String imageUrl,
        @Schema(description = "엑세스 토큰", example = "accessToken")
        String accessToken,
        @Schema(description = "리프레시 토큰", example = "refreshToken")
        String refreshToken
) {
}

package backend.taskweaver.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResponse(
        @Schema(description = "회원 아이디", example = "1")
        Long id,
        @Schema(description = "회원 이메일", example = "xxx@naver.com")
        String email,
        @Schema(description = "회원 닉네임", example = "코난")
        String nickname,
        @Schema(description = "회원 로그인 타입", example = "DEFAULT")
        String loginType,
        @Schema(description = "회원 이미지 url", example = "https://taskweaver-bucket.s3.ap-northeast-2.amazonaws.com/pen.png")
        String imageUrl
) {
}
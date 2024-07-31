package backend.taskweaver.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
        @Schema(description = "회원 이메일", example = "xxx@naver.com")
        @NotNull
        @Email(message = " 올바른 형식의 이메일 주소여야 합니다.")
        String email,

        @Schema(description = "회원 비밀번호", example = "amy1234!")
        @NotNull
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = " 비밀번호는 영문, 숫자, 특수문자 중 3종류 이상 조합하여 최소 8자리 이상이어야 합니다.")
        String password,

        @Schema(description = "회원 닉네임", example = "코난")
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = " 닉네임은 영문, 숫자, 한글만 허용됩니다.")
        String nickname,

        @Schema(description = "회원 로그인 타입", example = "DEFAULT")
        @NotNull
        String loginType
) {
}
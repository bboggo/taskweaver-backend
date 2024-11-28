package backend.taskweaver.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record EmailRequest(
        @Schema(description = "회원 이메일", example = "xxx@naver.com")
        @NotNull
        @Email
        String email
) {
}
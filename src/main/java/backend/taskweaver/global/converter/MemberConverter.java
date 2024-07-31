package backend.taskweaver.global.converter;

import backend.taskweaver.domain.member.dto.*;
import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.entity.enums.LoginType;
import backend.taskweaver.domain.project.entity.enums.ProjectStateName;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

public class MemberConverter {

    public static Member toMember(SignUpRequest signUpRequest, PasswordEncoder encoder, String profileImage) {
        String imageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = profileImage;
        }

        return Member.builder()
                .email(signUpRequest.email())
                .password(encoder.encode(signUpRequest.password()))
                .nickname(signUpRequest.nickname())
                .loginType(LoginType.findLoginType(signUpRequest.loginType()))
                .imageUrl(imageUrl)
                .build();
    }

    public static SignUpResponse toSignUpResponse(Member member) {
        return new SignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                String.valueOf(member.getLoginType()),
                member.getImageUrl()
        );
    }

    public static SignInResponse toSignInResponse(Member member, String accessToken, String refreshToken) {
        return new SignInResponse(
                member.getId(),
                member.getEmail(),
                member.getLoginType(),
                member.getNickname(),
//                member.getIsRead(),
                member.getImageUrl(),
                accessToken,
                refreshToken
        );
    }

    public static MemberInfoResponse toMemberInfoResponse(Member member) {
        return new MemberInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getLoginType(),
                member.getImageUrl()
        );
    }

    public static CreateAccessTokenResponse toCreateAccessTokenResponse(String newAccessToken) {
        return new CreateAccessTokenResponse(newAccessToken);
    }
}

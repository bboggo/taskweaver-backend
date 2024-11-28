package backend.taskweaver.domain.member.controller;

import backend.taskweaver.domain.member.dto.EmailRequest;
import backend.taskweaver.domain.member.dto.SignInRequest;
import backend.taskweaver.domain.member.dto.SignUpRequest;
import backend.taskweaver.domain.member.dto.UpdateMemberRequest;
import backend.taskweaver.domain.member.dto.UpdatePasswordRequest;
import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.service.EmailService;
import backend.taskweaver.domain.member.service.MemberService;
import backend.taskweaver.domain.member.service.SignService;
import backend.taskweaver.domain.team.dto.TeamRequest;
import backend.taskweaver.global.code.ApiResponse;
import backend.taskweaver.global.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "회원 관련 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/user")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    @Operation(summary = "마이페이지(회원 정보 조회)")
    @GetMapping
    public ResponseEntity<ApiResponse> getMemberInfo(@AuthenticationPrincipal User user) {
        ApiResponse ar = ApiResponse.builder()
                .result(memberService.getMemberInfo(Long.parseLong(user.getUsername())))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @Operation(summary = "개인정보 변경 api", description = "개인정보 수정 api 입니다.")
    @PatchMapping("")
    public ResponseEntity<ApiResponse> updateMember(@AuthenticationPrincipal User user, @RequestPart("request") UpdateMemberRequest request,
                                                    @RequestPart("profileImage") MultipartFile profileImage) throws IOException {
        memberService.updateMember(Long.parseLong(user.getUsername()), request, profileImage);
        ApiResponse ar = ApiResponse.builder()
                .result(memberService.updateMember(Long.parseLong(user.getUsername()), request, profileImage))
                .resultCode(SuccessCode.UPDATE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.UPDATE_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @Operation(summary = "비밀번호 변경 api", description = "회원의 비밀번호를 변경하는 api 입니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(@AuthenticationPrincipal User user,
                                                      @RequestBody @Valid UpdatePasswordRequest request) {
        memberService.updatePassword(Long.parseLong(user.getUsername()), request);
        ApiResponse ar = ApiResponse.builder()
                .resultCode(SuccessCode.UPDATE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.UPDATE_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @Operation(summary = "비밀번호 찾기 api", description = "임시 비밀번호를 발급하는 api입니다.")
    @PostMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody @Valid EmailRequest request) {
        memberService.findPassword(request);
        ApiResponse ar = ApiResponse.builder()
                .resultCode(SuccessCode.UPDATE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.UPDATE_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }


    @Operation(summary = "인증번호 발송 api", description = "인증번호 이메일 발송 api 입니다.")
    @PostMapping("/email")
    public ResponseEntity<ApiResponse> sendEmail(@RequestBody @Valid EmailRequest request) {
        //TODO 이메일 중복 여부 처리!
        ApiResponse ar = ApiResponse.builder()
                .result(emailService.sendMail(request))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @Operation(summary = "회원 탈퇴 api", description = "회원 탈퇴 api 입니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteUser(@AuthenticationPrincipal User user) {
        memberService.delete(Long.parseLong(user.getUsername()));
        ApiResponse ar = ApiResponse.builder()
                .resultCode(SuccessCode.DELETE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.DELETE_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }
}

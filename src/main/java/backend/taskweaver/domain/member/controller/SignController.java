package backend.taskweaver.domain.member.controller;

import backend.taskweaver.domain.member.dto.SignInRequest;
import backend.taskweaver.domain.member.dto.SignUpRequest;
import backend.taskweaver.domain.member.entity.oauth.KakaoProfile;
import backend.taskweaver.domain.member.entity.oauth.OauthToken;
import backend.taskweaver.domain.member.service.SignService;
import backend.taskweaver.global.code.ApiResponse;
import backend.taskweaver.global.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@Tag(name = "회원 가입 및 로그인")
@RequiredArgsConstructor
@RestController
@RequestMapping
@Slf4j
public class SignController {
    private final SignService signService;

    @Operation(summary = "회원 가입")
    @PostMapping(value = "/v1/auth/sign-up", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<ApiResponse> signUp(@RequestPart("request") @Valid SignUpRequest reqeust,
                                              @RequestPart("profileImage") MultipartFile profileImage) {
        ApiResponse ar = ApiResponse.builder()
                .result(signService.registerMember(reqeust, profileImage))
                .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.INSERT_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @Operation(summary = "로그인")
    @PostMapping("/v1/auth/sign-in")
    public ResponseEntity<ApiResponse> signIn(@RequestBody SignInRequest request) {
        ApiResponse ar = ApiResponse.builder()
                .result(signService.signIn(request))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @GetMapping("/v1/auth/kakao")
    public ResponseEntity<ApiResponse> getLogin(@RequestParam("code") String code) {
        ApiResponse ar = ApiResponse.builder()
                .result(signService.getKakaoAccessToken(code))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    @Operation(summary = "로그아웃")
    @DeleteMapping("/v1/auth/logout")
    public ResponseEntity<ApiResponse> logout(@AuthenticationPrincipal User user) {
        signService.logout(Long.parseLong(user.getUsername()));
        ApiResponse ar = ApiResponse.builder()
                .resultCode(SuccessCode.DELETE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.DELETE_SUCCESS.getMessage())
                .build();
        return new ResponseEntity<>(ar, HttpStatus.OK);
    }
}

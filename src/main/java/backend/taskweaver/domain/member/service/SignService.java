package backend.taskweaver.domain.member.service;


import backend.taskweaver.domain.files.service.S3Service;
import backend.taskweaver.domain.member.dto.*;
import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.entity.MemberRefreshToken;
import backend.taskweaver.global.redis.UserTokens;
import backend.taskweaver.domain.member.entity.oauth.KakaoProfile;
import backend.taskweaver.domain.member.entity.oauth.OauthToken;
import backend.taskweaver.domain.member.repository.MemberRefreshTokenRepository;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.converter.MemberConverter;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import backend.taskweaver.global.redis.RedisService;
import backend.taskweaver.global.security.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class SignService {
    private final MemberRepository memberRepository;
    private final MemberRefreshTokenRepository memberRefreshTokenRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final S3Service s3Service;

    @Value("${kakaoApiKey}")
    private String KakaoApiKey;

    @Value("${kakaoRedirectUrl}")
    private String RedirectURI;

    // 회원가입
    @Transactional
    public SignUpResponse registerMember(SignUpRequest request, MultipartFile profileImage) {
        String imageUrl;

        try {
            // 이미지 파일이 비어 있는지 확인
            if (profileImage == null || profileImage.isEmpty()) {
                // 디폴트 이미지 URL을 가져오는 방법
                imageUrl = s3Service.saveDefaultProfileImage(); // 디폴트 이미지를 저장하는 메소드 호출
            } else {
                // 업로드된 이미지 URL 가져오기
                imageUrl = s3Service.saveProfileImage(profileImage);
            }

            // 회원 정보 저장
            Member member = MemberConverter.toMember(request, encoder, imageUrl);
            member = memberRepository.saveAndFlush(member);

            return MemberConverter.toSignUpResponse(member);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessExceptionHandler(ErrorCode.DUPLICATED_EMAIL);
        } catch (IOException e) {
            throw new BusinessExceptionHandler(ErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public SignInResponse signIn(SignInRequest request) throws JsonProcessingException {
        Member member = memberRepository.findByEmail(request.email())
                .filter(it -> encoder.matches(request.password(), it.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));
        String accessToken = tokenProvider.createAccessToken(String.format("%s:%s", member.getId(), member.getLoginType()));    // token -> accessToken
        String refreshToken = tokenProvider.createRefreshToken();    // 리프레시 토큰 생성
        // 리프레시 토큰이 이미 있으면 토큰을 갱신하고 없으면 토큰을 추가
        memberRefreshTokenRepository.findById(member.getId())
                .ifPresentOrElse(
                        it -> it.updateRefreshToken(refreshToken),
                        () -> memberRefreshTokenRepository.save(new MemberRefreshToken(member, refreshToken))
                );
        UserTokens userTokens = new UserTokens(accessToken, request.deviceToken());
        redisService.setTokenValues(request.email(), userTokens);
        return MemberConverter.toSignInResponse(member, accessToken, refreshToken);
    }

    public OauthSignUpResponse getKakaoAccessToken(String code) {

        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KakaoApiKey);
        params.add("redirect_uri", RedirectURI);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        OauthToken oauthToken = null;
        try {
            oauthToken = objectMapper.readValue(accessTokenResponse.getBody(), OauthToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new OauthSignUpResponse(findKakaoProfile(oauthToken.getAccess_token()).getKakao_account().getEmail());
    }


    public KakaoProfile findKakaoProfile(String token) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);

        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                kakaoProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoProfile;
    }

    @Transactional(readOnly = true)
    public void logout(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));
        redisService.deleteValues(member.getEmail());
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        if(memberRepository.findByNickname(nickname).isPresent()) {
            throw new BusinessExceptionHandler(ErrorCode.DUPLICATED_NICKNAME);
        }
    }
}

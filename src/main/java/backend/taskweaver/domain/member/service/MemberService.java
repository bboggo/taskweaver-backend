package backend.taskweaver.domain.member.service;

import backend.taskweaver.domain.files.service.S3Service;
import backend.taskweaver.domain.member.dto.*;
import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.repository.MemberRefreshTokenRepository;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.domain.project.entity.Project;
import backend.taskweaver.domain.project.entity.ProjectMember;
import backend.taskweaver.domain.project.repository.ProjectMemberRepository;
import backend.taskweaver.domain.project.repository.ProjectRepository;
import backend.taskweaver.domain.task.repository.TaskMemberRepository;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.repository.TeamMemberRepository;
import backend.taskweaver.domain.team.repository.TeamRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.converter.MemberConverter;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import backend.taskweaver.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final S3Service s3Service;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final EmailService emailService;


    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(Long id) {
        return memberRepository.findById(id)
                .map(MemberConverter::toMemberInfoResponse)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));
    }


    // 회원정보 수정
    @Transactional
    public SignUpResponse updateMember(Long memberId, UpdateMemberRequest request, MultipartFile profileImage) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        member.setNickname(request.nickname());

        String imageUrl = (profileImage != null && !profileImage.isEmpty())
                ? s3Service.saveProfileImage(profileImage)
                : s3Service.saveDefaultProfileImage();

        if (imageUrl == null) {
            throw new BusinessExceptionHandler(ErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }

        member.setImageUrl(imageUrl); // 이미지 URL 업데이트
        return MemberConverter.toSignUpResponse(member);
    }


    @Transactional
    public void updatePassword(Long memberId, UpdatePasswordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호가 같은지 확인
        if(!encoder.matches(request.oldPassword(), member.getPassword())) {
            throw new BusinessExceptionHandler(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // 현재 비밀번호와 같은 비밀번호로 변경 불가능
        if(request.oldPassword().equals(request.newPassword())) {
            throw new BusinessExceptionHandler(ErrorCode.SAME_PASSWORD);
        }

        member.updatePassword(encoder.encode(request.newPassword()));
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!?@#$%^&*";
    private static final int PASSWORD_LENGTH = 8; // 원하는 비밀번호 길이

    public static String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return "TEMP-" + password.toString();
    }
    @Transactional
    public void findPassword(EmailRequest emailRequest) {
        //임시 비밀번호 생성
        String tempPassword = generateTemporaryPassword();
        Member member = memberRepository.findByEmail(emailRequest.email())
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        member.updatePassword(encoder.encode(tempPassword));
        emailService.sendMailForPassword(emailRequest, tempPassword);
    }

    @Transactional
    public void delete(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        List<Team> teams = teamRepository.findAllByTeamLeader(memberId);
        for (Team team : teams) {
            //  팀 리더고 팀 멤버가 2명 이상이면 탈퇴 불가 -> 리더 권한 변경 후 탈퇴 가능
            if (team.getTeamMembers().size() >= 2) {
                throw new BusinessExceptionHandler(ErrorCode.CANNOT_WITHDRAW_TEAM_LEADER);
            }
            // 팀 조회 때를 대비하여 팀의 teamLeader를 null로 변경
            team.setTeamLeader(null);
        }

        // 팀 멤버가 1명뿐이면 팀에서 바로 탈퇴 가능
        teamMemberRepository.deleteByMemberId(memberId);

        List<Project> projects = projectRepository.findAllByManagerId(memberId);
        for (Project project : projects) {
            List<ProjectMember> projectMembers = projectMemberRepository.findByProject(project);
            // 프로젝트 리더고 프로젝트 멤버가 2명 이상이면 탈퇴 불가 -> 리더 권한 변경 후 탈퇴 가능
            if (projectMembers.size() >= 2) {
                throw new BusinessExceptionHandler(ErrorCode.CANNOT_WITHDRAW_PROJECT_LEADER);
            }
            // 프로젝트 조회 때를 대비하여 프로젝트의 projectLeader를 null로 변경
            project.setManager(null, null);
        }

        // 프로젝트 멤버가 1명뿐이면 프로젝트에서 바로 탈퇴 가능
        projectMemberRepository.deleteByMemberId(memberId);

        // 속한 태스크에서 탈퇴하기
        taskMemberRepository.deleteByMemberId(memberId);

        // 회원 탈퇴하기
        member.deleteSoftly();
    }

}

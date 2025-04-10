package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.domain.notification.entity.Notification;
import backend.taskweaver.domain.notification.entity.NotificationMember;
import backend.taskweaver.domain.notification.entity.enums.NotificationType;
import backend.taskweaver.domain.notification.entity.enums.isRead;
import backend.taskweaver.domain.notification.repository.NotificationMemberRepository;
import backend.taskweaver.domain.notification.repository.NotificationRepository;
import backend.taskweaver.domain.team.dto.TeamInviteRequest;
import backend.taskweaver.domain.team.dto.TeamInviteResponse;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.entity.TeamMember;
import backend.taskweaver.domain.team.entity.TeamMemberState;
import backend.taskweaver.domain.team.entity.enums.InviteState;
import backend.taskweaver.domain.team.entity.enums.TeamRole;
import backend.taskweaver.domain.team.repository.TeamMemberRepository;
import backend.taskweaver.domain.team.repository.TeamMemberStateRepository;
import backend.taskweaver.domain.team.repository.TeamRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.converter.TeamConverter;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class TeamInviteServiceImpl implements TeamInviteService{

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    private final TeamMemberRepository teamMemberRepository;
    private final NotificationSender notificationSender;
    private final TeamMemberInviteManager inviteManager;

    // 팀 초대
    public TeamInviteRequest.EmailInviteRequest inviteEmail(TeamInviteRequest.EmailInviteRequest request) {

        Member target = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        if (target.getId().equals(team.getTeamLeader())) {
            throw new BusinessExceptionHandler(ErrorCode.CANNOT_INVITE_TEAM_LEADER);
        }

        inviteManager.createInviteState(team, target);
        notificationSender.sendTeamInvite(target, team);

        return request;
    }


    public TeamInviteResponse.InviteAnswerResult answerInvite(TeamInviteRequest.InviteAnswerRequest request, Long user) {
        Long teamId = request.getTeamId();
        Long userId = user;
        System.out.println(userId);
        System.out.println(user);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        // 초대 수락/거절 여부 확인
        if (request.getInviteState() == 1) {
            // 중복 확인
            if (teamMemberRepository.existsByTeamAndMember(team, member)) {
                throw new BusinessExceptionHandler(ErrorCode.DUPLICATE_TEAM_MEMBER);
            }

            TeamMember teamMember = TeamMember.builder()
                    .team(team)
                    .member(member)
                    .role(TeamRole.MEMBER)
                    .build();

            teamMemberRepository.save(teamMember);

            // 초대 응답 후에 해당 team id와 user id가 일치하는 값을 TeamMemberState에서 찾아서 inviteState 값을 ACCEPT로 바꾸기
            inviteManager.acceptInvite(team.getId(), userId);
            return TeamConverter.toInviteResponse(teamMember);

        } else if (request.getInviteState() == 2) {
            // 초대를 거절한 경우
            inviteManager.refuseInvite(team.getId(), userId);
            return null;
        } else {
            // 잘못된 응답 값에 대한 오류 처리
            throw new BusinessExceptionHandler(ErrorCode.INVALID_INVITE_RESPONSE);
        }
    }


}

package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.domain.notification.entity.Notification;
import backend.taskweaver.domain.notification.entity.NotificationMember;
import backend.taskweaver.domain.notification.entity.enums.NotificationType;
import backend.taskweaver.domain.notification.entity.enums.isRead;
import backend.taskweaver.domain.notification.repository.NotificationMemberRepository;
import backend.taskweaver.domain.notification.repository.NotificationRepository;
import backend.taskweaver.domain.team.dto.*;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamServiceImpl implements TeamService{
    private final TeamRepository teamRepository;
    private final TeamMemberManager teamMemberManager;
    private String generateInviteLink() {
        return "https://example.com/invite/" + UUID.randomUUID().toString();
    }


    public TeamResponse.teamCreateResult createTeam(TeamRequest.teamCreateRequest request, Long user) {

        Team team =  Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .inviteLink(generateInviteLink())
                .teamLeader(user)
                .build();

        team = teamRepository.save(team);
        teamMemberManager.addLeaderToTeam(team, user);
        return TeamConverter.toCreateResponse(team);
    }


    public TeamResponse.findTeamResult findTeam(Long id, Long userId) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        List<TeamMember> teamMembers = teamMemberManager.findAllByTeamWithoutDuplicates(id);

        String myRole = teamMembers.stream()
                .filter(member -> member.getMember().getId().equals(userId))
                .map(member -> member.getRole().toString())
                .findFirst()
                .orElse("");

        return TeamConverter.toGetTeamResponse(team, myRole, teamMembers);
    }


    @Transactional
    public TeamResponse.teamUpdateResult updateTeam(Long teamId, TeamRequest.teamCreateRequest request, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        TeamValidator.validateTeamLeader(team, userId);

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        teamRepository.save(team);
        return TeamConverter.toUpdateResponse(team);
    }



    @Transactional
    public TeamResponse.TeamDeleteResult deleteTeam(Long teamId, Long user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        TeamValidator.validateTeamLeader(team, user);
        teamRepository.delete(team);
        return TeamConverter.toDeleteResponse(team);
    }



    public List<TeamResponse.AllTeamInfo> findTeamsByUserId(Long userId) {
        List<TeamMember> teamMembers = teamMemberManager.findAllByMemberId(userId);
        return teamMembers.stream()
                .map(teamMember -> {
                    Team team = teamMember.getTeam();
                    String myRole = teamMember.getRole().toString();

                    List<TeamMember> distinctMembers = teamMemberManager.findAllByTeamWithoutDuplicates(team.getId());
                    List<TeamMember> filtered = teamMemberManager.filterAndLimit(distinctMembers, userId, 3);
                    filtered.add(0, teamMember);

                    List<TeamResponse.MemberInfo> members = filtered.stream()
                            .map(m -> new TeamResponse.MemberInfo(
                                    m.getMember().getId(),
                                    m.getMember().getImageUrl(),
                                    m.getMember().getNickname(),
                                    m.getRole()))
                            .toList();

                    int adjustedTotalMembers = Math.max(0, distinctMembers.size() - 3);

                    return TeamConverter.toGetAllTeamResponse(
                            team, myRole, adjustedTotalMembers, members
                    );
                })
                .toList();
    }

    // 팀원 삭제
    @Transactional
    public void deleteTeamMembers(Long teamId, List<Long> memberIds, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        TeamValidator.validateTeamLeader(team, userId); // 삭제하는 사람이 팀장인지 확인

        for (Long memberId : memberIds) {
            teamMemberManager.removeMember(team.getId(), memberId); // 내부에서 팀장인지 다시 확인
        }
    }


    public TeamLeaderResponse.ChangeLeaderResponse changeTeamLeader(Long teamId, TeamLeaderRequest.ChangeLeaderRequest request, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        TeamValidator.validateTeamLeader(team, userId); // 현재 유저가 팀장인지 검증

        Long newLeaderId = request.getNewLeaderId();

        teamMemberManager.changeRole(teamId, userId, TeamRole.MEMBER);     // 기존 팀장 → 멤버
        teamMemberManager.changeRole(teamId, newLeaderId, TeamRole.LEADER); // 새 팀장 → 리더

        team.setTeamLeader(newLeaderId);
        teamRepository.save(team);

        return TeamConverter.toChangeLeaderResponse(team, newLeaderId);
    }

//    public List<TeamMember> findAllDistinctTeamMembersWithTeam(Long teamId) {
//        List<TeamMember> allTeamMembers = teamMemberRepository.findAllByTeamId(teamId);
//        Set<Long> memberIds = new HashSet<>();
//        List<TeamMember> distinctTeamMembers = new ArrayList<>();
//
//        for (TeamMember teamMember : allTeamMembers) {
//            if (!memberIds.contains(teamMember.getMember().getId())) {
//                distinctTeamMembers.add(teamMember);
//                memberIds.add(teamMember.getMember().getId());
//            }
//        }
//
//        return distinctTeamMembers;
//    }

    //
//    // 중복된 팀 멤버 제거
//    private List<TeamMember> removeDuplicates(Set<TeamMember> teamMembers) {
//        Set<Long> seen = new HashSet<>();
//        List<TeamMember> uniqueTeamMembers = new ArrayList<>();
//        for (TeamMember teamMember : teamMembers) {
//            if (seen.add(teamMember.getMember().getId())) {
//                uniqueTeamMembers.add(teamMember);
//            }
//        }
//        return uniqueTeamMembers;
//    }
//
//
//    public TeamLeaderResponse.ChangeLeaderResponse changeTeamLeader(Long teamId, TeamLeaderRequest.ChangeLeaderRequest request, Long user) {
//        // 요청으로부터 팀 ID와 새로운 팀장 ID를 가져옵니다.
//        Long newLeaderId = request.getNewLeaderId();
//
//        // 팀을 찾고, 로그인한 유저가 팀장인지 확인
//        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));
//
//
//        if (!team.getTeamLeader().equals(user)) {
//            throw new BusinessExceptionHandler(ErrorCode.NOT_TEAM_LEADER);
//        }
//
//        // 새로운 팀장 정보가 유효한지 확인
//        Member newLeader = memberRepository.findById(newLeaderId)
//                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
//
//        // 기존 팀 리더의 role을 MEMBER로 변경
//        TeamMember currentLeaderMember = (TeamMember) teamMemberRepository.findByTeamIdAndMemberId(teamId, user)
//                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
//        currentLeaderMember.setRole(TeamRole.MEMBER);
//        teamMemberRepository.save(currentLeaderMember);
//
//        // 새로운 팀 리더의 role을 LEADER로 변경
//        TeamMember newLeaderMember = (TeamMember) teamMemberRepository.findByTeamIdAndMemberId(teamId, newLeaderId)
//                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
//        newLeaderMember.setRole(TeamRole.LEADER);
//        teamMemberRepository.save(newLeaderMember);
//
//
//        // 새로운 팀장으로 변경
//        team.setTeamLeader(newLeaderId);
//        teamRepository.save(team);
//
//        return TeamConverter.toChangeLeaderResponse(team, newLeaderId);
//    }


}

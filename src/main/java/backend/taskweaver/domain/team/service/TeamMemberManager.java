package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.entity.TeamMember;
import backend.taskweaver.domain.team.entity.enums.TeamRole;
import backend.taskweaver.domain.team.repository.TeamMemberRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeamMemberManager {

    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    public void addLeaderToTeam(Team team, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .member(member)
                .role(TeamRole.LEADER)
                .build();
        teamMemberRepository.save(teamMember);
    }

    //findAllDistinctTeamMembersWithTeam 대신
    public List<TeamMember> findAllByTeamWithoutDuplicates(Long teamId) {
        List<TeamMember> all = teamMemberRepository.findAllByTeamId(teamId);
        Set<Long> seen = new HashSet<>();
        return all.stream()
                .filter(tm -> seen.add(tm.getMember().getId()))
                .toList();
    }

    public List<TeamMember> findAllByMemberId(Long memberId) {
        return teamMemberRepository.findAllByMemberId(memberId);
    }


    public void removeMember(Long teamId, Long memberId) {
        teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);
    }

    public void changeRole(Long teamId, Long memberId, TeamRole newRole) {
        TeamMember member = teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        member.setRole(newRole);
        teamMemberRepository.save(member);
    }

    public List<TeamMember> filterAndLimit(List<TeamMember> teamMembers, Long userId, int limit) {
        return teamMembers.stream()
                .filter(member -> !member.getMember().getId().equals(userId))
                .limit(limit)
                .collect(Collectors.toList());
    }
}


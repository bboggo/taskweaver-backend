package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.entity.TeamMemberState;
import backend.taskweaver.domain.team.entity.enums.InviteState;
import backend.taskweaver.domain.team.repository.TeamMemberStateRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class TeamMemberInviteManager {

    private final TeamMemberStateRepository teamMemberStateRepository;

    public TeamMemberInviteManager(TeamMemberStateRepository teamMemberStateRepository) {
        this.teamMemberStateRepository = teamMemberStateRepository;
    }

    public void createInviteState(Team team, Member member) {
        if (teamMemberStateRepository.existsByTeamIdAndMemberId(team.getId(), member.getId())) {
            throw new BusinessExceptionHandler(ErrorCode.INVITATION_ALREADY_SENT);
        }

        TeamMemberState state = TeamMemberState.builder()
                .team(team)
                .member(member)
                .state(InviteState.IN_PROGRESS)
                .build();

        teamMemberStateRepository.save(state);
    }

    public void acceptInvite(Long teamId, Long memberId) {
        TeamMemberState state = teamMemberStateRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_STATE_NOT_FOUND));
        state.setState(InviteState.ACCEPT);
        teamMemberStateRepository.save(state);
    }

    public void refuseInvite(Long teamId, Long memberId) {
        TeamMemberState state = teamMemberStateRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_STATE_NOT_FOUND));
        state.setState(InviteState.REFUSE);
        teamMemberStateRepository.save(state);
    }
}

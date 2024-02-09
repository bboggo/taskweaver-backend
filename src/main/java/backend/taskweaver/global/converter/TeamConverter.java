package backend.taskweaver.global.converter;

import backend.taskweaver.domain.team.dto.TeamInviteResponse;
import backend.taskweaver.domain.team.dto.TeamRequest;
import backend.taskweaver.domain.team.dto.TeamResponse;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.entity.TeamMember;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamConverter {

    public static Team toTeam(TeamRequest.teamCreateRequest request) {
        return Team.builder()
                .name(request.getName())
                .inviteLink(generateInviteLink())
                .build();
    }

    public static TeamResponse.teamCreateResult toCreateResponse(Team team) {
        return new TeamResponse.teamCreateResult(
            team.getId(),
            team.getName(),
            team.getInviteLink(),
            team.getTeamLeader(),
            team.getCreatedAt()
        );
    }

    public static TeamInviteResponse.InviteAnswerResult toInviteResponse(TeamMember teamMember) {
        return new TeamInviteResponse.InviteAnswerResult(
                teamMember.getId(),
                teamMember.getRole(),
                teamMember.getTeam().getId(),
                teamMember.getMember().getId()
        );
    }

    public static TeamResponse.findTeamResult toGetTeamResponse(Team team, List<TeamMember> teamMembers) {
        List<TeamResponse.TeamMemberInfo> memberInfos = teamMembers.stream()
                .map(member -> new TeamResponse.TeamMemberInfo(member.getMember().getId(), member.getMember().getEmail(), member.getMember().getImageUrl(), member.getMember().getNickname()))
                .collect(Collectors.toList());

        return new TeamResponse.findTeamResult(
                team.getId(),
                team.getName(),
                team.getTeamLeader(),
                team.getInviteLink(),
                team.getCreatedAt(),
                memberInfos
        );
    }
    public static String generateInviteLink() {
        UUID uuid = UUID.randomUUID();
        // 도메인 결정 후
        return "https://localhost:" + "8081" + "/invite/" + uuid.toString();
    }

}

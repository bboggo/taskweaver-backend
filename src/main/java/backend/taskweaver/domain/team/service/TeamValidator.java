package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;

public class TeamValidator {

    public static void validateTeamLeader(Team team, Long userId) {
        if (!team.getTeamLeader().equals(userId)) {
            throw new BusinessExceptionHandler(ErrorCode.NOT_TEAM_LEADER);
        }
    }

    public static void validateNotTeamLeader(Team team, Long memberId) {
        if (team.getTeamLeader().equals(memberId)) {
            throw new BusinessExceptionHandler(ErrorCode.CANNOT_DELETE_TEAM_LEADER);
        }
    }
}

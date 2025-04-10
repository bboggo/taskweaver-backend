package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.team.entity.Team;

public interface NotificationSender {
    void sendTeamInvite(Member toMember, Team team);

}

package backend.taskweaver.domain.team.service;


import backend.taskweaver.domain.team.dto.*;

import java.util.List;


public interface TeamService {

    // 우선 팀 생성자 필드로만 추가
    public TeamResponse.teamCreateResult createTeam(TeamRequest.teamCreateRequest request, Long user);
    public TeamResponse.findTeamResult findTeam(Long id, Long userId);

    public List<TeamResponse.AllTeamInfo> findTeamsByUserId(Long userId);
    public TeamInviteRequest.EmailInviteRequest inviteEmail(TeamInviteRequest.EmailInviteRequest request);

    public TeamInviteResponse.InviteAnswerResult answerInvite(TeamInviteRequest.InviteAnswerRequest request, Long user);

    public void deleteTeamMembers(Long teamId, List<Long> memberIds, Long user);

    public TeamLeaderResponse.ChangeLeaderResponse changeTeamLeader(Long teamId, TeamLeaderRequest.ChangeLeaderRequest request, Long user);

    public TeamResponse.TeamDeleteResult deleteTeam(Long teamId, Long user);
}

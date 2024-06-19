package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.repository.MemberRepository;
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

import static backend.taskweaver.global.converter.TeamConverter.generateInviteLink;

@RequiredArgsConstructor
@Service
public class TeamServiceImpl implements TeamService{
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberStateRepository teamMemberStateRepository;
    private final TeamMemberRepository teamMemberRepository;

    // 팀 생성
    // 우선 팀 생성자 필드로만 추가
    public TeamResponse.teamCreateResult createTeam(TeamRequest.teamCreateRequest request, Long user) {
        // 팀 리더 정보를 설정하여 팀 객체 생성
        Team team =  Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .inviteLink(generateInviteLink())
                .teamLeader(user) // 팀 리더 설정
                .build();

        // 팀 저장
        team = teamRepository.save(team);

        // 팀 멤버 생성 및 저장
        Member leader = memberRepository.findById(user)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .member(leader)
                .role(TeamRole.LEADER) // 팀 멤버의 역할 설정 (여기서는 일반 사용자)
                .build();

        // 팀 멤버 저장
        teamMemberRepository.save(teamMember);

        return TeamConverter.toCreateResponse(team);
    }
    // 해당 팀 조회
    public TeamResponse.findTeamResult findTeam(Long id, Long userId) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        List<TeamMember> teamMembers = findAllDistinctTeamMembersWithTeam(id);

        // 로그인한 유저의 역할(role)을 조회
        String myRole = ""; // 초기값 설정

        // 로그인한 유저의 역할 조회 로직
        Optional<TeamMember> userTeamMember = teamMembers.stream()
                .filter(member -> member.getMember().getId().equals(userId))
                .findFirst();

        if (userTeamMember.isPresent()) {
            myRole = userTeamMember.get().getRole().toString();
        }

        return TeamConverter.toGetTeamResponse(team, myRole, teamMembers);
    }

    // 팀 삭제
    @Transactional
    public TeamResponse.TeamDeleteResult deleteTeam(Long teamId, Long user) {
        // 팀 조회
        Team existingTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));


        // 로그인한 유저가 팀의 소유자인지 확인
        if (!existingTeam.getTeamLeader().equals(user)) {
            throw new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }

        // 소프트 삭제
        existingTeam.setActivated(false);

        teamRepository.save(existingTeam);

        return TeamConverter.toDeleteResponse(existingTeam);
    }

    public List<TeamMember> findAllDistinctTeamMembersWithTeam(Long teamId) {
        List<TeamMember> allTeamMembers = teamMemberRepository.findAllByTeamId(teamId);
        Set<Long> memberIds = new HashSet<>();
        List<TeamMember> distinctTeamMembers = new ArrayList<>();

        for (TeamMember teamMember : allTeamMembers) {
            if (!memberIds.contains(teamMember.getMember().getId())) {
                distinctTeamMembers.add(teamMember);
                memberIds.add(teamMember.getMember().getId());
            }
        }

        return distinctTeamMembers;
    }

    // 전체 리스트 조회
    public List<TeamResponse.AllTeamInfo> findTeamsByUserId(Long userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByMemberId(userId);
        return teamMembers.stream()
                .map(teamMember -> {
                    Team team = teamMember.getTeam();
                    String myRole = teamMember.getRole().toString();

                    // 중복된 팀 멤버를 제거하고, 최대 3명의 유일한 멤버를 선택하되, 로그인한 유저도 포함
                    List<TeamMember> distinctTeamMembers = removeDuplicates(team.getTeamMembers());
                    List<TeamMember> filteredMembers = distinctTeamMembers.stream()
                            .filter(member -> !member.getMember().getId().equals(userId)) // 로그인한 유저 제외
                            .limit(3)
                            .collect(Collectors.toList());

                    // 로그인한 유저 추가
                    filteredMembers.add(0, teamMember); // 로그인한 유저를 리스트의 맨 앞에 추가

                    // 유일한 멤버 정보를 MemberInfo 객체로 매핑
                    List<TeamResponse.MemberInfo> members = filteredMembers.stream()
                            .map(member -> new TeamResponse.MemberInfo(member.getMember().getId(), member.getMember().getImageUrl()))
                            .collect(Collectors.toList());

                    // 전체 인원수 계산
                    int totalMembers = distinctTeamMembers.size(); // 중복 제거된 멤버 수를 기준으로 전체 인원수 계산
                    int adjustedTotalMembers = totalMembers - 3; // 전체 인원수에서 3을 뺌

                    // 음수일 경우 0으로 설정
                    adjustedTotalMembers = Math.max(0, adjustedTotalMembers);

                    // 팀 정보 반환
                    return TeamConverter.toGetAllTeamResponse(
                            team,
                            myRole,
                            adjustedTotalMembers,
                            members
                    );
                })
                .collect(Collectors.toList());
    }

    // 중복된 팀 멤버 제거
    private List<TeamMember> removeDuplicates(Set<TeamMember> teamMembers) {
        Set<Long> seen = new HashSet<>();
        List<TeamMember> uniqueTeamMembers = new ArrayList<>();
        for (TeamMember teamMember : teamMembers) {
            if (seen.add(teamMember.getMember().getId())) {
                uniqueTeamMembers.add(teamMember);
            }
        }
        return uniqueTeamMembers;
    }

    // 팀원 삭제
    @Transactional
    public void deleteTeamMembers(Long teamId, List<Long> memberIds, Long user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

        // 팀 리더인지 확인
        if (team.getTeamLeader().equals(user)) {
            for (Long memberId : memberIds) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));

                // 팀 리더는 삭제할 수 없음
                if (!memberId.equals(user)) {
                    teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);
                } else {
                    throw new BusinessExceptionHandler(ErrorCode.CANNOT_DELETE_TEAM_LEADER);
                }
            }
        } else {
            // 팀 리더가 아닌 경우 예외 처리
            throw new BusinessExceptionHandler(ErrorCode.NOT_TEAM_LEADER);
        }
    }

    // 팀장 권한 변경
    public TeamLeaderResponse.ChangeLeaderResponse changeTeamLeader(Long teamId, TeamLeaderRequest.ChangeLeaderRequest request, Long user) {
        // 요청으로부터 팀 ID와 새로운 팀장 ID를 가져옵니다.
        Long newLeaderId = request.getNewLeaderId();

        // 로그인한 유저가 팀장인지 확인
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = optionalTeam.filter(t -> t.getTeamLeader().equals(user))
                .orElseThrow(() -> {
                    System.out.println("팀 리더 ID: " + optionalTeam.map(Team::getTeamLeader).orElse(null)); // 팀 리더 ID 출력
                    System.out.println("로그인한 유저 ID: " + user); // 로그인한 유저 ID 출력
                    return new BusinessExceptionHandler(ErrorCode.NOT_TEAM_LEADER);
                });
        // 새로운 팀장 정보가 유효한지 확인
        Member newLeader = memberRepository.findById(newLeaderId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        // 기존 팀 리더의 role을 MEMBER로 변경
        TeamMember currentLeaderMember = (TeamMember) teamMemberRepository.findByTeamIdAndMemberId(teamId, user)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        currentLeaderMember.setRole(TeamRole.MEMBER);
        teamMemberRepository.save(currentLeaderMember);

        // 새로운 팀 리더의 role을 LEADER로 변경
        TeamMember newLeaderMember = (TeamMember) teamMemberRepository.findByTeamIdAndMemberId(teamId, newLeaderId)
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        newLeaderMember.setRole(TeamRole.LEADER);
        teamMemberRepository.save(newLeaderMember);


        // 새로운 팀장으로 변경
        team.setTeamLeader(newLeaderId);
        teamRepository.save(team);

        return TeamConverter.toChangeLeaderResponse(team, newLeaderId);
    }



    // 팀 초대
    public TeamInviteRequest.EmailInviteRequest inviteEmail(TeamInviteRequest.EmailInviteRequest request) {
        List<Member> members = memberRepository.findAll();

        Optional<Member> matchingMember = members.stream()
                .filter(member -> member.getEmail().equals(request.getEmail()))
                .findFirst();

        if (matchingMember.isPresent()) {
            Long userId = matchingMember.get().getId();
            System.out.println(userId);

            Long teamId = request.getTeamId();

            // 팀 리더인지 확인
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND));

            Long teamLeaderId = team.getTeamLeader(); // 팀 리더의 ID 가져오기

            if (userId.equals(teamLeaderId)) {
                throw new BusinessExceptionHandler(ErrorCode.CANNOT_INVITE_TEAM_LEADER);
            }


            // 이미 존재하는 팀 초대 요청인 경우
            if (teamMemberStateRepository.existsByTeamIdAndMemberId(teamId, userId)) {
                throw new BusinessExceptionHandler(ErrorCode.INVITATION_ALREADY_SENT);
            }

            TeamMemberState teamMemberState = TeamMemberState.builder()
                    .member(matchingMember.get())
                    .state(InviteState.IN_PROGRESS)
                    .team(teamRepository.findById(teamId)
                            .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TEAM_NOT_FOUND)))
                    .build();
            teamMemberStateRepository.save(teamMemberState);
        } else {
            throw new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }
        return request;
    }


    // 초대 응답

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
            acceptInvite(teamId, userId);

            return TeamConverter.toInviteResponse(teamMember);
        } else if (request.getInviteState() == 2) {
            // 초대를 거절한 경우
            refuseInvite(teamId, userId);
            return null;
        } else {
            // 잘못된 응답 값에 대한 오류 처리
            throw new BusinessExceptionHandler(ErrorCode.INVALID_INVITE_RESPONSE);
        }
    }

    // 초대 응답 수락 시에 해당 team id와 user id가 일치하는 값을 TeamMemberState에서 찾아서 inviteState 값을 ACCEPT로 바꾸는 메소드
    private void acceptInvite(Long teamId, Long userId) {
        Optional<TeamMemberState> teamMemberStateOptional = teamMemberStateRepository.findByTeamIdAndMemberId(teamId, userId);
        teamMemberStateOptional.ifPresentOrElse(teamMemberState -> {
            teamMemberState.setState(InviteState.ACCEPT);
            teamMemberStateRepository.save(teamMemberState);
        }, () -> {
            // teamId와 userId가 일치하는 값을 찾지 못한 경우
            throw new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_STATE_NOT_FOUND);
        });
    }

    // 초대 응답 거부
    private void refuseInvite(Long teamId, Long userId) {
        Optional<TeamMemberState> teamMemberStateOptional = teamMemberStateRepository.findByTeamIdAndMemberId(teamId, userId);
        teamMemberStateOptional.ifPresentOrElse(teamMemberState -> {
            teamMemberState.setState(InviteState.REFUSE);
            teamMemberStateRepository.save(teamMemberState);
        }, () -> {
            // teamId와 userId가 일치하는 값을 찾지 못한 경우
            throw new BusinessExceptionHandler(ErrorCode.TEAM_MEMBER_STATE_NOT_FOUND);
        });
    }


}

package backend.taskweaver.domain.team.controller;

import backend.taskweaver.domain.team.dto.TeamInviteRequest;
import backend.taskweaver.domain.team.dto.TeamRequest;
import backend.taskweaver.domain.team.service.TeamService;
import backend.taskweaver.global.code.ApiResponse;
import backend.taskweaver.global.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1")
@Tag(name = "팀 관련")
@RequiredArgsConstructor
@Slf4j
@RestController
@EnableJpaAuditing
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "팀 생성")
    @PostMapping("/team")
    public ResponseEntity<ApiResponse> createTeam(@RequestBody TeamRequest.teamCreateRequest request, @AuthenticationPrincipal User user) {
        try {
            ApiResponse apiResponse = ApiResponse.builder()
                    .result(teamService.createTeam(request, Long.parseLong(user.getUsername())))
                    .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                    .resultMsg(SuccessCode.INSERT_SUCCESS.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 팀 삭제
    @Operation(summary = "팀 삭제")
    @DeleteMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse> deleteTeam(@PathVariable(name = "teamId") Long teamId, @AuthenticationPrincipal User user) {
        try {
            ApiResponse apiResponse = ApiResponse.builder()
                    .result(teamService.deleteTeam(teamId, Long.parseLong(user.getUsername())))
                    .resultCode(SuccessCode.DELETE_SUCCESS.getStatus())
                    .resultMsg(SuccessCode.DELETE_SUCCESS.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "팀 조회")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse> findTeam(@PathVariable(name = "teamId") Long teamId, @AuthenticationPrincipal User user) {

        ApiResponse apiResponse = ApiResponse.builder()
                .result(teamService.findTeam(teamId,Long.parseLong(user.getUsername())))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    // 팀 멤버만 조회
//    @Operation(summary = "팀 조회")
//    @GetMapping("/team/{teamId}/member")
//    public ResponseEntity<ApiResponse> AllTeamMember(@PathVariable(name = "teamId") Long teamId, @AuthenticationPrincipal User user) {
//        ApiResponse apiResponse = ApiResponse.builder()
//                .result(teamService.findTeam(teamId))
//                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
//                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
//                .build();
//        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
//    }

    // 팀 전체 조회
    @Operation(summary =  "로그인한 유저의 팀 전체 조회")
    @GetMapping("/teams")
    public ResponseEntity<ApiResponse> AllTeam(@AuthenticationPrincipal User user) {
        ApiResponse apiResponse = ApiResponse.builder()
                .result(teamService.findTeamsByUserId(Long.parseLong(user.getUsername())))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @Operation(summary =  "팀원 삭제")
    @PostMapping("/team/{teamId}/delete")
    public ResponseEntity<ApiResponse> deleteTeamMembers(@PathVariable Long teamId, @RequestBody TeamRequest.teamMemberDeleteRequest request, @AuthenticationPrincipal User user) {
        teamService.deleteTeamMembers(teamId, request.getMemberId(), Long.parseLong(user.getUsername())); // deleteTeamMembers 메서드 직접 호출
        ApiResponse apiResponse = ApiResponse.builder()
                .resultCode(SuccessCode.DELETE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.DELETE_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    // 팀장 권한 변경
    /*
    @Operation(summary =  "팀장 권한 변경")
    @PutMapping("/team/{teamId}/changeLeader")
    public ResponseEntity<ApiResponse> changeTeamLeader(@PathVariable Long teamId, @RequestBody TeamLeaderRequest.ChangeLeaderRequest request, @AuthenticationPrincipal User user) {
        teamService.changeTeamLeader(teamId, request, Long.parseLong(user.getUsername())); // deleteTeamMembers 메서드 직접 호출
        ApiResponse apiResponse = ApiResponse.builder()
                .result(teamService.changeTeamLeader(teamId, request, Long.parseLong(user.getUsername())))
                .resultCode(SuccessCode.UPDATE_SUCCESS.getStatus())
                .resultMsg(SuccessCode.UPDATE_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
    */


    @Operation(summary = "팀원 초대 - 이메일로 초대")
    @PostMapping("/team/invitation/email")
    public ResponseEntity<ApiResponse> inviteEmail(@RequestBody TeamInviteRequest.EmailInviteRequest request) {
        ApiResponse apiResponse = ApiResponse.builder()
                .result(teamService.inviteEmail(request))
                .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.INSERT_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @Operation(summary =  "팀원 초대 - 이메일로 초대 응답")
    @PostMapping("/team/invitation/answer")
    public ResponseEntity<ApiResponse> answerInviteEmail(@RequestBody TeamInviteRequest.InviteAnswerRequest request, @AuthenticationPrincipal User user) {
        ApiResponse apiResponse = ApiResponse.builder()
                .result(teamService.answerInvite(request, Long.parseLong(user.getUsername())))
                .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.INSERT_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}

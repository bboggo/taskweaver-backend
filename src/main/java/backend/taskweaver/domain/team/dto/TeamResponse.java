package backend.taskweaver.domain.team.dto;

import backend.taskweaver.domain.team.entity.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


public class TeamResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class teamCreateResult {
        Long id;
        String name;
        String description;
        String inviteLink;
        Long teamLeader;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class findTeamResult {
        @Schema(description = "팀 id", example = "1")
        Long id;

        @Schema(description = "팀 이름", example = "팀 이름")
        String name;

        @Schema(description = "팀 소개", example = "팀 소개")
        String description;


        @Schema(description = "팀 리더 id", example = "1")
        Long teamLeader;

        @Schema(description = "초대 링크", example = "https://localhost:8081/invite/abea91ea-c861-40aa-b1fe-be2fe98ba4e2")
        String inviteLink;

        @Schema(description = "생성 날짜", example = "2024-02-08T22:58:10.061223")
        LocalDateTime createdAt;

        @Schema(description = "로그인한 유저 권한", example = "LEADER")
        String myRole;

        List<TeamMemberInfo> teamMembers;

        @Schema(description = "전체 팀원 수", example = "3")
        Long memberCount;
    }



    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberInfo {
        @Schema(description = "멤버 id", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "example@example.com")
        private String email;


        @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
        private String imageUrl;

        @Schema(description = "닉네임", example = "user123")
        private String nickname;

        @Schema(description = "역할", example = "MEMBER")
        private String role; // Role 추가
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class deleteTeamMemberResult {
        @Schema(description = "삭제 원하는 id 리스트 형태로", example = "[1, 2, 3]")
        List<Long> memberId;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamDeleteResult {
        @Schema(description = "팀 id", example = "1")
        Long id;

        @Schema(description = "팀 이름", example = "팀 이름")
        String name;

        @Schema(description = "팀 리더 id", example = "1")
        Long teamLeader;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllTeamInfo {

        @Schema(description = "팀 id", example = "1")
        Long id;

        @Schema(description = "팀 이름", example = "팀 이름")
        String name;

        @Schema(description = "팀 소개", example = "팀 소개")
        String description;

        @Schema(description = "팀 리더 id", example = "1")
        Long teamLeader;

        @Schema(description = "초대 링크", example = "https://localhost:8081/invite/abea91ea-c861-40aa-b1fe-be2fe98ba4e2")
        String inviteLink;

        @Schema(description = "생성 날짜", example = "2024-02-08T22:58:10.061223")
        LocalDateTime createdAt;

        @Schema(description = "로그인한 유저 권한", example = "LEADER")
        String myRole;
        @Schema(description = "추가 멤버 수", example = "2")
        int totalMembers;

        List<MemberInfo> members; // members 추가
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {

        @Schema(description = "멤버 id", example = "1")
        private Long id;
        @Schema(description = "프로필 사진 url", example = "domain 주소")
        private String imageUrl;
    }
}

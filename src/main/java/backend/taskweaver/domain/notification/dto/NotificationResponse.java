package backend.taskweaver.domain.notification.dto;

import backend.taskweaver.domain.notification.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllNotificationInfo {
        @Schema(description = "알림 id", example = "1")
        Long id;

        @Schema(description = "발신자", example = "김민지")
        String sender;

        @Schema(description = "내용", example = "김민지님, TaskWeaver 팀에 초대되었습니다.")
        private String content;

        @Schema(description = "알림 타입", example = "Team")
        NotificationType type;

        @Schema(description = "관련된 팀, 프로젝트, 태스크 id", example = "1")
        Long relatedTypeId;

        @Schema(description = "팀 제목", example = "백엔드 개발팀")
        private String relatedTeamTitle; // 팀 제목 추가

        @Schema(description = "조회 여부", example = "YES")
        private String isRead; // is_read 필드 추가

        // 추가된 필드들
        @Schema(description = "로그인한 멤버 id", example = "1")
        private Long memberId;

        @Schema(description = "생성 날짜", example = "2024-02-08T22:58:10.061223")
        LocalDateTime createdAt;
    }

    
}

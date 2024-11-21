package backend.taskweaver.global.converter;

import backend.taskweaver.domain.notification.dto.NotificationResponse;
import backend.taskweaver.domain.notification.entity.Notification;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.notification.entity.NotificationMember;
import backend.taskweaver.domain.notification.entity.enums.NotificationType;
import backend.taskweaver.domain.team.repository.TeamRepository;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;

public class NotificationConverter {

    public static NotificationResponse.AllNotificationInfo toGetAllNotificationResponse(Notification notification, NotificationMember notificationMember, TeamRepository teamRepository) {


        String relatedTeamTitle = null;

        // 알림 타입이 TEAM인 경우 팀 제목 조회
        if (notification.getType() == NotificationType.TEAM) {
            Long relatedTypeId = notification.getRelatedTypeId();
            relatedTeamTitle = teamRepository.findById(relatedTypeId)
                    .map(Team::getName)
                    .orElse(null); // 팀이 없을 경우 null 반환
        }

        return new NotificationResponse.AllNotificationInfo(
                notification.getId(),
                notification.getSender(),
                notification.getContent(),
                notification.getType(),
                notification.getRelatedTypeId(),
                relatedTeamTitle,
                notificationMember.getIsRead().name(),
                notificationMember.getMember().getId(),
                notification.getCreatedAt()
        );
    }

}

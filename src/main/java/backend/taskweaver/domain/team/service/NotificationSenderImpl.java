package backend.taskweaver.domain.team.service;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.notification.entity.Notification;
import backend.taskweaver.domain.notification.entity.NotificationMember;
import backend.taskweaver.domain.notification.entity.enums.NotificationType;
import backend.taskweaver.domain.notification.entity.enums.isRead;
import backend.taskweaver.domain.notification.repository.NotificationMemberRepository;
import backend.taskweaver.domain.notification.repository.NotificationRepository;
import backend.taskweaver.domain.team.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationSenderImpl implements NotificationSender{
    private final NotificationRepository notificationRepository;
    private final NotificationMemberRepository notificationMemberRepository;

    @Override
    public void sendTeamInvite(Member toMember, Team team) {
        // 알림 생성 및 저장
        Notification notification = Notification.builder()
                .sender(team.getTeamLeader().toString()) // 팀 리더의 이름이나 아이디를 문자열로 변환하여 보냄
                .content("팀 초대가 도착했습니다.") // 알림 내용
                .type(NotificationType.TEAM)
                .relatedTypeId(team.getId()) // 초대와 관련된 팀의 ID
                .build();
        notificationRepository.save(notification);

        // 초대받은 멤버에게 알림 연결
        NotificationMember notificationMember = NotificationMember.builder()
                .isRead(isRead.NO)
                .member(toMember)
                .notification(notification)
                .build();

        notificationMemberRepository.save(notificationMember);
    }

}

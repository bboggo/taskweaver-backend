package backend.taskweaver.domain.notification.service;

import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.domain.notification.dto.NotificationResponse;
import backend.taskweaver.domain.notification.entity.Notification;
import backend.taskweaver.domain.notification.entity.NotificationMember;
import backend.taskweaver.domain.notification.entity.enums.isRead;
import backend.taskweaver.domain.notification.repository.NotificationMemberRepository;
import backend.taskweaver.domain.notification.repository.NotificationRepository;
import backend.taskweaver.domain.team.repository.TeamRepository;
import backend.taskweaver.global.converter.NotificationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationMemberRepository notificationMemberRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository; // MemberRepository 추가
    private final TeamRepository teamRepository; // TeamRepository 추가

    public List<NotificationResponse.AllNotificationInfo> getAllNotificationsForUser(Long memberId) {
        // 로그인한 사용자의 모든 알림 조회
        List<NotificationMember> notificationMembers = notificationMemberRepository.findByMemberId(memberId);

        notificationMembers.forEach(notificationMember -> {
            if (notificationMember.getIsRead() == isRead.NO) {
                notificationMember.setIsRead(isRead.YES);
                notificationMemberRepository.save(notificationMember); // 변경 사항 저장
            }
        });

        // 조회된 알림을 NotificationResponse.AllNotificationInfo 객체로 변환
        return notificationMembers.stream()
                .map(notificationMember -> NotificationConverter.toGetAllNotificationResponse(
                        notificationMember.getNotification(),
                        notificationMember,
                        teamRepository)) // teamRepository 전달
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOldNotifications(LocalDateTime cutoffDate) {
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);
        notificationRepository.deleteAll(oldNotifications);
    }
}

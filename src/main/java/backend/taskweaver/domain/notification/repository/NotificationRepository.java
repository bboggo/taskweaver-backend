package backend.taskweaver.domain.notification.repository;

import backend.taskweaver.domain.notification.entity.Notification;
import backend.taskweaver.domain.notification.entity.enums.isRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n JOIN NotificationMember nm ON n.id = nm.notification.id WHERE nm.member.id = :memberId")
    List<Notification> findAllByMemberId(@Param("memberId") Long memberId);

    List<Notification> findByCreatedAtBefore(LocalDateTime cutoffDate);

}
package backend.taskweaver.domain.notification.repository;

import backend.taskweaver.domain.notification.entity.NotificationMember;
import backend.taskweaver.domain.notification.entity.enums.isRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationMemberRepository extends JpaRepository<NotificationMember, Long> {
    List<NotificationMember> findByMemberId(Long memberId);

    boolean existsByMember_IdAndIsRead(Long memberId, isRead isRead);
}

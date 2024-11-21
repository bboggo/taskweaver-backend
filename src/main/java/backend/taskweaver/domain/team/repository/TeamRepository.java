package backend.taskweaver.domain.team.repository;

import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Object> findByIdAndTeamLeader(Long teamId, Long user);
    List<Team> findAllByTeamLeader(Long memberId);

    Optional<Team> findById(Long id);
}

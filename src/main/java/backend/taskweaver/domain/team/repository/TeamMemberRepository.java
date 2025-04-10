package backend.taskweaver.domain.team.repository;


import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.team.entity.Team;
import backend.taskweaver.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findAllByTeam(Team team);

    List<TeamMember> findAllByTeamId(Long teamId);

    void deleteByTeamIdAndMemberId(Long teamId, Long memberId);

    //Optional<TeamMember> findByTeamIdAndMemberId(Team team, Member member);

    Optional<TeamMember> findByTeamIdAndMemberId(Long teamId, Long memberId);
    List<TeamMember> findAllByMemberId(Long memberId);

    boolean existsByTeamAndMember(Team team, Member member);

    void deleteByMemberId(Long memberId);
}

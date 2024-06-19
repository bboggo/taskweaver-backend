package backend.taskweaver.domain.team.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class TeamRequest {

    @Getter
    public static class teamCreateRequest {
        @Schema(description = "팀 이름", example = "Team Name")
        String name;

        @Schema(description = "팀 소개", example = "Team Description")
        String description;
    }


    @Getter
    public static class teamMemberDeleteRequest {
        @Schema(description = "삭제 원하는 id 리스트 형태로", example = "[1, 2, 3]")
        List<Long> memberId;
    }

}

package kr.co.kwonshzzang.videogameleaderboard.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScoreEvent {
    private Long playerId;
    private Long productId;
    private Double score;
}

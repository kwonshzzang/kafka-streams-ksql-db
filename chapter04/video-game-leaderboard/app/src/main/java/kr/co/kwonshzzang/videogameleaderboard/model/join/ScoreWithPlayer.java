package kr.co.kwonshzzang.videogameleaderboard.model.join;

import kr.co.kwonshzzang.videogameleaderboard.model.Player;
import kr.co.kwonshzzang.videogameleaderboard.model.ScoreEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ScoreWithPlayer {
    private ScoreEvent scoreEvent;
    private Player player;
}

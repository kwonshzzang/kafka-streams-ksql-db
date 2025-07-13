package kr.co.kwonshzzang.videogameleaderboard.model.join;

import kr.co.kwonshzzang.videogameleaderboard.model.Product;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Enriched implements Comparable<Enriched> {
    private final Long playerId;
    private final Long productId;
    private final String playerName;
    private final String gameName;
    private final Double score;

    public Enriched(ScoreWithPlayer scoreWithPlayer, Product product) {
        this.playerId = scoreWithPlayer.getPlayer().getId();
        this.productId = product.getId();
        this.playerName = scoreWithPlayer.getPlayer().getName();
        this.gameName = product.getName();
        this.score = scoreWithPlayer.getScoreEvent().getScore();
    }


    @Override
    public int compareTo(Enriched o) {
        return Double.compare(o.getScore(), score);
    }
}

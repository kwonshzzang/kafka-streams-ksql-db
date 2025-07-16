package kr.co.kwonshzzang.videogameleaderboard;

import kr.co.kwonshzzang.videogameleaderboard.model.join.Enriched;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class HighScores {
    private final TreeSet<Enriched> highScores = new TreeSet<>();

    public HighScores add(Enriched enriched) {
        highScores.add(enriched);

        // keep only the top 3 high scores
        if(highScores.size() > 3)
            highScores.remove(highScores.last());

        return this;
    }

    public List<Enriched> toList() {
        return new ArrayList<>(highScores);
    }
}

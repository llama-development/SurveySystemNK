package net.lldv.surveysystem.components.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class Survey {

    private final String title;
    private final String text;
    private final Status status;
    private final String id;
    private final long time;
    private final Map<String, Integer> votedPlayers;

    public enum Status {
        OPEN, CLOSED
    }

}

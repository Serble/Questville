package net.serble.questville.schemas;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class NpcQuestion {
    private final String npcName;
    private final HashMap<String, ConfigurationSection> answerActions;

    public NpcQuestion(String npcName, HashMap<String, ConfigurationSection> answerActions) {
        this.npcName = npcName;
        this.answerActions = answerActions;
    }

    public String getNpcName() {
        return npcName;
    }

    public HashMap<String, ConfigurationSection> getAnswerActions() {
        return answerActions;
    }
}

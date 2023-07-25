package net.serble.questville.schemas.items;

public class ItemRarity {
    private final String name;
    private final String display;

    public ItemRarity(String name, String display) {
        this.name = name;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }
}

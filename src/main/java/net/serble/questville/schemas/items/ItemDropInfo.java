package net.serble.questville.schemas.items;

public class ItemDropInfo {
    private final String resource;
    private final String dropType;
    private final int money;
    private final int dropAmount;

    public ItemDropInfo(String resource, String dropType, int money, int dropAmount) {
        this.resource = resource;
        this.dropType = dropType;
        this.money = money;
        this.dropAmount = dropAmount;
    }

    public String getResource() {
        return resource;
    }

    public String getDropType() {
        return dropType;
    }

    public int getMoney() {
        return money;
    }

    public int getDropAmount() {
        return dropAmount;
    }
}

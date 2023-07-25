package net.serble.questville;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.serble.questville.schemas.NpcQuestion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class NpcManager {
    private HashMap<String, ConfigurationSection> npcSections;
    private final HashMap<UUID, NpcQuestion> askedQuestions = new HashMap<>();

    public NpcManager() {
        Questville.getInstance().saveResource("npcs.yml", false);

        YmlConfig npcList;
        try {
            npcList = new YmlConfig("npcs.yml");
        } catch (Exception e) {
            Questville.getInstance().disableDueToError(e);
            return;
        }

        npcSections = new HashMap<>();
        ConfigurationSection npcSection = npcList.getConfiguration().getConfigurationSection("npcs");
        assert npcSection != null;

        for (String npcKey : npcSection.getKeys(false)) {
            ConfigurationSection npcConfig = npcSection.getConfigurationSection(npcKey);
            assert npcConfig != null;
            npcSections.put(npcKey, npcConfig);
        }
    }

    public void playerInteract(Player p, String npcId) {
        ConfigurationSection npcConfig = npcSections.get(npcId);
        assert npcConfig != null;

        String name = npcConfig.getString("name");

        ConfigurationSection actions = Objects.requireNonNull(npcConfig.getConfigurationSection("actions"));
        for (String actionKey : actions.getKeys(false)) {
            ConfigurationSection actionSection = actions.getConfigurationSection(actionKey);
            assert actionSection != null;
            runAction(p, actionSection, name);
        }
    }

    private void runActions(Player p, ConfigurationSection section, String npcName) {
        for (String actionKey : section.getKeys(false)) {
            ConfigurationSection actionSection = section.getConfigurationSection(actionKey);
            assert actionSection != null;
            runAction(p, actionSection, npcName);
        }
    }

    private void runAction(Player p, ConfigurationSection actionSection, String npcName) {
        String type = actionSection.getString("type");
        assert type != null;

        switch (type) {
            case "speak": {
                String message = actionSection.getString("text");
                assert message != null;
                p.sendMessage(Utils.t("&e[" + npcName + "] &r" + message));
                return;
            }

            case "ifhasresource": {
                String resource = actionSection.getString("resource");
                int count = actionSection.getInt("count");
                assert resource != null;
                assert count != 0;
                if (p.getInventory().containsAtLeast(Questville.getInstance().getItemManager().getResource(resource), count)) {
                    runActions(p, Objects.requireNonNull(actionSection.getConfigurationSection("actions")), npcName);
                    return;
                }
                // Else
                ConfigurationSection elseSection = actionSection.getConfigurationSection("else");
                if (elseSection != null) {
                    runActions(p, elseSection, npcName);
                }
                return;
            }

            case "ifhasitem": {
                String item = actionSection.getString("item");
                int count = actionSection.getInt("count");
                assert item != null;
                assert count != 0;
                if (Questville.getInstance().getItemManager().hasItem(p, item, count)) {
                    runActions(p, Objects.requireNonNull(actionSection.getConfigurationSection("actions")), npcName);
                    return;
                }
                // Else
                ConfigurationSection elseSection = actionSection.getConfigurationSection("else");
                if (elseSection != null) {
                    runActions(p, elseSection, npcName);
                }
                return;
            }

            case "takeresource": {
                String resourceTake = actionSection.getString("resource");
                int countTake = actionSection.getInt("count");
                assert resourceTake != null;
                assert countTake != 0;
                ItemStack remove = Questville.getInstance().getItemManager().getResource(resourceTake);
                remove.setAmount(countTake);
                p.getInventory().removeItem(remove);
                return;
            }

            case "takeitem": {
                String itemTake = actionSection.getString("item");
                assert itemTake != null;
                Questville.getInstance().getItemManager().removeItemFromPlayer(p, itemTake, 1);
                return;
            }

            case "giveresource": {
                String resourceGive = actionSection.getString("resource");
                int countGive = actionSection.getInt("count");
                assert resourceGive != null;
                assert countGive != 0;
                ItemStack give = Questville.getInstance().getItemManager().getResource(resourceGive);
                give.setAmount(countGive);
                p.getInventory().addItem(give);
                return;
            }

            case "giveitem": {
                String itemGive = actionSection.getString("item");
                assert itemGive != null;
                ItemStack give = Questville.getInstance().getItemManager().createItem(p, itemGive);
                p.getInventory().addItem(give);
                return;
            }

            case "ifholding": {
                String resourceHolding = actionSection.getString("resource");
                assert resourceHolding != null;
                if (p.getInventory().getItemInMainHand().isSimilar(Questville.getInstance().getItemManager().getResource(resourceHolding))) {
                    runActions(p, Objects.requireNonNull(actionSection.getConfigurationSection("actions")), npcName);
                    return;
                }
                // Else
                ConfigurationSection elseSectionHolding = actionSection.getConfigurationSection("else");
                if (elseSectionHolding != null) {
                    runActions(p, elseSectionHolding, npcName);
                }
                return;
            }

            case "hasmoney": {
                int money = actionSection.getInt("amount");
                if (Questville.getInstance().getItemManager().hasAmount(p, money)) {
                    runActions(p, Objects.requireNonNull(actionSection.getConfigurationSection("actions")), npcName);
                    return;
                }
                // Else
                ConfigurationSection elseSectionMoney = actionSection.getConfigurationSection("else");
                if (elseSectionMoney != null) {
                    runActions(p, elseSectionMoney, npcName);
                }
                return;
            }

            case "charge": {
                int money = actionSection.getInt("amount");
                Questville.getInstance().getItemManager().chargePlayerAmount(p, money);
                return;
            }

            case "pay": {
                int money = actionSection.getInt("amount");
                ItemStack[] moneys = Questville.getInstance().getItemManager().getCurrencyMatchingAmount(money);
                for (ItemStack m : moneys) {
                    p.getInventory().addItem(m);
                }
                return;
            }

            case "givepotioneffect": {
                String effect = actionSection.getString("effect");
                int duration = actionSection.getInt("duration");
                int amplifier = actionSection.getInt("amplifier");
                assert effect != null;
                assert duration != 0;
                assert amplifier != 0;
                PotionEffectType potionEffectType = PotionEffectType.getByName(effect);
                assert potionEffectType != null;
                PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, amplifier);
                p.addPotionEffect(potionEffect);
                return;
            }

            case "command": {
                String command = actionSection.getString("command");
                assert command != null;
                command = command.replace("%player%", p.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                return;
            }

            case "question": {
                String question = actionSection.getString("question");
                assert question != null;

                ConfigurationSection options = actionSection.getConfigurationSection("options");
                assert options != null;

                ConfigurationSection labelsSection = actionSection.getConfigurationSection("labels");
                assert labelsSection != null;

                HashMap<String, String> optionLabels = new HashMap<>();
                HashMap<String, ConfigurationSection> optionSections = new HashMap<>();
                for (String optionKey : options.getKeys(false)) {
                    ConfigurationSection optionSection = options.getConfigurationSection(optionKey);
                    assert optionSection != null;
                    optionSections.put(optionKey, optionSection);
                }

                for (String labelKey : labelsSection.getKeys(false)) {
                    String label = labelsSection.getString(labelKey);
                    assert label != null;
                    optionLabels.put(labelKey, label);
                }

                TextComponent msg = new TextComponent(Utils.t(question + " "));
                for (String optionKey : optionSections.keySet()) {
                    ConfigurationSection optionSection = optionSections.get(optionKey);
                    assert optionSection != null;
                    String label = optionLabels.get(optionKey);
                    assert label != null;
                    TextComponent option = new TextComponent(Utils.t(label + " "));
                    option.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/questville answerquestion " + optionKey));
                    Content hoverContent = new Text("Click to respond");
                    option.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent));
                    msg.addExtra(option);
                }

                askedQuestions.put(p.getUniqueId(), new NpcQuestion(npcName, optionSections));
                p.spigot().sendMessage(msg);
                break;
            }

            case "delay": {
                int delay = actionSection.getInt("delay");
                assert delay != 0;
                ConfigurationSection actions = actionSection.getConfigurationSection("actions");
                assert actions != null;
                Bukkit.getScheduler().runTaskLater(Questville.getInstance(), () -> runActions(p, actions, npcName), delay);
                break;
            }

            case "random": {
                ConfigurationSection actions = actionSection.getConfigurationSection("actions");
                assert actions != null;
                List<String> keys = new ArrayList<>(actions.getKeys(false));
                String randomKey = keys.get(new Random().nextInt(keys.size()));
                ConfigurationSection randomSection = actions.getConfigurationSection(randomKey);
                assert randomSection != null;
                runAction(p, randomSection, npcName);
                break;
            }

            case "group": {
                ConfigurationSection actions = actionSection.getConfigurationSection("actions");
                assert actions != null;
                runActions(p, actions, npcName);
                break;
            }

        }
    }

    public void answerQuestion(Player p, String answerKey) {
        NpcQuestion question = askedQuestions.get(p.getUniqueId());
        if (question == null) {
            p.sendMessage(Utils.t("&cYou have no question to answer"));
            return;
        }
        ConfigurationSection answerSection = question.getAnswerActions().get(answerKey);
        assert answerSection != null;
        runActions(p, answerSection, question.getNpcName());
        askedQuestions.remove(p.getUniqueId());
    }

    public String getName(String id) {
        ConfigurationSection npcConfig = npcSections.get(id);
        assert npcConfig != null;
        return npcConfig.getString("name");
    }

    public boolean doesNpcExist(String id) {
        return npcSections.containsKey(id);
    }

}

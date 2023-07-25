package net.serble.questville;

import net.serble.questville.enchants.CustomEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class QuestvilleCommand implements CommandExecutor {
    public QuestvilleCommand() {
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("questville")).setExecutor(this);
    }

    private boolean hasPerm(CommandSender sender) {
        if (!sender.hasPermission("questville.command")) {
            e(sender, "You do not have permission to use this command.");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendAllUsage(sender);
            return true;
        }

        switch (args[0]) {
            case "npc": {
                if (!hasPerm(sender)) return true;
                if (args.length != 3) {
                    e(sender, "/questville npc <npc name> <player>");
                    return true;
                }
                String npcName = args[1];
                String playerName = args[2];
                Player player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    e(sender, "Player not found.");
                    return true;
                }

                if (!Questville.getInstance().getNpcManager().doesNpcExist(npcName)) {
                    e(sender, "NPC not found.");
                    return true;
                }
                Questville.getInstance().getNpcManager().playerInteract(player, npcName);
                break;
            }

            case "giveresource": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville giveresource <resource>");
                    return true;
                }
                String resource = args[1];
                Player p = (Player) sender;
                ItemStack item = Questville.getInstance().getItemManager().getResource(resource);
                if (item == null) {
                    e(sender, "Resource not found.");
                    return true;
                }
                p.getInventory().addItem(item);
                s(sender, "&aResource given.");
                break;
            }

            case "giveitem": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville giveitem <item>");
                    return true;
                }
                String itemname = args[1];
                Player p = (Player) sender;
                ItemStack item2 = Questville.getInstance().getItemManager().createItem(p, itemname);
                if (item2 == null) {
                    e(sender, "Item not found.");
                    return true;
                }
                p.getInventory().addItem(item2);
                s(sender, "&aItem given.");
                break;
            }

            case "givemoney": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville givemoney <amount>");
                    return true;
                }
                int amount = Integer.parseInt(args[1]);
                Player p = (Player) sender;

                ItemStack[] items = Questville.getInstance().getItemManager().getCurrencyMatchingAmount(amount);
                for (ItemStack item : items) {
                    p.getInventory().addItem(item);
                }
                s(sender, "&aMoney given.");
                break;
            }

            case "updateitem": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                ItemStack heldItem = ((Player) sender).getInventory().getItemInMainHand();
                Questville.getInstance().getItemManager().updateLore(heldItem);
                s(sender, "&aItem updated.");
                break;
            }

            case "charge": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville charge <amount>");
                    return true;
                }
                int amount = Integer.parseInt(args[1]);
                Player p = (Player) sender;
                Questville.getInstance().getItemManager().chargePlayerAmount(p, amount);
                s(sender, "&aCharged.");
                break;
            }

            case "hasamount": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville hasamount <amount>");
                    return true;
                }
                int amount = Integer.parseInt(args[1]);
                Player p = (Player) sender;
                boolean hasAmount = Questville.getInstance().getItemManager().hasAmount(p, amount);
                s(sender, "&aHas amount: " + hasAmount);
                break;
            }

            case "openbank": {
                if (!hasPerm(sender)) return true;
                if (args.length != 2) {
                    e(sender, "/questville openbank <player>");
                    return true;
                }
                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    e(sender, "Player not found.");
                    return true;
                }
                Questville.getInstance().getBankManager().openBank(player);
                break;
            }

            case "answerquestion": {
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville answerquestion <answer>");
                    return true;
                }
                String answer = args[1];
                Player p = (Player) sender;
                Questville.getInstance().getNpcManager().answerQuestion(p, answer);
                break;
            }

            case "spawnmob": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 2) {
                    e(sender, "/questville spawnmob <mob>");
                    return true;
                }
                String mob = args[1];
                Player p = (Player) sender;
                Questville.getInstance().getMobManager().spawnMob(mob, p.getLocation());
                s(p, "&aMob spawned.");
                break;
            }

            case "enchant": {
                if (!hasPerm(sender)) return true;
                if (!(sender instanceof Player)) {
                    e(sender, "Only players can use this command.");
                    return true;
                }
                if (args.length != 3) {
                    e(sender, "/questville enchant <enchantment> <level>");
                    return true;
                }

                String enchantment = args[1];
                int level = Integer.parseInt(args[2]);

                Player p = (Player) sender;
                ItemStack item = p.getInventory().getItemInMainHand();

                Questville.getInstance().getCustomEnchantsManager().enchantItem(item, CustomEnchantment.valueOf(enchantment), level);
                s(p, "&aEnchanted.");
                break;
            }

            default:
                if (!hasPerm(sender)) return true;
                sendAllUsage(sender);
                break;
        }

        return true;
    }

    private void sendAllUsage(CommandSender p) {
        e(p, "Please provide a subcommand.");
        e(p, "/questville npc <npc name> <player> - Trigger NPC action for player");
        e(p, "/questville giveresource <resource> - Give player resource");
        e(p, "/questville giveitem <item> - Give player item");
        e(p, "/questville givemoney <amount> - Give player money");
        e(p, "/questville updateitem - Update lore of item in hand");
        e(p, "/questville charge <amount> - Charge player currency amount");
        e(p, "/questville hasamount <amount> - Check if player has currency amount");
        e(p, "/questville openbank <player> - Open bank for player");
        e(p, "/questville answerquestion <answer> - Answer question from an NPC");
        e(p, "/questville spawnmob <mob> - Spawn mob");
        e(p, "/questville enchant <enchantment> <level> - Enchant item");
    }

    private void s(CommandSender p, String msg) {
        p.sendMessage(Utils.t(msg));
    }

    private void e(CommandSender p, String msg) {
        s(p, "&c" + msg);
    }
}

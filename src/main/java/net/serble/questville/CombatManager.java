package net.serble.questville;

import net.serble.questville.enchants.CustomEnchantment;
import net.serble.questville.schemas.CustomMob;
import net.serble.questville.schemas.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CombatManager implements Listener {

    public CombatManager() {
        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity)) {
            return;
        }
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity attacker = (LivingEntity) e.getDamager();
        LivingEntity defender = (LivingEntity) e.getEntity();
        ItemStack attackItem = Objects.requireNonNull(attacker.getEquipment()).getItemInMainHand();

        // Damage
        float damage = 0;
        CustomMob mob = Questville.getInstance().getMobManager().getMob(attacker);
        if (mob != null) {
            damage = mob.getDamage();
        }

        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            damage = Questville.getInstance().getPlayerPropertyManager().getPlayerBaseDamage(player);
        }

        CustomItem item = Questville.getInstance().getItemManager().getItem(attackItem);
        if (item != null) {
            float itemDamage = item.getDamage();
            int sharpnessLevel = Questville.getInstance().getCustomEnchantsManager().getEnchantmentLevel(attackItem, CustomEnchantment.Sharpness);
            itemDamage += sharpnessLevel * 1.25;
            damage += itemDamage;
        }

        // Defence
        float defence = 0;
        mob = Questville.getInstance().getMobManager().getMob(defender);
        if (mob != null) {
            defence = 0;
        }

        if (defender instanceof Player) {
            Player player = (Player) defender;
            defence = Questville.getInstance().getPlayerPropertyManager().getPlayerDefence(player);
        }

        damage -= defence;
        if (damage < 0) {
            damage = 0;
        }
        e.setDamage(damage);
    }

}

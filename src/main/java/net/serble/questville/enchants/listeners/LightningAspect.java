package net.serble.questville.enchants.listeners;

import net.serble.questville.Questville;
import net.serble.questville.enchants.CustomEnchantment;
import net.serble.questville.enchants.EnchantListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LightningAspect extends EnchantListener {

    public CustomEnchantment getEnchant() {
        return CustomEnchantment.LightningAspect;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) e.getDamager();
        if (entity.getEquipment() == null) {
            return;
        }

        int enchantLevel = Questville.getInstance().getCustomEnchantsManager().getEnchantmentLevel(entity.getEquipment().getItemInMainHand(), getEnchant());

        double percentChance = 0.05 * enchantLevel;
        if (Math.random() < percentChance) {
            e.getEntity().getWorld().strikeLightning(e.getEntity().getLocation());
        }
    }


}

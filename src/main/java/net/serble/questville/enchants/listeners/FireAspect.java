package net.serble.questville.enchants.listeners;

import net.serble.questville.Questville;
import net.serble.questville.enchants.EnchantListener;
import net.serble.questville.enchants.CustomEnchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FireAspect extends EnchantListener {

    public CustomEnchantment getEnchant() {
        return CustomEnchantment.FireAspect;
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
        e.getEntity().setFireTicks(enchantLevel * 80);
    }


}

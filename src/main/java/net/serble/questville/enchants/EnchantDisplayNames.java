package net.serble.questville.enchants;

import java.util.HashMap;

public class EnchantDisplayNames {
    private static final HashMap<CustomEnchantment, String> displayNames = new HashMap<>() {{
            put(CustomEnchantment.FireAspect, "Fire Aspect");
            put(CustomEnchantment.LightningAspect, "Lightning Aspect");
            put(CustomEnchantment.Sharpness, "Sharpness");
        }};

    public static String getDisplayName(CustomEnchantment enchantment) {
        return displayNames.get(enchantment);
    }
}

package jp.reitou_mugicha.supportCrossplay.enchantment;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EnchantmentPoisonAspect implements Listener
{
    private final String ENCHANT_ID = "poison_aspect";

    @EventHandler
    public void onHitEntity(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!Helpers.hasEnchantment(mainHand, ENCHANT_ID)) return;

        if (event.getEntity() instanceof LivingEntity target)
        {
            Enchantment enchantment = Helpers.getEnchantment(mainHand, ENCHANT_ID);
            if (enchantment == null) return;

            int level = mainHand.getEnchantmentLevel(enchantment);
            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.POISON,
                    40 + level * 20,
                    level - 1
            ));
        }
    }
}
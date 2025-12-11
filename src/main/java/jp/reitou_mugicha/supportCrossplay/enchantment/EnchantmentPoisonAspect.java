package jp.reitou_mugicha.supportCrossplay.enchantment;

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
    @EventHandler
    public void onHitEntity(EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player player)) return;

        var enchants = player.getInventory().getItemInMainHand().getEnchantments();

        for (var entry : enchants.entrySet())
        {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();

            if (ench.getKey().getKey().equals("poison_aspect"))
            {
                if (e.getEntity() instanceof LivingEntity target)
                {
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON,
                            40 + level * 20,
                            level - 1
                    ));
                }
            }
        }
    }
}
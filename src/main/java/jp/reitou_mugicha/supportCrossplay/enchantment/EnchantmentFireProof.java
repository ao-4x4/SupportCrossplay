package jp.reitou_mugicha.supportCrossplay.enchantment;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnchantmentFireProof implements Listener
{
    private final String ENCHANT_ID = "fire_proof";

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Item item)) return;
        if (!Helpers.hasEnchantment(item.getItemStack(), ENCHANT_ID)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)
        {
            event.setCancelled(true);
        }
    }
}
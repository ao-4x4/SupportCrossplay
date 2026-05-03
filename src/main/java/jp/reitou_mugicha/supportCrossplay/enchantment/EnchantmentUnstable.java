package jp.reitou_mugicha.supportCrossplay.enchantment;

import io.papermc.paper.event.entity.EntityDamageItemEvent;
import jp.reitou_mugicha.supportCrossplay.Helpers;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantmentUnstable implements Listener
{
    private final String ENCHANT_ID = "unstable";
    
    @EventHandler
    public void onPlayerDamageItem(PlayerItemDamageEvent event)
    {
        ItemStack itemStack = event.getItem();
        Player player = event.getPlayer();
        if (itemStack == null) return;
        if (!Helpers.hasEnchantment(itemStack, ENCHANT_ID)) return;

        if (Helpers.probability(20))
        {
            itemStack.setAmount(0);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
        }
    }
}

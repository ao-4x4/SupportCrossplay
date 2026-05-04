package jp.reitou_mugicha.supportCrossplay.enchantment;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentSoulbound implements Listener
{
    private final String ENCHANT_ID = "soulbound";

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        List<ItemStack> drops = event.getDrops();
        List<ItemStack> itemsToKeep = new ArrayList<>();

        drops.removeIf(item -> {
            if (Helpers.hasEnchantment(item, ENCHANT_ID)) {
                itemsToKeep.add(item);
                return true;
            }
            return false;
        });

        if (!itemsToKeep.isEmpty()) {
            event.getItemsToKeep().addAll(itemsToKeep);
        }
    }
}

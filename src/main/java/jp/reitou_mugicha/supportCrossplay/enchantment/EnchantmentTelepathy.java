package jp.reitou_mugicha.supportCrossplay.enchantment;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentTelepathy implements Listener
{
    private final String ENCHANT_ID = "telepathy";

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event)
    {
        Player player = event.getPlayer();
        if (player == null) return;

        Inventory inventory = player.getInventory();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!Helpers.hasEnchantment(mainHand, ENCHANT_ID)) return;

        event.setCancelled(true);

        var drops = event.getItems();
        List<Item> overflowedItems = new ArrayList<>();

        for (var drop : drops)
        {
            if (inventory.firstEmpty() == -1)
            {
                overflowedItems.add(drop);
                continue;
            }

            inventory.addItem(drop.getItemStack());
        }

        for (var item : overflowedItems)
        {
            player.getWorld().dropItemNaturally(event.getBlock().getLocation(), item.getItemStack());
        }
    }
}
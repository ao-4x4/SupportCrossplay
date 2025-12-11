package jp.reitou_mugicha.supportCrossplay.feature;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FeatureRepairAnvil implements Listener
{
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (mainHand.getType() != Material.IRON_BLOCK) return;
        if (mainHand.getAmount() < 1) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        switch (block.getType())
        {
            case CHIPPED_ANVIL:
                block.setType(Material.ANVIL);
                consume(player);
                break;
            case DAMAGED_ANVIL:
                block.setType(Material.CHIPPED_ANVIL);
                consume(player);
                break;
        }
    }

    private void consume(Player player)
    {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        mainHand.setAmount(mainHand.getAmount() - 1);
        player.sendMessage(ChatColor.GREEN + "金床を修繕しました！");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
    }
}
package jp.reitou_mugicha.supportCrossplay.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FeatureReActiveShrieker implements Listener
{
    private final Material CONSUME_ITEM = Material.ECHO_SHARD;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SCULK_SHRIEKER) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null || item.getType() != CONSUME_ITEM) return;

        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (!(block.getBlockData() instanceof SculkShrieker shriekerData)) return;

        Player player = event.getPlayer();

        if (shriekerData.isCanSummon())
        {
            player.sendMessage(ChatColor.RED + "すでに有効化されています！");
            return;
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        shriekerData.setCanSummon(true);
        block.setBlockData(shriekerData);

        block.getWorld().playSound(
                block.getLocation(),
                Sound.BLOCK_SCULK_CHARGE,
                SoundCategory.BLOCKS,
                1.0f,
                1.2f
        );

        block.getWorld().spawnParticle(
                Particle.SCULK_CHARGE_POP,
                block.getLocation().add(0.5, 0.8, 0.5),
                20,
                0.2, 0.2, 0.2, 0.05
        );

        player.sendMessage(ChatColor.GREEN + "スカルクシュリーカーを有効化しました！");

        event.setCancelled(true);
    }
}
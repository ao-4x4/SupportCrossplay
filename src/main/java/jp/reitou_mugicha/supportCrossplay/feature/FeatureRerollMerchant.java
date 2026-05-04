package jp.reitou_mugicha.supportCrossplay.feature;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FeatureRerollMerchant implements Listener
{
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (mainHand.getType() != Material.LECTERN) return;

        if (villager.getVillagerLevel() > 1) {
            return;
        }

        Villager.Profession oldProfession = villager.getProfession();

        if (oldProfession == Villager.Profession.NONE || oldProfession == Villager.Profession.NITWIT) {
            return;
        }

        villager.setProfession(Villager.Profession.NONE);

        villager.setProfession(oldProfession);

        player.playSound(villager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "職業を再抽選しました！");
    }
}
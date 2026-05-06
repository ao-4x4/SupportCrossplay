package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class FeatureReapJobs implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();

        if (!player.isSneaking() && Helpers.isHoe(player.getItemInHand()) && event.getRightClicked().getType() == EntityType.VILLAGER)
        {
            Villager villager = (Villager) event.getRightClicked();
            Villager.Profession profession = villager.getProfession();

            if ((profession != Villager.Profession.NITWIT) || (player.getItemInHand().getType() == Material.NETHERITE_HOE && profession == Villager.Profession.NITWIT))
            {
                villager.setProfession(Villager.Profession.NONE);
                villager.setVillagerExperience(0);
                villager.setVillagerLevel(1);

                villager.setMemory(MemoryKey.JOB_SITE, null);
                villager.setMemory(MemoryKey.HOME, null);
                villager.setMemory(MemoryKey.POTENTIAL_JOB_SITE, null);
                villager.setMemory(MemoryKey.MEETING_POINT, null);

                player.sendMessage(ChatColor.GREEN + "対象の村人の職業を消しました!");
                player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_HIT, 1f, 1f);
                player.getWorld().spawnParticle(
                        Particle.SWEEP_ATTACK,
                        villager.getLocation().add(0, 1, 0),
                        1
                );
            }
        }
    }
}
package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.item.ItemEnchantShard;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FeaturePiglinBarter implements Listener
{
    private final Set<Material> bannedItems = Set.of(
            Material.ENDER_PEARL,
            Material.OBSIDIAN
    );

    private final List<Material> allowedLoot = List.of(
            Material.STRING,
            Material.LEATHER,
            Material.GRAVEL,
            Material.NETHER_BRICK,
            Material.IRON_NUGGET,
            Material.QUARTZ,
            Material.SOUL_SAND,
            Material.SPECTRAL_ARROW,
            Material.CRYING_OBSIDIAN,
            Material.FIRE_CHARGE,
            Material.AMETHYST_SHARD
    );

    @EventHandler
    public void onPiglinBarter(PiglinBarterEvent event)
    {
        if (!Helpers.isEconomy()) return;
        List<ItemStack> newLoot = new ArrayList<>();
        for (ItemStack item : event.getOutcome())
        {
            Material material = item.getType();
            if (material == Material.AMETHYST_SHARD)
            {
                newLoot.add(ItemEnchantShard.getItem());
            }
            else if (bannedItems.contains(material))
            {
                ItemStack rerolledItem = rerollItem();
                newLoot.add(rerolledItem);
            }
            else
            {
                newLoot.add(item);
            }
        }

        event.getOutcome().clear();
        event.getOutcome().addAll(newLoot);
    }

    private ItemStack rerollItem()
    {
        int maxTries = 10;

        for (int i = 0; i < maxTries; i++)
        {
            Material selected = allowedLoot.get(SupportCrossplay.random.nextInt(allowedLoot.size()));
            if (!bannedItems.contains(selected))
            {
                return new ItemStack(selected, 1);
            }
        }

        return ItemEnchantShard.getItem();
    }
}
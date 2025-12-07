package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.Helpers;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemEnchantShard implements Listener
{
    private static final ItemStack Item;
    private static final String ITEM_TAG = "enchant_shard";;

    static {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("エンチャントの欠片");
        meta.setLore(List.of("スポーンの村人とエンチャント本を交換できます。"));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(SupportCrossplay.KEY, ITEM_TAG);
        item = nbtItem.getItem();

        Item = item.clone();

        SupportCrossplay.addItem(item);
    }

    public static ItemStack getItem()
    {
        return Item.clone();
    }
    public static boolean is(ItemStack item)
    {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey(SupportCrossplay.KEY) && nbtItem.getString(SupportCrossplay.KEY).equals(ITEM_TAG);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (Helpers.isContainShard(event.getBlock()))
        {
            if (Helpers.probability(3))
            {
                World world = event.getBlock().getWorld();
                world.dropItemNaturally(event.getBlock().getLocation(), getItem()).setGlowing(true);

                Player player = event.getPlayer();
                player.sendMessage(ChatColor.GREEN + "エンチャントの欠片がドロップしました！");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        // --- 村人司書処理（既存） ---
        if (event.getEntity() instanceof Villager villager)
        {
            if (villager.getProfession() == Villager.Profession.LIBRARIAN)
            {
                if (Helpers.probability(0.5))
                {
                    World world = event.getEntity().getWorld();
                    world.dropItemNaturally(event.getEntity().getLocation(), getItem()).setGlowing(true);

                    Player killer = event.getEntity().getKiller();
                    if (killer != null)
                    {
                        killer.sendMessage(ChatColor.GREEN + "エンチャントの欠片がドロップしました！");
                        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }
                }
            }
            return;
        }

        switch (event.getEntity().getType()) {
            case ZOMBIE:
            case HUSK:
            case DROWNED:
            case SKELETON:
            case STRAY:
            case CREEPER:
            case WITCH:
            case SPIDER:
            case CAVE_SPIDER:
                break;
            default:
                return;
        }

        if (Helpers.probability(6))
        {
            World world = event.getEntity().getWorld();
            world.dropItemNaturally(event.getEntity().getLocation(), getItem()).setGlowing(true);

            Player killer = event.getEntity().getKiller();
            if (killer != null)
            {
                killer.sendMessage(ChatColor.GREEN + "エンチャントの欠片がドロップしました！");
                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
    }
}
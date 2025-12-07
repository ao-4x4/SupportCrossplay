package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemStarterPack implements Listener
{
    private static final ItemStack Item;
    private static final String ITEM_TAG = "starter_pack";

    static {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("スターターパック");
        meta.setLore(List.of("右クリックでスターターパックをゲット！"));
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
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!is(mainHand)) return;

        player.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
        player.getInventory().addItem(ItemStack.of(Material.WOODEN_AXE));
        player.getInventory().addItem(ItemStack.of(Material.WOODEN_PICKAXE));
        player.getInventory().addItem(ItemStack.of(Material.WOODEN_SHOVEL));
        player.getInventory().addItem(ItemStack.of(Material.BREAD, 10));
        player.sendMessage(ChatColor.GREEN + "スターターパックを開封しました！");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }
}
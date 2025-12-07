package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class ItemSmithingTableOnAStick implements Listener
{
    private static final ItemStack Item;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "smithing_table_on_a_stick");
    private static final String ITEM_TAG = "smithing_table_on_a_stick";

    static {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("鍛冶台付きの棒");
        meta.setLore(List.of("その場で鍛冶台が使えます。"));

        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setStrings(List.of("smithing_table_on_a_stick"));
        meta.setCustomModelDataComponent(component);

        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(SupportCrossplay.KEY, ITEM_TAG);
        item = nbtItem.getItem();

        Item = item.clone();

        SupportCrossplay.addItem(item);
        registerRecipe();
    }

    public static ItemStack getItem()
    {
        return Item.clone();
    }

    public static void registerRecipe()
    {
        if (Bukkit.getRecipe(recipeKey) != null) return;

        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, Item);
        recipe.addIngredient(Material.STICK);
        recipe.addIngredient(Material.SMITHING_TABLE);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
            if (mainHandItem == null || mainHandItem.getType() == Material.AIR || mainHandItem.getAmount() == 0) return;

            NBTItem nbt = new NBTItem(mainHandItem);
            if (!ITEM_TAG.equals(nbt.getString(SupportCrossplay.KEY))) return;

            Player player = event.getPlayer();
            player.openSmithingTable(player.getLocation(), true);
        }
    }
}

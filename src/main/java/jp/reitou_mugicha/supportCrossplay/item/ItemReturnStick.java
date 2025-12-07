package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.data.HomeData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemReturnStick implements Listener
{
    private static final ItemStack Item;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "return_stick");
    private static final String ITEM_TAG = "return_stick";

    static {
        ItemStack item = new ItemStack(Material.BREEZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("帰還棒");
        meta.setLore(List.of("設定した拠点に帰ります。"));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
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

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, Item);
        recipe.shape(" C ", " L ", " S ");
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('C', Material.COMPASS);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (item == null || item.getType() == Material.AIR) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            NBTItem nbt = new NBTItem(item);
            if (nbt.hasKey(SupportCrossplay.KEY) && nbt.getString(SupportCrossplay.KEY).equals(ITEM_TAG))
            {
                if (!player.isSneaking())
                {
                    player.sendMessage("しゃがみながら右クリックをすると拠点に帰れます。");
                    return;
                }

                String path = player.getUniqueId() + ".position";
                if (HomeData.config.isLocation(path))
                {
                    Location location = HomeData.config.getLocation(path);
                    player.teleport(new Location(location.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5));
                    player.sendMessage("拠点にテレポートしました！");
                } else {
                    player.sendMessage("拠点が設定されていません。");
                }
            }
        }
    }
}

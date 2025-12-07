package jp.reitou_mugicha.supportCrossplay.item;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemShikattoStick
{
    private static final ItemStack Item;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "shikatto_stick");

    static {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("しかっと棒");
        meta.setLore(List.of("クロちゃんです！"));
        meta.addEnchant(Enchantment.KNOCKBACK, 30, true);
        item.setItemMeta(meta);
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
        recipe.shape("   ", " B ", " B ");
        recipe.setIngredient('B', Material.BLAZE_ROD);
        Bukkit.addRecipe(recipe);
    }
}
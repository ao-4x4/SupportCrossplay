package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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

public class ItemBedrockDestroyer implements Listener
{
    private static final ItemStack Item;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "bedrock_destroyer");
    private static final String ITEM_TAG = "bedrock_destroyer";

    static {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("岩盤破壊棒");
        meta.setLore(List.of("岩盤に向けて右クリックをすると岩盤が破壊されます。。"));
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
        recipe.shape("TTT", "OPO", "TTT");
        recipe.setIngredient('T', Material.TNT);
        recipe.setIngredient('P', Material.PISTON);
        recipe.setIngredient('O', Material.OBSIDIAN);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
        if (mainHandItem == null || mainHandItem.getType() == Material.AIR || mainHandItem.getAmount() == 0) return;

        NBTItem nbt = new NBTItem(mainHandItem);
        if (!ITEM_TAG.equals(nbt.getString(SupportCrossplay.KEY))) return;

        if (event.getClickedBlock().getType() == Material.BEDROCK) {
            Location location = event.getClickedBlock().getLocation();
            World world = location.getWorld();
            event.getClickedBlock().setType(Material.AIR);

            world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
            world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            mainHandItem.setAmount(mainHandItem.getAmount() - 1);
        }
    }
}
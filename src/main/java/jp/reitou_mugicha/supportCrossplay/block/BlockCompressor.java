package jp.reitou_mugicha.supportCrossplay.block;

import de.tr7zw.nbtapi.NBTBlock;
import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BlockCompressor implements Listener
{
    private static final ItemStack Block;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "compressor");
    private static final String BLOCK_TAG = "compressor";

    static {
        ItemStack block = new ItemStack(Material.IRON_BLOCK);
        ItemMeta meta = block.getItemMeta();
        meta.setDisplayName("圧縮機");
        meta.setLore(List.of("鉱石をブロックに変換します。"));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        block.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(block);
        nbtItem.setString(SupportCrossplay.KEY, BLOCK_TAG);
        block = nbtItem.getItem();

        Block = block.clone();

        SupportCrossplay.addItem(block);
    }

    public static ItemStack getItem()
    {
        return Block.clone();
    }
    public static boolean is(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR || item.isEmpty()) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.getString(SupportCrossplay.KEY).equals(BLOCK_TAG);
    }
    public static boolean is(Block block)
    {
        if (block == null || block.getType() == Material.AIR || block.isEmpty()) return false;
        NBTBlock blockNBT = new NBTBlock(block);
        return blockNBT.getData().getString(SupportCrossplay.KEY).equals(BLOCK_TAG);
    }

    public static void registerRecipe()
    {
        if (Bukkit.getRecipe(recipeKey) != null) return;

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, Block);
        recipe.shape("LOL", "CCC", "LOL");
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('C', Material.COMPASS);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (!is(event.getItemInHand())) return;

        Block block = event.getBlockPlaced();
        NBTBlock nbtBlock = new NBTBlock(block);
        nbtBlock.getData().setString(SupportCrossplay.KEY, BLOCK_TAG);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null || event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (!is(block)) return;

        Player player = event.getPlayer();
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "圧縮機");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (!is(event.getBlock())) return;

        event.setCancelled(true);

        Block block = event.getBlock();
        ItemStack dropItem = getItem();

        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), dropItem);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event)
    {
        Block upperHopper = event.getDestination().getLocation().getBlock();
        Block lowerMachine = upperHopper.getRelative(0, -1, 0);
        Block lowerHopper = upperHopper.getRelative(0, -2, 0);

        if (is(lowerMachine) && upperHopper.getType() == Material.HOPPER) {
            if (!(upperHopper.getState() instanceof Container hopperContainer)) return;

            if (hopperContainer.getInventory().contains(Material.IRON_INGOT, 8)) {
                for (ItemStack item : hopperContainer.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.IRON_INGOT) {
                        if (item.getAmount() >= 9) {
                            item.setAmount(item.getAmount() - 9);
                            break;
                        }
                    }
                }

                if (!(lowerHopper.getState() instanceof Container container)) return;
                Inventory hopperInventory = container.getInventory();
                hopperInventory.addItem(new ItemStack(Material.IRON_BLOCK));
            }
        }
    }
}
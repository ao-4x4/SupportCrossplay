package jp.reitou_mugicha.supportCrossplay.block;

import de.tr7zw.nbtapi.NBTBlock;
import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
        registerRecipe();
    }

    public static ItemStack getItem()
    {
        return Block.clone();
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
        ItemStack handItem = event.getItemInHand();
        NBTItem nbtItem = new NBTItem(handItem);

        if (!nbtItem.hasKey(SupportCrossplay.KEY) || !nbtItem.getString(SupportCrossplay.KEY).equals(BLOCK_TAG)) return;

        Block block = event.getBlockPlaced();
        if (block.getType() == Material.IRON_BLOCK)
        {
            NBTBlock nbtBlock = new NBTBlock(block);
            nbtBlock.getData().setBoolean(BLOCK_TAG, true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null || event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        NBTBlock nbtBlock = new NBTBlock(block);
        if (nbtBlock.getData().getBoolean(BLOCK_TAG))
        {
            Player player = event.getPlayer();
            player.sendMessage(Component.text("圧縮機"));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        NBTBlock nbtBlock = new NBTBlock(block);
        if (!nbtBlock.getData().getBoolean(BLOCK_TAG)) return;

        ItemStack compressor = getItem();

        event.setCancelled(true);
        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), compressor);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event)
    {
        Block hopperBlock = event.getDestination().getLocation().getBlock();
        Block lowerMachine = hopperBlock.getRelative(0, -1, 0);
        Block lowerHopper = hopperBlock.getRelative(0, -2, 0);

        NBTBlock machineNbt = new NBTBlock(lowerMachine);

        if (machineNbt.getData().getBoolean(BLOCK_TAG) && hopperBlock.getType() == Material.HOPPER) {
            if (!(hopperBlock.getState() instanceof Container)) return;
            Container hopperContainer = (Container) hopperBlock.getState();

            if (hopperContainer.getInventory().contains(Material.IRON_INGOT, 8)) {
                for (ItemStack item : hopperContainer.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.IRON_INGOT) {
                        if (item.getAmount() >= 9) {
                            item.setAmount(item.getAmount() - 9);
                            break;
                        }
                    }
                }

                if (!(lowerHopper.getState() instanceof Container)) return;
                Container container = (Container) lowerHopper.getState();
                Inventory hopperInventory = container.getInventory();
                hopperInventory.addItem(new ItemStack(Material.IRON_BLOCK));
            }
        }
    }
}
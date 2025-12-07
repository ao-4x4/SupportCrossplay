package jp.reitou_mugicha.supportCrossplay.block;

import de.tr7zw.nbtapi.NBTBlock;
import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.data.HomeData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BlockHomeBlock implements Listener
{
    private static final ItemStack Block;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "home_block");
    private static final String BLOCK_TAG = "home_block";

    static {
        ItemStack block = new ItemStack(Material.LAPIS_BLOCK);
        ItemMeta meta = block.getItemMeta();
        meta.setDisplayName("拠点ブロック");
        meta.setLore(List.of("拠点を設定し、帰還棒を使ってこの地点に帰ります。"));
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

    public static void registerRecipe()
    {
        if (Bukkit.getRecipe(recipeKey) != null) return;

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, Block);
        recipe.shape("III", "RRR", "III");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) return;

        NBTItem nbt = new NBTItem(item);
        if (nbt.hasKey(SupportCrossplay.KEY) && nbt.getString(SupportCrossplay.KEY).equals(BLOCK_TAG))
        {
            Block block = event.getBlockPlaced();

            NBTBlock nbtBlock = new NBTBlock(block);
            nbtBlock.getData().setBoolean(BLOCK_TAG, true);
            nbtBlock.getData().setString("PlayerUUID", player.getUniqueId().toString());

            HomeData.config.set(player.getUniqueId() + ".position", block.getLocation());
            HomeData.config.set(player.getUniqueId() + ".broken", false);
            HomeData.saveConfig();
            player.sendMessage(Component.text("拠点を設定しました。\nX: " + block.getX() + " Y: " + block.getY() + " Z: " + block.getZ()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block.getType() == Material.AIR) return;
        NBTBlock nbtBlock = new NBTBlock(block);

        if (nbtBlock.getData().getBoolean(BLOCK_TAG))
        {
            ItemStack result = getItem();

            String ownerUUID = nbtBlock.getData().getString("PlayerUUID");
            Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));;
            HomeData.config.set(ownerUUID + ".position", null);
            HomeData.config.set(ownerUUID + ".broken", true);
            HomeData.saveConfig();

            if (nbtBlock.getData().getString("PlayerUUID").equals(player.getUniqueId().toString()))
            {
                event.setCancelled(true);
                block.setType(Material.AIR);

                block.getWorld().dropItemNaturally(block.getLocation(), result);

                player.sendMessage(Component.text("拠点の紐づけを解除しました。"));
            } else {
                if (owner != null)
                {
                    if (owner.isOnline())
                    {
                        if (owner.getInventory().firstEmpty() == -1)
                        {
                            owner.getWorld().dropItemNaturally(owner.getLocation(), result);
                        } else {
                            owner.getInventory().addItem(result);
                        }

                        event.setCancelled(true);
                        block.setType(Material.AIR);

                        owner.sendMessage(Component.text("あなたの拠点ブロックが" + player.getName() + "により破壊されました！\n壊された拠点ブロックはあなたのインベントリに追加されます..."));
                    }
                    else
                    {
                        event.setCancelled(true);
                        block.setType(Material.AIR);
                    }

                    player.sendMessage(owner.getName() + "の拠点を破壊しました！");
                }
            }
            nbtBlock.getData().clearNBT();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (block == null || block.getType() == Material.AIR) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            NBTBlock nbtBlock = new NBTBlock(block);
            if (nbtBlock.getData().getBoolean(BLOCK_TAG))
            {
                if (nbtBlock.getData().hasKey("PlayerUUID"))
                {
                    Player owner = Bukkit.getPlayer(UUID.fromString(nbtBlock.getData().getString("PlayerUUID")));
                    if (owner == null) return;
                    player.sendMessage(owner.getName() + "の拠点");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        if (HomeData.config.getBoolean(playerUUID + ".broken"))
        {
            ItemStack result = getItem();

            if (HomeData.config.getString(playerUUID + ".brokenBy") != null)
            {
                player.sendMessage("あなたがオフライン中に、" + Bukkit.getPlayer(UUID.fromString(Objects.requireNonNull(HomeData.config.getString(playerUUID + ".brokenBy")))) + "によって拠点ブロックが破壊されました！\n拠点ブロックはあなたのインベントリに追加されます...");
            }

            if (player.getInventory().firstEmpty() == -1)
            {
                player.getWorld().dropItemNaturally(player.getLocation(), result);
            } else {
                player.getInventory().addItem(result);
            }
        }
    }
}
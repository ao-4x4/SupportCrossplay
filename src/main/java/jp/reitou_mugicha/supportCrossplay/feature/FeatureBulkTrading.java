package jp.reitou_mugicha.supportCrossplay.feature;

import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FeatureBulkTrading implements Listener
{
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (!((event.getRightClicked()) instanceof Villager villager)) return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.IRON_BLOCK) return;

        event.setCancelled(true);
        openBulkTradingMenu(player, villager);
    }

    public void openBulkTradingMenu(Player player, Villager villager)
    {
        Merchant merchant = Bukkit.createMerchant("一括取引");
        List<MerchantRecipe> recipes = villager.getRecipes();

        List<MerchantRecipe> bulkRecipe = new ArrayList<>(recipes);
        for (MerchantRecipe recipe : bulkRecipe)
        {
            recipe.setPriceMultiplier(1.0F);
        }

        merchant.setRecipes(bulkRecipe);
        player.openMerchant(merchant, true);
    }

    @EventHandler
    public void onPlayerTrade(PlayerPurchaseEvent event)
    {
        Player player = event.getPlayer();
        MerchantRecipe selectedRecipe = event.getTrade();
        if (player.getInventory().getItemInMainHand().getType() == Material.IRON_BLOCK)
        {
            executeBulkTrade(player, selectedRecipe);
            event.setCancelled(true);
        }
    }

    private void executeBulkTrade(Player player, MerchantRecipe recipe)
    {
        ItemStack result = recipe.getResult();
        List<ItemStack> ingredients = recipe.getIngredients();

        int maxTrades = Integer.MAX_VALUE;
        for (ItemStack ingredient : ingredients)
        {
            if (ingredient != null)
            {
                int available = countItem(player, ingredient.getType());
                maxTrades = Math.min(maxTrades, available / ingredient.getAmount());
            }
        }

        if (maxTrades <= 0) return;

        int totalAmount = (int) (result.getAmount() * maxTrades * recipe.getPriceMultiplier());
        for (ItemStack ingredient : ingredients)
        {
            if (ingredient != null)
            {
                removeItems(player, ingredient.getType(), ingredient.getAmount() * maxTrades);
            }
        }

        giveItem(player, result, totalAmount);
    }

    private int countItem(Player player, Material material)
    {
        int count = 0;

        Inventory tradeInventory = player.getOpenInventory().getTopInventory();
        for (ItemStack item : tradeInventory.getContents())
        {
            if (item != null && item.getAmount() > 0 && item.getType() == material)
            {
                count += item.getAmount();
            }
        }

        for (ItemStack item : player.getInventory().getContents())
        {
            if (item != null && item.getType() == material) count += item.getAmount();
        }

        return count;
    }

    private void removeItems(Player player, Material material, int amount)
    {
        for (ItemStack item : player.getInventory().getContents())
        {
            if (item != null && item.getType() == material)
            {
                int stackAmount = item.getAmount();
                if (stackAmount > amount)
                {
                    item.setAmount(stackAmount - amount);
                    return;
                }
                else
                {
                    player.getInventory().remove(item);
                    amount -= stackAmount;
                    if (amount <= 0) return;
                }
            }
        }

        Inventory tradeInventory = player.getOpenInventory().getTopInventory();
        for (ItemStack item : tradeInventory.getContents())
        {
            if (item != null && item.getAmount() > 0 && item.getType() == material)
            {
                int stackAmount = item.getAmount();
                if (stackAmount > amount)
                {
                    item.setAmount(stackAmount - amount);
                    return;
                }
                else
                {
                    item.setAmount(0);
                    amount -= stackAmount;
                    if (amount <= 0) return;
                }
            }
        }
    }

    private void giveItem(Player player, ItemStack item, int amount)
    {
        ItemStack result = item.clone();
        result.setAmount(amount);
        player.getInventory().addItem(result);

        Random rand = new Random();
        for (int i = 0; i < amount; i++)
        {
            player.giveExp(rand.nextInt(4) + 3);
        }
    }
}
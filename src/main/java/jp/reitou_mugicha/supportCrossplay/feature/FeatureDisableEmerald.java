package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import jp.reitou_mugicha.supportCrossplay.data.GeneralConfig;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class FeatureDisableEmerald implements Listener
{
    @EventHandler
    public void onAcquire(VillagerAcquireTradeEvent event)
    {
        if (Helpers.isEconomy())
        {
            MerchantRecipe recipe = event.getRecipe();
            if (recipe.getResult().getType() == Material.EMERALD)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (Helpers.isEconomy())
        {
            if (event.getRightClicked().getType() == EntityType.VILLAGER)
            {
                Villager villager = (Villager) event.getRightClicked();
                List<MerchantRecipe> merchantRecipes = villager.getRecipes();
                for (MerchantRecipe recipe : villager.getRecipes())
                {
                    if (recipe.getResult().getType() == Material.EMERALD)
                    {
                        merchantRecipes.remove(recipe);
                    }
                }
                villager.setRecipes(merchantRecipes);
            }
            else if (event.getRightClicked().getType() == EntityType.WANDERING_TRADER)
            {
                WanderingTrader trader = (WanderingTrader) event.getRightClicked();
                List<MerchantRecipe> merchantRecipes = trader.getRecipes();
                for (MerchantRecipe recipe : trader.getRecipes())
                {
                    if (recipe.getResult().getType() == Material.EMERALD)
                    {
                        merchantRecipes.remove(recipe);
                    }
                }
                trader.setRecipes(merchantRecipes);
            }
        }
    }
}
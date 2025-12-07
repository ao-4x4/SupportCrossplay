package jp.reitou_mugicha.supportCrossplay.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class EntityDamageSnowball extends Snowball
{

    private static final float DAMAGE = 3.0F;

    public EntityDamageSnowball(Level level, LivingEntity entity, ItemStack item)
    {
        super(level, entity, item);
    }

    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity && this.getOwner() != null)
        {
            if (entity instanceof SnowGolem && this.getOwner() instanceof SnowGolem)
            {
                super.onHitEntity(result);
                return;
            }

            DamageSource damageSource = this.damageSources().thrown(this, this.getOwner());
            entity.hurt(damageSource, DAMAGE);
        }

        super.onHitEntity(result);
    }
}
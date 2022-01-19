package com.cinematic.cinematic.mixin;


import com.cinematic.cinematic.events.FallEventCallback;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class FallEventMixin extends LivingEntity {

    protected FallEventMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target="Lnet/minecraft/entity/LivingEntity;handleFallDamage(FF)Z"), method = "handleFallDamage", cancellable = true)
    private void onTick(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Boolean> info) {
        ActionResult result = FallEventCallback.EVENT.invoker().interact((PlayerEntity) (Object) this, fallDistance, damageMultiplier, computeFallDamage(fallDistance, damageMultiplier));

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
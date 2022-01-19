package com.cinematic.cinematic.mixin;

import com.cinematic.cinematic.events.EatEventCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class EatEventMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/ConsumeItemCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/item/ItemStack;)V"), method = "eatFood", cancellable = true)
    private void onEat(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        ActionResult result = EatEventCallback.EVENT.invoker().interact((PlayerEntity) (Object) this, stack);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
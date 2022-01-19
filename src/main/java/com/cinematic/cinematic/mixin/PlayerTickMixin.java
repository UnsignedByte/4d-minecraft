package com.cinematic.cinematic.mixin;


import com.cinematic.cinematic.events.PlayerTickCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerTickMixin {

    @Inject(at = @At(value = "RETURN"), method = "tick", cancellable = true)
    private void onTick(CallbackInfo info) {
        ActionResult result = PlayerTickCallback.EVENT.invoker().interact((PlayerEntity) (Object) this);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
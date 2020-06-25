package net.louislourson.elytraautoflight.mixin;

import net.louislourson.elytraautoflight.ElytraAutoFlight;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.DrownedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    ElytraAutoFlight elytraAutoFlight;

    @Inject(at = @At(value = "RETURN"), method = "addEntity")
    private void addEntity(int int_1, Entity entity_1, CallbackInfo ci) {

        if (elytraAutoFlight == null) elytraAutoFlight = ElytraAutoFlight.instance;

        if (entity_1 instanceof DrownedEntity) {
            //System.out.println(entity_1.toString());

            elytraAutoFlight.addDrowned((DrownedEntity)entity_1);
        }

    }
}

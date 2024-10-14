package net.mobilelize;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.logging.Logger;

public class Casting {

    public static PlayerEntity getLookedAtPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Ensure the player is in the world and the client is active
        if (client.player != null && client.world != null) {
            // Get the current hit result from the crosshair (ray trace result)
            HitResult hitResult = client.crosshairTarget;

            // Check if the hit result is of type ENTITY
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();

                // If the entity is a player, return the PlayerEntity
                if (entity instanceof PlayerEntity) {
                    return (PlayerEntity) entity; // Return the player the user is looking at
                }
            }
        }

        return null; // No player was found in the crosshair
    }
}

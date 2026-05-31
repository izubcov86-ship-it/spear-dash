package com.knight.speardash;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class SpearDashMod implements ClientModInitializer {
    private static KeyBinding dashKey;
    private static boolean wasPressed = false;
    private static long lastDashTick = 0;
    private static final long DASH_COOLDOWN_TICKS = 3;

    @Override
    public void onInitializeClient() {
        dashKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.speardash.dash",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Q,
                "category.speardash"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean isPressed = dashKey.isPressed();

            if (isPressed) {
                long currentTick = client.player.age;
                if (!wasPressed || (currentTick - lastDashTick >= DASH_COOLDOWN_TICKS)) {
                    performDash(client.player);
                    lastDashTick = currentTick;
                }
            }
            wasPressed = isPressed;
        });
    }

    private void performDash(PlayerEntity player) {
        int slot = findSpearSlot(player);
        if (slot == -1) return;

        ItemStack currentMainHand = player.getMainHandStack().copy();
        ItemStack spearStack = player.getInventory().getStack(slot);

        player.getInventory().setStack(slot, currentMainHand);
        player.getInventory().main.set(player.getInventory().selectedSlot, spearStack);

        Vec3d lookVec = player.getRotationVec(1.0F);
        double multiplier = 1.6;

        player.setVelocity(lookVec.multiply(multiplier));
        player.velocityModified = true;
        player.swingHand(Hand.MAIN_HAND);

        ItemStack newMainHand = player.getMainHandStack().copy();
        player.getInventory().setStack(slot, newMainHand);
        player.getInventory().main.set(player.getInventory().selectedSlot, currentMainHand);
    }

    private int findSpearSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().main.get(i).getItem() == Items.TRIDENT) {
                return i;
            }
        }
        for (int i = 9; i < player.getInventory().main.size(); i++) {
            if (player.getInventory().main.get(i).getItem() == Items.TRIDENT) {
                return i;
            }
        }
        return -1;
    }
}
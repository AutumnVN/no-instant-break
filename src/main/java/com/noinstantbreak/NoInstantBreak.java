package com.noinstantbreak;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NoInstantBreak.MODID)
public class NoInstantBreak {
    public static final String MODID = "noinstantbreak";

    private final KeyMapping toggleKey = new KeyMapping("key.noinstantbreak.toggle", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.categories.noinstantbreak");

    private boolean enabled = true;
    private long ticks;
    private long cooldownUntil = -1;

    public NoInstantBreak(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(toggleKey));
    }

    @SubscribeEvent
    public void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!enabled || !event.isAttack() || minecraft.level == null || minecraft.player == null || !(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        if (ticks < cooldownUntil) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        if (!state.isAir() && state.getDestroyProgress(minecraft.player, minecraft.level, hit.getBlockPos()) >= 1.0F) {
            cooldownUntil = ticks + 6;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            cooldownUntil = -1;
            ticks = 0;
            return;
        }
        if (toggleKey.consumeClick()) {
            enabled = !enabled;
            if (!enabled) {
                cooldownUntil = -1;
            }
            minecraft.player.displayClientMessage(Component.translatable(enabled ? "noinstantbreak.on" : "noinstantbreak.off").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        }
        ticks++;
    }
}

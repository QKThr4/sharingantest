package com.sharinganmod.logic;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.net.Net;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SharinganMod.MODID)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer sp) SharinganLogic.tick(sp);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post e) {
        SharinganLogic.charmTick(e.getServer());
    }

    /** Kamui: intangibilidade bloqueia todo o dano. */
    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && SharinganData.get(sp).intangibleTicks > 0) {
            e.setCanceled(true);
        }
    }

    /** Despertar do Sharingan: sobreviver a um golpe quase fatal (30% de chance). */
    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            SharinganData d = SharinganData.get(sp);
            if (d.stage == 0 && sp.getHealth() > 0 && sp.getHealth() <= 4f && sp.getRandom().nextFloat() < 0.3f) {
                SharinganLogic.awaken(sp, d);
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;
        SharinganData d = SharinganData.get(sp);
        LivingEntity victim = e.getEntity();
        if (d.stage == 3) {
            int type = SharinganLogic.mangekyouTypeFor(victim);
            if (type > 0) {
                SharinganLogic.awakenMangekyou(sp, d, type);
                return;
            }
        }
        if (d.eyesActive() && victim instanceof Enemy) SharinganLogic.addXp(sp, d, 10);
    }

    @SubscribeEvent
    public static void xp(PlayerXpEvent.PickupXp e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            SharinganData d = SharinganData.get(sp);
            if (d.eyesActive()) SharinganLogic.addXp(sp, d, e.getOrb().getValue());
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Net.sync(sp);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) SharinganLogic.onRespawn(sp);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Net.sync(sp);
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        SharinganCommands.register(e.getDispatcher());
    }
}

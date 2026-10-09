package com.sharinganmod.logic;

import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import com.sharinganmod.net.Net;
import com.sharinganmod.registry.ModRegistries;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Toda a logica de estado do Sharingan (servidor). */
public final class SharinganLogic {
    private SharinganLogic() {}

    /** Mobs dominados pelo Kotoamatsukami: UUID -> ticks restantes. */
    public static final Map<UUID, Integer> CHARM = new HashMap<>();

    // ---------- utilitarios ----------
    public static void msg(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    public static void chat(ServerPlayer p, String text) {
        p.sendSystemMessage(Component.literal(text));
    }

    public static boolean isPet(Entity e, Entity owner) {
        return e instanceof TamableAnimal t && owner instanceof LivingEntity o && t.isOwnedBy(o);
    }

    // ---------- progressao ----------
    public static void awaken(ServerPlayer p, SharinganData d) {
        d.stage = 1;
        d.xp = 0;
        d.equipped = true;
        chat(p, "§c§lSeu Sharingan despertou! §r§7(1 Tomoe) V = ativar | J = Aba Sharingan");
        p.level().playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 1f, 0.7f);
        Net.sync(p);
    }

    public static void addXp(ServerPlayer p, SharinganData d, int amount) {
        if (d.stage < 1 || d.stage >= 3) return;
        d.xp += amount;
        while (d.stage < 3 && d.xp >= d.xpNeeded()) {
            d.xp -= d.xpNeeded();
            d.stage++;
            chat(p, "§c§lSeu Sharingan evoluiu para " + d.stage + " Tomoe!");
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 0.6f);
            if (d.sharinganOn) refreshEffects(p, d);
        }
        if (d.stage >= 3) d.xp = 0;
    }

    /** Retorna o tipo de Mangekyou (1-4) para o mob domesticado morto, ou 0. */
    public static int mangekyouTypeFor(LivingEntity victim) {
        if (victim instanceof TamableAnimal t && t.isTame()) {
            if (t instanceof net.minecraft.world.entity.animal.Wolf) return 1;
            if (t instanceof net.minecraft.world.entity.animal.Cat) return 4;
            if (t instanceof net.minecraft.world.entity.animal.Parrot) return 2;
            return 1;
        }
        if (victim instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h && h.isTamed()) return 3;
        return 0;
    }

    public static void awakenMangekyou(ServerPlayer p, SharinganData d, int type) {
        d.stage = 4;
        d.type = type;
        d.halfEye = false;
        d.gaveEye = false;
        d.strain = 0;
        d.mangekyouOn = false;
        chat(p, "§4§lA dor de perder quem voce ama despertou o Mangekyou Sharingan de "
                + Skills.TYPE_NAMES[type] + "!");
        chat(p, "§7B = ativar Mangekyou | \\ Z X C = habilidades | cuidado: o uso prolongado causa cegueira.");
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        p.level().playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8f, 1.4f);
        Net.sync(p);
    }

    /** Implanta um olho. Retorna true se consumido. */
    public static boolean transplant(ServerPlayer p, int kind) {
        SharinganData d = SharinganData.get(p);
        if (d.stage < 3) {
            msg(p, "Seus olhos ainda nao suportam este poder (precisa de 3 Tomoe).");
            return false;
        }
        if (d.stage == 5) {
            msg(p, "Voce ja possui o Mangekyou Eterno.");
            return false;
        }
        if (d.stage == 3) {
            d.stage = 4;
            d.type = kind;
            d.halfEye = kind == 3;
            d.gaveEye = kind == 3;
            d.strain = 0;
            d.mangekyouOn = false;
            chat(p, "§4Voce implantou o olho: Mangekyou Sharingan de " + Skills.TYPE_NAMES[kind] + ".");
            Net.sync(p);
            return true;
        }
        // stage 4: so Itachi + Sasuke formam o eterno
        if ((d.type == 1 && kind == 2) || (d.type == 2 && kind == 1)) {
            d.stage = 5;
            d.type = 5;
            d.halfEye = false;
            d.blind = false;
            d.strain = 0;
            d.mangekyouOn = false;
            if (d.susanooOn) endSusanoo(p, d);
            chat(p, "§4§lOs olhos de Itachi e Sasuke se uniram: MANGEKYOU SHARINGAN ETERNO! Sua visao foi restaurada.");
            p.level().playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8f, 1.2f);
            Net.sync(p);
            return true;
        }
        msg(p, "Este olho nao e compativel com o seu Mangekyou (somente Itachi + Sasuke se unem).");
        return false;
    }

    // ---------- acoes vindas do cliente ----------
    public static void handleAction(ServerPlayer p, int id) {
        SharinganData d = SharinganData.get(p);
        switch (id) {
            case 0 -> toggleSharingan(p, d);
            case 1 -> toggleMangekyou(p, d);
            case 2, 3, 4, 5 -> Abilities.use(p, d, id - 2);
            case 7 -> shareObito(p, d);
            case 8 -> toggleEquip(p, d);
            case 9 -> extractEye(p, d);
            default -> { }
        }
        Net.sync(p);
    }

    private static void toggleSharingan(ServerPlayer p, SharinganData d) {
        if (d.stage < 1) { msg(p, "Voce ainda nao despertou o Sharingan."); return; }
        if (!d.equipped) { msg(p, "O Sharingan nao esta equipado (abra a aba com J)."); return; }
        if (d.sharinganOn) {
            deactivateAll(p, d);
            msg(p, "Sharingan desativado.");
        } else {
            d.sharinganOn = true;
            refreshEffects(p, d);
            msg(p, "Sharingan ativado.");
            p.level().playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 0.6f, 1.5f);
        }
    }

    private static void toggleMangekyou(ServerPlayer p, SharinganData d) {
        if (d.stage < 4) { msg(p, "Voce precisa do Mangekyou Sharingan."); return; }
        if (!d.equipped) { msg(p, "O Sharingan nao esta equipado (abra a aba com J)."); return; }
        if (d.blind) { msg(p, "Seus olhos estao destruidos... so o Sharingan comum responde."); return; }
        if (d.mangekyouOn) {
            d.mangekyouOn = false;
            if (d.susanooOn) endSusanoo(p, d);
            refreshEffects(p, d);
            msg(p, "Mangekyou desativado.");
        } else {
            d.sharinganOn = true;
            d.mangekyouOn = true;
            refreshEffects(p, d);
            msg(p, "Mangekyou Sharingan ativado.");
            p.level().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 0.5f);
        }
    }

    private static void toggleEquip(ServerPlayer p, SharinganData d) {
        if (d.stage < 1) { msg(p, "Voce nao tem Sharingan para equipar."); return; }
        d.equipped = !d.equipped;
        if (!d.equipped) deactivateAll(p, d);
        msg(p, d.equipped ? "Sharingan equipado." : "Sharingan removido.");
    }

    private static void shareObito(ServerPlayer p, SharinganData d) {
        if (d.stage == 4 && d.type == 3 && !d.halfEye && !d.gaveEye) {
            give(p, new ItemStack(ModRegistries.EYE_OBITO.get()));
            d.halfEye = true;
            d.gaveEye = true;
            chat(p, "§5Voce partiu seu olho ao meio. Entregue o Meio Olho a quem voce confia.");
        } else {
            msg(p, "Somente o Mangekyou de Obito inteiro pode ser partilhado.");
        }
    }

    private static void extractEye(ServerPlayer p, SharinganData d) {
        if (d.stage == 4 && (d.type == 1 || d.type == 2 || d.type == 4)) {
            give(p, new ItemStack(ModRegistries.eyeFor(d.type)));
            if (d.susanooOn) endSusanoo(p, d);
            d.stage = 3;
            d.type = 0;
            d.mangekyouOn = false;
            d.halfEye = false;
            refreshEffects(p, d);
            chat(p, "§4Voce extraiu seu olho de Mangekyou. Voltou ao Sharingan de 3 Tomoe.");
        } else if (d.stage == 4 && d.type == 3) {
            msg(p, "O olho de Obito so pode ser partilhado (metade).");
        } else {
            msg(p, "Nao ha Mangekyou extraivel.");
        }
    }

    private static void give(ServerPlayer p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    // ---------- efeitos ----------
    public static void deactivateAll(ServerPlayer p, SharinganData d) {
        d.sharinganOn = false;
        d.mangekyouOn = false;
        if (d.susanooOn) endSusanoo(p, d);
        removeEffects(p);
    }

    public static void refreshEffects(ServerPlayer p, SharinganData d) {
        removeEffects(p);
        int lvl = d.level();
        if (lvl <= 0) return;
        applyEffects(p, lvl);
    }

    /**
     * 1 tomoe: visao noturna. 2: + velocidade. 3: + forca. Mangekyou: + resistencia e forca maior.
     * Eterno: resistencia maior.
     */
    private static void applyEffects(ServerPlayer p, int lvl) {
        effect(p, MobEffects.NIGHT_VISION, 260, 0);
        if (lvl >= 2) effect(p, MobEffects.MOVEMENT_SPEED, 60, 0);
        if (lvl >= 3) effect(p, MobEffects.DAMAGE_BOOST, 60, lvl >= 4 ? 1 : 0);
        if (lvl >= 4) effect(p, MobEffects.DAMAGE_RESISTANCE, 60, lvl >= 5 ? 1 : 0);
    }

    static void effect(ServerPlayer p, Holder<MobEffect> eff, int ticks, int amp) {
        p.addEffect(new MobEffectInstance(eff, ticks, amp, true, false, false));
    }

    private static void removeOurs(ServerPlayer p, Holder<MobEffect> eff) {
        MobEffectInstance i = p.getEffect(eff);
        if (i != null && i.isAmbient() && !i.isVisible()) p.removeEffect(eff);
    }

    private static void removeEffects(ServerPlayer p) {
        removeOurs(p, MobEffects.NIGHT_VISION);
        removeOurs(p, MobEffects.MOVEMENT_SPEED);
        removeOurs(p, MobEffects.DAMAGE_BOOST);
        removeOurs(p, MobEffects.DAMAGE_RESISTANCE);
        removeOurs(p, MobEffects.ABSORPTION);
    }

    // ---------- desgaste e cegueira ----------
    public static void addStrain(ServerPlayer p, SharinganData d, float amount) {
        if (d.stage >= 5 || d.blind || amount <= 0) return;
        float old = d.strain;
        d.strain += amount * (d.halfEye ? 0.5f : 1f);
        if (old < 60 && d.strain >= 60) msg(p, "§cSeus olhos comecam a arder...");
        if (old < 85 && d.strain >= 85) msg(p, "§4§lVoce esta prestes a perder a visao!");
        if (d.strain >= 100) {
            d.strain = 100;
            d.blind = true;
            d.mangekyouOn = false;
            if (d.susanooOn) endSusanoo(p, d);
            refreshEffects(p, d);
            chat(p, "§4§lO uso prolongado do Mangekyou destruiu sua visao. Voce esta cego.");
            chat(p, "§7Com o Sharingan ativo (V) voce ainda percebe silhuetas do mundo.");
            p.level().playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.8f, 0.8f);
        }
    }

    // ---------- Susanoo ----------
    public static void startSusanoo(ServerPlayer p, SharinganData d) {
        boolean perfect = d.stage >= 5;
        d.susanooOn = true;
        d.susanooTicks = perfect ? 1800 : 900;
        effect(p, MobEffects.ABSORPTION, d.susanooTicks, perfect ? 9 : 4);
        p.level().playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.9f, 0.6f);
        msg(p, perfect ? "Susanoo Perfeito!" : "Susanoo!");
    }

    public static void endSusanoo(ServerPlayer p, SharinganData d) {
        d.susanooOn = false;
        d.susanooTicks = 0;
        d.cooldowns[3] = Math.max(d.cooldowns[3], 300);
        removeOurs(p, MobEffects.ABSORPTION);
        msg(p, "Susanoo dissipado.");
    }

    private static void susanooTick(ServerPlayer p, SharinganData d) {
        boolean perfect = d.stage >= 5;
        d.susanooTicks--;
        addStrain(p, d, 0.15f);
        if (!d.susanooOn) return;
        if (p.tickCount % 20 == 0) {
            effect(p, MobEffects.DAMAGE_RESISTANCE, 60, 3);
            effect(p, MobEffects.DAMAGE_BOOST, 60, perfect ? 2 : 1);
        }
        if (p.tickCount % 10 == 0) {
            double rad = perfect ? 6 : 4;
            float dmg = perfect ? 10f : 6f;
            for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(rad),
                    x -> x != p && x.isAlive() && !isPet(x, p)
                            && (x instanceof Enemy || (x instanceof Mob m && m.getTarget() == p)))) {
                e.hurt(p.damageSources().playerAttack(p), dmg);
                e.knockback(0.8, p.getX() - e.getX(), p.getZ() - e.getZ());
            }
        }
        if (p.tickCount % 4 == 0) susanooShell(p, perfect);
        if (d.susanooTicks <= 0 || !d.mangekyouOn) endSusanoo(p, d);
    }

    /** Armadura de chakra roxa/azul ao redor do jogador (versao visual simplificada do Susanoo). */
    private static void susanooShell(ServerPlayer p, boolean perfect) {
        ServerLevel l = p.serverLevel();
        double radius = perfect ? 2.6 : 1.8;
        double height = perfect ? 5.0 : 3.4;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(perfect ? 0.35f : 0.45f, 0.4f, 1.0f), 1.6f);
        double[] hs = {0.1, height * 0.3, height * 0.55, height * 0.8};
        for (int i = 0; i < 10; i++) {
            double a = i * (Math.PI * 2 / 10) + p.tickCount * 0.1;
            for (double h : hs) {
                double r = radius * (0.6 + 0.4 * Math.sin(Math.PI * h / height));
                l.sendParticles(dust, p.getX() + Math.cos(a) * r, p.getY() + h, p.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }
    }

    // ---------- tick do jogador ----------
    public static void tick(ServerPlayer p) {
        SharinganData d = SharinganData.get(p);
        if (d.stage <= 0) return;

        for (int i = 0; i < 4; i++) if (d.cooldowns[i] > 0) d.cooldowns[i]--;

        int lvl = d.level();
        if (lvl > 0) {
            slowAura(p, lvl);
            if (p.tickCount % 20 == 0) applyEffects(p, lvl);
            if (p.tickCount % 40 == 0) addXp(p, d, 1);
        }

        if (d.mangekyouOn) {
            addStrain(p, d, 0.02f);
        } else if (!d.blind && d.strain > 0) {
            d.strain = Math.max(0, d.strain - 0.025f);
        }

        if (d.susanooOn) susanooTick(p, d);

        if (d.intangibleTicks > 0) {
            d.intangibleTicks--;
            if (p.tickCount % 2 == 0) {
                p.serverLevel().sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 6, 0.4, 0.8, 0.4, 0.1);
            }
        }

        if (d.suctionTicks > 0) suctionTick(p, d);

        if (p.tickCount % 10 == 0) Net.sync(p);
    }

    /** O Sharingan "diminui os ticks" ao redor: inimigos e projeteis se movem mais devagar. */
    private static void slowAura(ServerPlayer p, int lvl) {
        double f = (20 - SharinganData.SLOW_TICKS[lvl]) / 20.0;
        AABB box = p.getBoundingBox().inflate(10);
        for (Entity e : p.level().getEntities(p, box)) {
            if (e instanceof Projectile pr) {
                if (pr.getOwner() == p) continue;
                e.setDeltaMovement(e.getDeltaMovement().scale(f));
            } else if (e instanceof Mob m) {
                if (isPet(m, p)) continue;
                m.setDeltaMovement(m.getDeltaMovement().multiply(f, 1, f));
            }
        }
    }

    private static void suctionTick(ServerPlayer p, SharinganData d) {
        d.suctionTicks--;
        Vec3 c = p.getEyePosition().add(p.getLookAngle().scale(2.5));
        for (Entity e : p.level().getEntities(p, p.getBoundingBox().inflate(14))) {
            if (!(e instanceof LivingEntity le) || isPet(e, p)) continue;
            Vec3 pull = c.subtract(e.position().add(0, e.getBbHeight() / 2, 0));
            double dist = pull.length();
            if (dist < 0.5) continue;
            e.setDeltaMovement(e.getDeltaMovement().add(pull.normalize().scale(0.35)));
            e.hurtMarked = true;
            if (dist < 3 && p.tickCount % 10 == 0) le.hurt(p.damageSources().indirectMagic(p, p), 3f);
        }
        p.serverLevel().sendParticles(ParticleTypes.PORTAL, c.x, c.y, c.z, 25, 1.2, 1.2, 1.2, 0.3);
    }

    // ---------- Kotoamatsukami (controle de mobs) ----------
    public static void charmTick(MinecraftServer server) {
        if (CHARM.isEmpty() || server.getTickCount() % 10 != 0) return;
        Iterator<Map.Entry<UUID, Integer>> it = CHARM.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> en = it.next();
            en.setValue(en.getValue() - 10);
            Entity ent = null;
            for (ServerLevel l : server.getAllLevels()) {
                ent = l.getEntity(en.getKey());
                if (ent != null) break;
            }
            if (!(ent instanceof Mob m) || !m.isAlive() || en.getValue() <= 0) {
                it.remove();
                continue;
            }
            Monster target = m.level().getEntitiesOfClass(Monster.class, m.getBoundingBox().inflate(16),
                            x -> x != m && x.isAlive() && !CHARM.containsKey(x.getUUID()))
                    .stream().min(Comparator.comparingDouble((Monster x) -> m.distanceToSqr(x))).orElse(null);
            m.setTarget(target);
        }
    }

    public static void onRespawn(ServerPlayer p) {
        SharinganData d = SharinganData.get(p);
        d.susanooOn = false;
        d.susanooTicks = 0;
        d.intangibleTicks = 0;
        d.suctionTicks = 0;
        Net.sync(p);
    }
}

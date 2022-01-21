package com.github.llytho.lootjs_test.tests;

import com.github.llytho.lootjs.core.ILootContextData;
import com.github.llytho.lootjs.loot.action.*;
import com.github.llytho.lootjs_test.AllTests;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ActionTests {
    @Nullable
    private static Consumer<ExplosionEvent> explosionTest = null;

    @Nullable
    private static Consumer<EntityJoinWorldEvent> lightningTest = null;

    public static void loadTests() {
        AllTests.add("AddLootAction", helper -> {
            LootContext ctx = helper.chestContext(helper.player.position(), true);
            ILootContextData data = helper.fillExampleLoot(ctx);

            helper.shouldFail(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.APPLE)),
                    "No apple before adding");
            helper.shouldFail(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.LANTERN)),
                    "No lantern before adding");
            AddLootAction action = new AddLootAction(new ItemStack[]{
                    new ItemStack(Items.APPLE), new ItemStack(Items.LANTERN)
            });
            action.accept(ctx);
            helper.shouldSucceed(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.APPLE)),
                    "Apple exist after adding");
            helper.shouldSucceed(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.LANTERN)),
                    "Lantern exist after adding");
        });

        AllTests.add("RemoveLootAction", helper -> {
            LootContext ctx = helper.chestContext(helper.player.position(), true);
            ILootContextData data = helper.fillExampleLoot(ctx);

            helper.shouldSucceed(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.DIAMOND)),
                    "Diamond exist");
            helper.shouldSucceed(data.getGeneratedLoot().size() == 3, "3 items in loot pool");
            RemoveLootAction action = new RemoveLootAction(i -> i.getItem().equals(Items.DIAMOND));
            action.accept(ctx);
            helper.shouldFail(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.DIAMOND)),
                    "Diamond does not exist anymore");
            helper.shouldSucceed(data.getGeneratedLoot().size() == 2, "2 items in loot pool after removing diamond");
        });

        AllTests.add("ReplaceLootAction", helper -> {
            LootContext ctx = helper.chestContext(helper.player.position(), true);
            ILootContextData data = helper.fillExampleLoot(ctx);

            helper.shouldSucceed(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.DIAMOND)),
                    "Diamond exist");
            helper.shouldFail(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.MAGMA_CREAM)),
                    "Magma cream does not exist currently");
            helper.shouldSucceed(data.getGeneratedLoot().size() == 3, "3 items in loot pool");
            ReplaceLootAction action = new ReplaceLootAction(i -> i.getItem().equals(Items.DIAMOND),
                    new ItemStack(Items.MAGMA_CREAM));
            action.accept(ctx);
            helper.shouldFail(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.DIAMOND)),
                    "Diamond does not exist anymore");
            helper.shouldSucceed(data.getGeneratedLoot().stream().anyMatch(i -> i.getItem().equals(Items.MAGMA_CREAM)),
                    "Magma cream exists now");
            helper.shouldSucceed(data.getGeneratedLoot().size() == 3, "still 3 items in loot pool");
        });

        AllTests.add("LightningStrikeAction", helper -> {
            helper.debugStack.pushLayer();
            LootContext ctx = helper.chestContext(helper.player.position(), true);

            helper.debugStack.h2("shouldDamageEntity: false");
            LightningStrikeAction action = new LightningStrikeAction(false);
            lightningTest = event -> {
                LightningBoltEntity entity = (LightningBoltEntity) event.getEntity();
                helper.shouldSucceed(entity.visualOnly, "Is only visual");
                helper.shouldSucceed(entity.position().equals(helper.player.position()), "Correct position");
            };
            action.accept(ctx);

            helper.debugStack.h2("shouldDamageEntity: true");
            action = new LightningStrikeAction(true);
            lightningTest = event -> {
                LightningBoltEntity entity = (LightningBoltEntity) event.getEntity();
                helper.shouldFail(entity.visualOnly, "Will damage entity");
                helper.shouldSucceed(entity.position().equals(helper.player.position()), "Correct position");
            };
            action.accept(ctx);
            helper.debugStack.popLayer();
        });

        AllTests.add("ExplodeAction", helper -> {
            helper.debugStack.pushLayer();
            Vector3d airPosition = new Vector3d(helper.player.position().x, 256, helper.player.position().z);
            LootContext ctx = helper.chestContext(airPosition, true);

            helper.debugStack.h2("Explosion with radius 5, mode NONE, fire false");
            ExplodeAction action = new ExplodeAction(5, Explosion.Mode.NONE, false);
            explosionTest = event -> {
                Explosion explosion = event.getExplosion();
                helper.shouldSucceed(explosion.getPosition().equals(airPosition), "Correct position");
                helper.shouldSucceed(!explosion.fire, "Fire is off");
                helper.shouldSucceed(explosion.blockInteraction == Explosion.Mode.NONE, "Mode is NONE");
                helper.shouldSucceed(explosion.radius == 5f, "Radius is 5");
            };
            action.accept(ctx);

            helper.debugStack.h2("Explosion with radius 3, mode DESTROY, fire true");
            action = new ExplodeAction(3, Explosion.Mode.DESTROY, true);
            explosionTest = event -> {
                Explosion explosion = event.getExplosion();
                helper.shouldSucceed(explosion.getPosition().equals(airPosition), "Correct position");
                helper.shouldSucceed(explosion.fire, "Fire is on");
                helper.shouldSucceed(explosion.blockInteraction == Explosion.Mode.DESTROY, "Mode is DESTROY");
                helper.shouldSucceed(explosion.radius == 3f, "Radius is 3");
            };
            action.accept(ctx);
            helper.debugStack.popLayer();
        });
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent event) {
        if (explosionTest != null) {
            explosionTest.accept(event);
            explosionTest = null;
        }
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityJoinWorldEvent event) {
        if (lightningTest != null && event.getEntity().getType() == EntityType.LIGHTNING_BOLT) {
            lightningTest.accept(event);
            lightningTest = null;
        }
    }
}
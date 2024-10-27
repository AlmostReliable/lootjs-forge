package com.almostreliable.lootjs.loot;

import com.almostreliable.lootjs.LootJS;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.condition.*;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public interface LootConditionsContainer<C> {

    default C matchTool(ItemPredicate predicate) {
        return addCondition(new MatchTool(Optional.of(predicate)));
    }

    default C matchMainHand(ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(EquipmentSlot.MAINHAND, filter));
    }

    default C matchOffHand(ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(EquipmentSlot.OFFHAND, filter));
    }

    default C matchHead(ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(EquipmentSlot.HEAD, filter));
    }

    default C matchChest(ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(EquipmentSlot.CHEST, filter));
    }

    default C matchLegs(ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(EquipmentSlot.LEGS, filter));
    }

    default C matchFeet(ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(EquipmentSlot.FEET, filter));
    }

    default C matchEquip(EquipmentSlot slot, ItemFilter filter) {
        return addCondition(new MatchEquipmentSlot(slot, filter));
    }

    default C matchAnyInventorySlot(ItemFilter filter) {
        return addCondition(new MatchAnyInventorySlot(filter, false));
    }

    default C matchAnyHotbarSlot(ItemFilter filter) {
        return addCondition(new MatchAnyInventorySlot(filter, true));
    }

    default C survivesExplosion() {
        return addCondition(ExplosionCondition.survivesExplosion().build());
    }

    default C matchTime(long period, int min, int max) {
        return addCondition(new TimeCheck.Builder(IntRange.range(min, max)).setPeriod(period).build());
    }

    default C matchTime(int min, int max) {
        return matchTime(24000L, min, max);
    }

    default C matchWeather(@Nullable Boolean raining, @Nullable Boolean thundering) {
        WeatherCheck.Builder builder = new WeatherCheck.Builder();
        if (raining != null) builder.setRaining(raining);
        if (thundering != null) builder.setThundering(thundering);
        return addCondition(builder.build());
    }

    default C randomChance(NumberProvider value) {
        return addCondition(LootItemRandomChanceCondition.randomChance(value).build());
    }

    default C randomChanceWithEnchantment(Holder<Enchantment> enchantment, float[] chances) {
        Preconditions.checkArgument(chances.length > 0, "There must be at least one chance");
        float first = chances[0];
        List<Float> enchantmentChances = new ArrayList<>();
        for (int i = 1; i < chances.length; i++) {
            enchantmentChances.add(chances[i]);
        }
        LevelBasedValue.Lookup lookup = new LevelBasedValue.Lookup(enchantmentChances, new LevelBasedValue.Constant(0));
        return addCondition(new LootItemRandomChanceWithEnchantedBonusCondition(first, lookup, enchantment));
    }

    default C randomTableBonus(Holder<Enchantment> enchantment, float[] chances) {
        return addCondition(BonusLevelTableCondition.bonusLevelFlatChance(enchantment, chances).build());
    }

    default C matchLocation(LocationPredicate predicate) {
        return matchLocation(BlockPos.ZERO, predicate);
    }

    default C matchLocation(BlockPos offset, LocationPredicate predicate) {
        return addCondition(new LocationCheck(Optional.of(predicate), offset));
    }

    default C matchBiome(HolderSet<Biome> biomes) {
        return addCondition(new MatchBiome(biomes));
    }

    default C matchDimension(ResourceLocation... dimensions) {
        return addCondition(new MatchDimension(dimensions));
    }

    default C matchStructure(HolderSet<Structure> structures) {
        return addCondition(new MatchStructure(structures, true));
    }

    default C matchStructure(HolderSet<Structure> structures, boolean exact) {
        return addCondition(new MatchStructure(structures, exact));
    }

    default C isLightLevel(int min, int max) {
        return addCondition(new IsLightLevel(min, max));
    }

    default C luck(MinMaxBounds.Doubles bounds) {
        return matchPlayerCustom(serverPlayer -> bounds.matches(serverPlayer.getLuck()));
    }

    default C killedByPlayer() {
        return addCondition(LootItemKilledByPlayerCondition.killedByPlayer().build());
    }

    @SuppressWarnings("deprecation")
    default C matchBlock(Block block) {
        return addCondition(new LootItemBlockStatePropertyCondition(block.builtInRegistryHolder(), Optional.empty()));
    }

    @SuppressWarnings("deprecation")
    default C matchBlock(Block block, StatePropertiesPredicate properties) {
        return addCondition(new LootItemBlockStatePropertyCondition(block.builtInRegistryHolder(),
                Optional.of(properties)));
    }

    default C matchEntity(EntityPredicate entityPredicate) {
        return addCondition(LootItemEntityPropertyCondition
                .hasProperties(LootContext.EntityTarget.THIS, entityPredicate)
                .build());
    }

    default C matchAttacker(EntityPredicate entityPredicate) {
        return addCondition(LootItemEntityPropertyCondition
                .hasProperties(LootContext.EntityTarget.ATTACKER, entityPredicate)
                .build());
    }

    default C matchDirectAttacker(EntityPredicate entityPredicate) {
        return addCondition(LootItemEntityPropertyCondition
                .hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, entityPredicate)
                .build());
    }

    default C matchPlayer(EntityPredicate entityPredicate) {
        return addCondition(new MatchPlayer(entityPredicate));
    }

    default C matchDamageSource(DamageSourcePredicate predicate) {
        return addCondition(new DamageSourceCondition(Optional.of(predicate)));
    }

    default C matchDistance(DistancePredicate distancePredicate) {
        return addCondition(new MatchKillerDistance(distancePredicate));
    }

    default C matchPlayerCustom(Predicate<ServerPlayer> predicate) {
        return addCondition(new PlayerParamPredicate(predicate));
    }

    default C matchEntityCustom(Predicate<Entity> predicate) {
        return addCondition(new CustomParamPredicate<>(LootContextParams.THIS_ENTITY, predicate));
    }

    default C matchAttackerCustom(Predicate<Entity> predicate) {
        return addCondition(new CustomParamPredicate<>(LootContextParams.ATTACKING_ENTITY, predicate));
    }

    default C matchDirectAttackerCustom(Predicate<Entity> predicate) {
        return addCondition(new CustomParamPredicate<>(LootContextParams.DIRECT_ATTACKING_ENTITY, predicate));
    }

    default C blockEntity(Predicate<BlockEntity> predicate) {
        return addCondition(new CustomParamPredicate<>(LootContextParams.BLOCK_ENTITY, predicate));
    }

    default C hasAnyStage(String... stages) {
        if (stages.length == 1) {
            String stage = stages[0];
            var condition = new PlayerParamPredicate((player) -> player.getTags().contains(stage));
            return addCondition(condition);
        }

        var condition = new PlayerParamPredicate((player) -> {
            for (String stage : stages) {
                if (player.getTags().contains(stage)) {
                    return true;
                }
            }
            return false;
        });

        return addCondition(condition);
    }

    default C matchAnyOf(LootItemCondition... conditions) {
        return addCondition(new AnyOfCondition(Arrays.asList(conditions)));
    }

    default C matchAllOf(LootItemCondition... conditions) {
        return addCondition(new AllOfCondition(Arrays.asList(conditions)));
    }

    default C matchCustomCondition(JsonObject json) {
        var regOps = RegistryOps.create(JsonOps.INSTANCE, LootJS.lookup());
        var condition = LootItemCondition.DIRECT_CODEC.parse(regOps, json).getOrThrow();
        return addCondition(condition);
    }

    @HideFromJS
    C addCondition(LootItemCondition condition);
}

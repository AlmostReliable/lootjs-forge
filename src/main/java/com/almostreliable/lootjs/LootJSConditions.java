package com.almostreliable.lootjs;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.condition.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Just exist so we have the types registered. But they should not be used for json stuff anyway. So we just return units.
 */
public class LootJSConditions {
    static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE,
            BuildConfig.MOD_ID);

    private static LootItemConditionType create(LootItemCondition unit) {
        return new LootItemConditionType(MapCodec.unit(unit));
    }

    public static Holder<LootItemConditionType> MATCH_EQUIP = CONDITIONS.register("match_equip",
            () -> create(new MatchEquipmentSlot()));
    public static Holder<LootItemConditionType> MATCH_ANY_INVENTORY_SLOT = CONDITIONS.register(
            "match_any_inventory_slot",
            () -> create(new MatchAnyInventorySlot(ItemFilter.NONE, false)));
    public static Holder<LootItemConditionType> DISTANCE = CONDITIONS.register("match_distance",
            () -> create(new MatchKillerDistance(DistancePredicate.vertical(MinMaxBounds.Doubles.ANY))));
    public static Holder<LootItemConditionType> ANY_STRUCTURE = CONDITIONS.register("match_structure",
            () -> create(new MatchStructure(HolderSet.direct(), true)));
    public static Holder<LootItemConditionType> BIOME = CONDITIONS.register("match_biome",
            () -> create(new MatchBiome(HolderSet.direct())));
    public static Holder<LootItemConditionType> LIGHT_LEVEL = CONDITIONS.register("light_level",
            () -> create(new IsLightLevel(-1, -1)));
    public static Holder<LootItemConditionType> ANY_DIMENSION = CONDITIONS.register("match_dimension",
            () -> create(new MatchDimension(new ResourceLocation[]{})));
    public static Holder<LootItemConditionType> PARAM = CONDITIONS.register("param",
            () -> create(new CustomParamPredicate<>(LootContextParams.THIS_ENTITY, entity -> false)));
    public static Holder<LootItemConditionType> PLAYER_PARAM = CONDITIONS.register("player_param",
            () -> create(new PlayerParamPredicate(p -> false)));
    public static Holder<LootItemConditionType> MATCH_PLAYER = CONDITIONS.register("match_player",
            () -> create(new MatchPlayer(EntityPredicate.Builder.entity().build())));
}

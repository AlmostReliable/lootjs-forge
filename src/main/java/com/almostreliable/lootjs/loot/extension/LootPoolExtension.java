package com.almostreliable.lootjs.loot.extension;

import com.almostreliable.lootjs.loot.LootConditionList;
import com.almostreliable.lootjs.loot.LootEntryList;
import com.almostreliable.lootjs.loot.LootFunctionList;
import com.almostreliable.lootjs.util.DebugInfo;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public interface LootPoolExtension {

    static LootPoolExtension cast(LootPool pool) {
        return (LootPoolExtension) pool;
    }

    LootPool lootjs$asVanillaPool();

    LootEntryList lootjs$getEntries();

    LootConditionList lootjs$getConditions();

    LootFunctionList lootjs$getFunctions();

    void lootjs$setName(String name);

    default void lootjs$collectDebugInfo(DebugInfo info) {
        var rollStr = NumberProviders.CODEC
                .encodeStart(JsonOps.INSTANCE, lootjs$asVanillaPool().getRolls())
                .getOrThrow();
        var bonusStr = NumberProviders.CODEC
                .encodeStart(JsonOps.INSTANCE, lootjs$asVanillaPool().getBonusRolls())
                .getOrThrow();
        info.add("% Rolls -> " + rollStr.toString());
        info.add("% Bonus rolls -> " + bonusStr.toString());
        lootjs$getEntries().collectDebugInfo(info);
        lootjs$getConditions().collectDebugInfo(info);
        lootjs$getFunctions().collectDebugInfo(info);
    }

    @HideFromJS
    void lootjs$recompose();
}

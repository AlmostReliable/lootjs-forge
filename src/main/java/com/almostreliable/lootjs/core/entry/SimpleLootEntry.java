package com.almostreliable.lootjs.core.entry;

import com.almostreliable.lootjs.loot.LootConditionList;
import com.almostreliable.lootjs.loot.LootFunctionList;
import com.almostreliable.lootjs.loot.LootFunctionsContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.function.Consumer;

public interface SimpleLootEntry extends LootEntry, LootFunctionsContainer<SimpleLootEntry> {

    LootFunctionList getFunctions();

    @Override
    default SimpleLootEntry when(Consumer<LootConditionList> callback) {
        callback.accept(getConditions());
        return this;
    }

    default SimpleLootEntry apply(Consumer<LootFunctionList> callback) {
        callback.accept(getFunctions());
        return this;
    }

    @Override
    default SimpleLootEntry addFunction(LootItemFunction lootItemFunction) {
        getFunctions().add(lootItemFunction);
        return this;
    }

    default SimpleLootEntry setCount(NumberProvider numberProvider) {
        LootItemFunction sc = SetItemCountFunction.setCount(numberProvider).build();
        if (!getFunctions().replace(LootItemFunctions.SET_COUNT, sc)) {
            getFunctions().add(sc);
        }

        return this;
    }

    default SimpleLootEntry withWeight(int weight) {
        setWeight(weight);
        return this;
    }

    void setWeight(int weight);

    int getWeight();

    default SimpleLootEntry withQuality(int quality) {
        setQuality(quality);
        return this;
    }

    void setQuality(int quality);

    int getQuality();
}

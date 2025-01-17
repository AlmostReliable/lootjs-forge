package com.almostreliable.lootjs.mixin;

import com.almostreliable.lootjs.loot.LootConditionList;
import com.almostreliable.lootjs.loot.LootEntryList;
import com.almostreliable.lootjs.loot.LootFunctionList;
import com.almostreliable.lootjs.loot.extension.CompositeEntryBaseExtension;
import com.almostreliable.lootjs.loot.extension.LootPoolExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Mixin(LootPool.class)
public class LootPoolMixin implements LootPoolExtension {
    @Mutable @Shadow @Final private List<LootPoolEntryContainer> entries;
    @Mutable @Shadow @Final private List<LootItemCondition> conditions;
    @Mutable @Shadow @Final private Predicate<LootContext> compositeCondition;
    @Mutable @Shadow @Final private List<LootItemFunction> functions;
    @Mutable @Shadow @Final private BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    @Shadow @Nullable private String name;

    @Override
    public LootPool lootjs$asVanillaPool() {
        return (LootPool) (Object) this;
    }

    @Override
    public LootEntryList lootjs$getEntries() {
        var list = new LootEntryList(this.entries);
        this.entries = list.getElements();
        return list;
    }

    @Override
    public LootConditionList lootjs$getConditions() {
        LootConditionList cl = new LootConditionList(this.conditions);
        this.conditions = cl.getElements();
        this.compositeCondition = cl;
        return cl;
    }

    @Override
    public LootFunctionList lootjs$getFunctions() {
        LootFunctionList fl = new LootFunctionList(this.functions);
        this.functions = fl.getElements();
        this.compositeFunction = fl;
        return fl;
    }

    @Override
    public void lootjs$setName(String name) {
        this.name = name;
    }

    @Override
    public void lootjs$recompose() {
        for (var entry : entries) {
            if (entry instanceof CompositeEntryBaseExtension ext) {
                ext.lootjs$recompose();
            }
        }
    }
}

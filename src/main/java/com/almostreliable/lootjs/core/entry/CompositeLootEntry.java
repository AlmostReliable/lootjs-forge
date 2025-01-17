package com.almostreliable.lootjs.core.entry;

import com.almostreliable.lootjs.loot.LootConditionList;
import com.almostreliable.lootjs.loot.LootEntryList;
import com.almostreliable.lootjs.loot.extension.CompositeEntryBaseExtension;
import com.almostreliable.lootjs.loot.table.LootEntriesTransformer;
import com.almostreliable.lootjs.loot.table.LootEntryAppender;
import com.almostreliable.lootjs.util.DebugInfo;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class CompositeLootEntry implements LootEntry, LootEntriesTransformer, LootEntryAppender {
    private final CompositeEntryBase vanillaEntry;
    @Nullable
    private LootEntryList entries;
    @Nullable
    private LootConditionList conditions;

    public CompositeLootEntry(CompositeEntryBase vanillaEntry) {
        this.vanillaEntry = vanillaEntry;
    }

    public CompositeLootEntry(CompositeEntryBase vanillaEntry, LootEntryList entries, LootConditionList conditions) {
        this.vanillaEntry = vanillaEntry;
        this.entries = entries;
        this.conditions = conditions;
    }

    public LootEntryList getEntries() {
        if (entries == null) {
            entries = new LootEntryList(((CompositeEntryBaseExtension) vanillaEntry).lootjs$getEntries());
        }

        return entries;
    }

    public CompositeLootEntry entries(Consumer<LootEntryList> callback) {
        callback.accept(getEntries());
        return this;
    }

    @Override
    public LootPoolEntryType getVanillaType() {
        return vanillaEntry.getType();
    }

    @Override
    public CompositeEntryBase getVanillaEntry() {
        return vanillaEntry;
    }

    @Override
    public CompositeLootEntry when(Consumer<LootConditionList> callback) {
        callback.accept(getConditions());
        return this;
    }

    @Override
    public LootConditionList getConditions() {
        if (conditions == null) {
            conditions = new LootConditionList(vanillaEntry.conditions);
            vanillaEntry.conditions = conditions.getElements();
            vanillaEntry.compositeCondition = conditions;
        }

        return conditions;
    }

    @Override
    public void collectDebugInfo(DebugInfo info) {
        info.add(getType().toString());
        info.push();
        entries(entries -> entries.collectDebugInfo(info));
        when(conditions -> conditions.collectDebugInfo(info));
        info.pop();
    }

    @Override
    public CompositeLootEntry addEntry(LootEntry entry) {
        entries(entries -> entries.add(entry));
        return this;
    }

    @Override
    public CompositeLootEntry modifyEntry(UnaryOperator<SimpleLootEntry> onTransform, boolean deepTransform) {
        getEntries().modifyEntry(onTransform, deepTransform);
        return this;
    }

    @Override
    public CompositeLootEntry removeEntry(Predicate<SimpleLootEntry> onRemove, boolean deepRemove) {
        getEntries().removeEntry(onRemove, deepRemove);
        return this;
    }

    @Override
    public CompositeLootEntry addCondition(LootItemCondition condition) {
        getConditions().add(condition);
        return this;
    }
}

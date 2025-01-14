package com.almostreliable.lootjs.loot;

import com.almostreliable.lootjs.core.LootType;
import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.LootTableFilter;
import com.almostreliable.lootjs.loot.table.LootTableList;
import com.almostreliable.lootjs.loot.table.MutableLootTable;
import com.mojang.serialization.Lifecycle;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LootTableEvent {
    private static final RegistrationInfo REG_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.stable());
    private final WritableRegistry<LootTable> registry;

    public LootTableEvent(WritableRegistry<LootTable> registry) {
        this.registry = registry;
    }

    protected WritableRegistry<LootTable> registry() {
        return registry;
    }

    public Set<ResourceLocation> getLootTableIds() {
        return Collections.unmodifiableSet(registry().keySet());
    }

    public Set<ResourceLocation> getLootTableIds(IdFilter filter) {
        return registry().keySet().stream().filter(filter).collect(Collectors.toSet());
    }

    public void forEachTable(IdFilter filter, Consumer<MutableLootTable> onForEach) {
        getLootTableIds(filter).forEach(location -> {
            var table = getLootTable(location);
            onForEach.accept(table);
        });
    }

    public void forEachTable(Consumer<MutableLootTable> onForEach) {
        forEachTable(location -> true, onForEach);
    }

    public boolean hasLootTable(ResourceLocation location) {
        return registry().containsKey(location);
    }

    public void clearLootTables(IdFilter filter) {
        for (LootTable lootTable : registry()) {
            if (filter.test(lootTable.getLootTableId())) {
                new MutableLootTable(lootTable).clear();
            }
        }
    }

    public MutableLootTable getLootTable(ResourceLocation location) {
        LootTable lootTable = registry().get(location);
        if (lootTable == null) {
            throw new IllegalArgumentException("Unknown loot table: " + location);
        }

        return new MutableLootTable(lootTable);
    }

    public MutableLootTable getBlockTable(Block block) {
        return getLootTable(block.getLootTable().location());
    }

    public MutableLootTable getEntityTable(EntityType<?> entityType) {
        return getLootTable(entityType.getDefaultLootTable().location());
    }

    public LootTableList modifyLootTables(LootTableFilter... filters) {
        var tables = registry()
                .stream()
                .filter(lootTable -> {
                    for (var f : filters) {
                        if (f.test(lootTable)) {
                            return true;
                        }
                    }

                    return false;
                })
                .map(MutableLootTable::new)
                .toList();
        return new LootTableList(tables);
    }

    public LootTableList modifyBlockTables(IdFilter filter) {
        var tables = BuiltInRegistries.BLOCK
                .holders()
                .filter(ref -> filter.test(ref.key().location()))
                .map(ref -> this.getBlockTable(ref.value()))
                .toList();
        return new LootTableList(tables);
    }

    public LootTableList modifyEntityTables(IdFilter filter) {
        var tables = BuiltInRegistries.ENTITY_TYPE
                .holders()
                .filter(ref -> filter.test(ref.key().location()))
                .map(ref -> this.getEntityTable(ref.value()))
                .toList();
        return new LootTableList(tables);
    }

    @Deprecated(forRemoval = true)
    public LootTableList modifyLootTypeTables(LootType... types) {
        ConsoleJS.SERVER.error(
                "LootJS: `modifyLootTypeTables` is deprecated. Use `modifyLootTables` with LootType instead.");
        Set<LootType> asSet = new HashSet<>(Arrays.asList(types));

        var tables = registry().stream().filter(lootTable -> {
            LootType type = LootType.getLootType(lootTable.getParamSet());
            return asSet.contains(type);
        }).map(MutableLootTable::new).toList();
        return new LootTableList(tables);
    }

    public MutableLootTable create(ResourceLocation location) {
        return create(location, LootType.CHEST);
    }

    public MutableLootTable create(ResourceLocation location, LootType type) {
        if (hasLootTable(location)) {
            throw new RuntimeException("[LootJS Error] Loot table already exists, cannot create new one: " + location);
        }

        LootContextParamSet paramSet = type.getParamSet();
        LootTable lootTable = new LootTable.Builder().setParamSet(paramSet).setRandomSequence(location).build();
        //noinspection ConstantValue
        if (lootTable.getLootTableId() == null) {
            lootTable.setLootTableId(location);
        }

        var key = ResourceKey.create(Registries.LOOT_TABLE, location);
        registry().register(key, lootTable, REG_INFO);
        return new MutableLootTable(lootTable);
    }
}

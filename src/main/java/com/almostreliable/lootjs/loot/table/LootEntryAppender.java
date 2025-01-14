package com.almostreliable.lootjs.loot.table;

import com.almostreliable.lootjs.core.entry.LootEntry;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;

public interface LootEntryAppender {
    default LootEntryAppender addCustomEntry(JsonObject json) {
        var vanilla = LootPoolEntries.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        return addEntry(LootEntry.ofVanilla(vanilla));
    }

    LootEntryAppender addEntry(LootEntry entry);
}

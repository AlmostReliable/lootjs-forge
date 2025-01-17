package com.almostreliable.lootjs.loot.extension;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.List;

public interface CompositeEntryBaseExtension {
    List<LootPoolEntryContainer> lootjs$getEntries();

    @HideFromJS
    void lootjs$recompose();
}

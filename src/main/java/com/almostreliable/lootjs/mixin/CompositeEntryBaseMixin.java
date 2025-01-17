package com.almostreliable.lootjs.mixin;

import com.almostreliable.lootjs.loot.extension.CompositeEntryBaseExtension;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(CompositeEntryBase.class)
public abstract class CompositeEntryBaseMixin implements CompositeEntryBaseExtension {

    @Mutable @Final @Shadow public List<LootPoolEntryContainer> children;

    @Mutable @Shadow @Final private ComposableEntryContainer composedChildren;


    @Shadow
    protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list);

    @Override
    public List<LootPoolEntryContainer> lootjs$getEntries() {
        if (this.children.getClass() != ArrayList.class) { // We only want exactly ArrayList, so we know its mutable
            this.children = new ArrayList<>(this.children);
        }

        return this.children;
    }

    @HideFromJS
    @Override
    public void lootjs$recompose() {
        if (this.children.getClass() != ArrayList.class) {
            return;
        }

        this.composedChildren = this.compose(this.children);
        for (var child : children) {
            if (child instanceof CompositeEntryBaseExtension ext) {
                ext.lootjs$recompose();
            }
        }
    }
}

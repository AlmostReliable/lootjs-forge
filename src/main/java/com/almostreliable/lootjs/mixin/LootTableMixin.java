package com.almostreliable.lootjs.mixin;

import com.almostreliable.lootjs.LootJS;
import com.almostreliable.lootjs.core.LootBucket;
import com.almostreliable.lootjs.loot.LootFunctionList;
import com.almostreliable.lootjs.loot.extension.LootPoolExtension;
import com.almostreliable.lootjs.loot.extension.LootTableExtension;
import com.almostreliable.lootjs.loot.table.LootTracker;
import com.almostreliable.lootjs.loot.table.PostLootAction;
import com.almostreliable.lootjs.loot.table.PostLootActionOwner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableMixin implements PostLootActionOwner, LootTableExtension {

    @Mutable @Shadow @Final private LootContextParamSet paramSet;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Mutable
    @Shadow
    @Final
    private Optional<ResourceLocation> randomSequence;
    @Mutable @Shadow @Final private List<LootItemFunction> functions;
    @Mutable @Shadow @Final private BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    @Mutable @Shadow @Final private List<LootPool> pools;
    @Unique
    @Nullable
    private PostLootAction lootjs$postLootAction;

    @Shadow
    public abstract ResourceLocation getLootTableId();

    @Override
    public void lootjs$setPostLootAction(PostLootAction postLootAction) {
        this.lootjs$postLootAction = postLootAction;
    }

    @Nullable
    @Override
    public PostLootAction lootjs$getPostLootAction() {
        return this.lootjs$postLootAction;
    }

    @ModifyVariable(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), argsOnly = true)
    private Consumer<ItemStack> lootjs$createLootTracker(Consumer<ItemStack> output, LootContext ctx, Consumer<ItemStack> $) {
        if (lootjs$getPostLootAction() != null) {
            return new LootTracker(output, this.getLootTableId());
        }

        return output;
    }

    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void lootjs$resolveLootTracker(LootContext context, Consumer<ItemStack> output, CallbackInfo ci) {
        if (!(output instanceof LootTracker tracker)) {
            return;
        }

        var tableId = this.getLootTableId();
        if (tracker.getTableId() != tableId) {
            LootJS.LOG.warn(
                    "LootJS: Loot table id mismatch when trying to resolve loot tracker. Expected: '{}', actual: '{}'",
                    tableId,
                    tracker.getTableId());
            return;
        }

        PostLootAction action = lootjs$getPostLootAction();
        if (action != null) {
            action.alter(context, new LootBucket(context, tracker.getLoot()));
        }

        tracker.resolve();
    }

    @Override
    public List<LootPool> lootjs$getPools() {
        return this.pools;
    }

    @Override
    public void lootjs$setPools(List<LootPool> pools) {
        this.pools = pools;
    }

    @Override
    public LootFunctionList lootjs$createFunctionList() {
        LootFunctionList fl = new LootFunctionList(this.functions);
        this.functions = fl.getElements();
        this.compositeFunction = fl;
        return fl;
    }

    @Override
    public void lootjs$setRandomSequence(@Nullable ResourceLocation randomSequence) {
        this.randomSequence = Optional.ofNullable(randomSequence);
    }

    @Override
    @Nullable
    public ResourceLocation lootjs$getRandomSequence() {
        return this.randomSequence.orElse(null);
    }

    @Override
    public void lootjs$setParamSet(LootContextParamSet paramSet) {
        this.paramSet = paramSet;
    }

    @Override
    public LootContextParamSet lootjs$getParamSet() {
        return this.paramSet;
    }

    @Override
    public void lootjs$recompose() {
        for (var p : lootjs$getPools()) {
            ((LootPoolExtension) p).lootjs$recompose();
        }
    }
}

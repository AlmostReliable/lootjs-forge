package com.github.llytho.lootjs.loot.action;

import com.github.llytho.lootjs.core.Constants;
import com.github.llytho.lootjs.core.LootAction;
import com.github.llytho.lootjs.core.ILootContextData;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;

public class AddLootAction implements LootAction {

    private final ItemStack[] itemStacks;

    public AddLootAction(ItemStack[] itemStacks) {
        this.itemStacks = itemStacks;
    }

    @Override
    public boolean accept(LootContext context) {
        ILootContextData data = context.getParamOrNull(Constants.DATA);
        if (data != null) {
            for (ItemStack itemStack : itemStacks) {
                data.getGeneratedLoot().add(itemStack.copy());
            }
        }
        return true;
    }
}

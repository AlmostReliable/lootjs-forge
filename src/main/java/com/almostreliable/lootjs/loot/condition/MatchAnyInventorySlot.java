package com.almostreliable.lootjs.loot.condition;

import com.almostreliable.lootjs.LootJSConditions;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.util.LootContextUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record MatchAnyInventorySlot(ItemFilter filter, boolean hotbar) implements LootItemCondition {

    @Override
    public boolean test(LootContext ctx) {
        ServerPlayer player = LootContextUtils.getPlayerOrNull(ctx);
        if (player == null) {
            return false;
        }

        var inv = player.getInventory();
        int max = hotbar ? 9 : 36;
        for (int i = 0; i < max; i++) {
            if (filter.test(inv.getItem(i))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public LootItemConditionType getType() {
        return LootJSConditions.MATCH_ANY_INVENTORY_SLOT.value();
    }
}

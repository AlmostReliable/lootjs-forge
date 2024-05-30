package com.almostreliable.lootjs.kube;

import com.almostreliable.lootjs.LootModificationsAPI;
import com.almostreliable.lootjs.core.LootType;
import com.almostreliable.lootjs.core.filters.ResourceLocationFilter;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.almostreliable.lootjs.util.Utils;
import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class LootModificationEventJS implements KubeEvent {
    private final Map<ResourceLocation, IGlobalLootModifier> modifiers;
    private final List<ResourceLocation> removedGlobalModifiers = new ArrayList<>();
    private final List<LootModifier.Builder> modifierBuilders = new ArrayList<>();

    public LootModificationEventJS(Map<ResourceLocation, IGlobalLootModifier> modifiers) {
        this.modifiers = modifiers;
    }

    public List<String> getGlobalModifiers() {
        return modifiers.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList());
    }

    public void removeGlobalModifier(String... locationOrModIds) {
        Set<String> modIds = new HashSet<>();
        Set<ResourceLocation> locations = new HashSet<>();
        for (String locationOrModId : locationOrModIds) {
            if (locationOrModId.startsWith("@")) {
                modIds.add(locationOrModId.substring(1));
            } else {
                locations.add(new ResourceLocation(locationOrModId));
            }
        }

        Set<ResourceLocation> collectedByModIds = modifiers.keySet()
                .stream()
                .filter(rl -> modIds.contains(rl.getNamespace()))
                .collect(Collectors.toSet());
        Set<ResourceLocation> collectedByLocations = modifiers.keySet()
                .stream()
                .filter(locations::contains)
                .collect(Collectors.toSet());

        remove(collectedByModIds);
        remove(collectedByLocations);
    }

    private void remove(Set<ResourceLocation> locations) {
        locations.forEach(modifiers::remove);
        removedGlobalModifiers.addAll(locations);
    }

    public void enableLogging() {
        LootModificationsAPI.DEBUG_LOOT_MODIFIERS = true;
    }

    public void disableLootModification(ResourceLocationFilter... filters) {
        if (filters.length == 0) {
            throw new IllegalArgumentException("No loot table were given.");
        }

        LootModificationsAPI.FILTERS.addAll(Arrays.asList(filters));
    }

    public LootModifier.Builder addTableModifier(ResourceLocationFilter... filters) {
        if (filters.length == 0) {
            throw new IllegalArgumentException("No loot table were given.");
        }

        LootModifier.Builder builder = new LootModifier.Builder(new LootModifier.TableFiltered(filters),
                Utils.quote("LootTables", Arrays.asList(filters)));
        modifierBuilders.add(builder);
        return builder;
    }

    public LootModifier.Builder addTypeModifier(LootType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("No loot type were given.");
        }

        LootModifier.Builder builder = new LootModifier.Builder(new LootModifier.TypeFiltered(types),
                Utils.quote("Types", Arrays.asList(types)));
        modifierBuilders.add(builder);
        return builder;
    }

    public LootModifier.Builder addBlockModifier(Object o) {
        Preconditions.checkNotNull(o);
        BlockStatePredicate blockStatePredicate = BlockStatePredicate.of(o);
        LootModifier.Builder builder = new LootModifier.Builder(new LootModifier.BlockFiltered(blockStatePredicate),
                "BlocksPredicate for: " + StringUtils.abbreviate(o.toString(), 50));
        modifierBuilders.add(builder);
        return builder;
    }

    public LootModifier.Builder addEntityModifier(EntityType<?>... entities) {
        Set<EntityType<?>> set = Arrays.stream(entities).filter(Objects::nonNull).collect(Collectors.toSet());

        if (set.isEmpty()) {
            throw new IllegalArgumentException("No valid entities given.");
        }

        List<ResourceLocation> entityIds = set
                .stream()
                .map(BuiltInRegistries.ENTITY_TYPE::getKey)
                .collect(Collectors.toList());
        LootModifier.Builder builder = new LootModifier.Builder(new LootModifier.EntityFiltered(set),
                Utils.quote("Entities", entityIds));
        modifierBuilders.add(builder);
        return builder;
    }

    public void disableWitherStarDrop() {
        LootModificationsAPI.DISABLE_WITHER_DROPPING_NETHER_STAR = true;
    }

    public void disableCreeperHeadDrop() {
        LootModificationsAPI.DISABLE_CREEPER_DROPPING_HEAD = true;
    }

    public void disableSkeletonHeadDrop() {
        LootModificationsAPI.DISABLE_SKELETON_DROPPING_HEAD = true;
    }

    public void disableZombieHeadDrop() {
        LootModificationsAPI.DISABLE_ZOMBIE_DROPPING_HEAD = true;
    }

    @Override
    public void afterPosted(EventResult result) {
        if (LootJSPlugin.eventsAreDisabled()) {
            return;
        }

        if (!removedGlobalModifiers.isEmpty()) {
            ConsoleJS.SERVER.info("[LootJS] Removed " + removedGlobalModifiers.size() + " global loot modifiers: " +
                                  removedGlobalModifiers
                                          .stream()
                                          .map(ResourceLocation::toString)
                                          .collect(Collectors.joining(", ")));
        }

        try {
            for (var b : modifierBuilders) {
                LootModificationsAPI.addModification(b.build());
            }
        } catch (Exception exception) {
            ConsoleJS.SERVER.error(exception);
        }
    }
}

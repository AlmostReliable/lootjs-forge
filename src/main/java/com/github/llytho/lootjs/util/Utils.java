package com.github.llytho.lootjs.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class Utils {

    public static <T> String getClassNameEnding(T t) {
        String tName = t.getClass().getName();
        return tName.substring(tName.lastIndexOf('.') + 1);
    }

    public static <T extends IForgeRegistryEntry<T>> TagOrEntry<T> getTagOrEntry(IForgeRegistry<T> registry, String idOrTag) {
        @SuppressWarnings("unchecked")
        ITagCollection<T> tagCollection = (ITagCollection<T>) getTagCollectionByRegistry(registry);
        if (idOrTag.startsWith("#")) {
            ITag<T> tag = tagCollection.getTag(new ResourceLocation(idOrTag.substring(1)));
            if (tag == null) {
                throw new IllegalArgumentException(
                        "Tag " + idOrTag + " does not exists for " + registry.getRegistryName());
            }
            return TagOrEntry.withTag(tag);
        } else {
            T entry = registry.getValue(new ResourceLocation(idOrTag));
            if (entry == null || entry.getRegistryName() == null ||
                entry.getRegistryName().equals(registry.getDefaultKey())) {
                throw new IllegalArgumentException(
                        "Type " + idOrTag + " does not exists for " + registry.getRegistryName());
            }
            return TagOrEntry.withEntry(entry);
        }
    }

    public static ITagCollection<? extends IForgeRegistryEntry<?>> getTagCollectionByRegistry(IForgeRegistry<?> registry) {
        if (registry == ForgeRegistries.BLOCKS) {
            return TagCollectionManager.getInstance().getBlocks();
        }

        if (registry == ForgeRegistries.FLUIDS) {
            return TagCollectionManager.getInstance().getFluids();
        }

        if (registry == ForgeRegistries.ITEMS) {
            return TagCollectionManager.getInstance().getItems();
        }

        if (registry == ForgeRegistries.ENTITIES) {
            return TagCollectionManager.getInstance().getEntityTypes();
        }

        return TagCollectionManager.getInstance().getCustomTypeCollection(registry);
    }

    @Nullable
    public static String formatEntity(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }

        return String.format("Type='%s', Id=%s, Dim='%s', x=%.2f, y=%.2f, z=%.2f",
                entity.getType().getRegistryName(),
                entity.getId(),
                entity.level == null ? "~NO DIM~" : entity.level.dimension().location(),
                entity.getX(),
                entity.getY(),
                entity.getZ());
    }

    @Nullable
    public static String formatPosition(@Nullable Vector3d position) {
        if (position == null) {
            return null;
        }

        return String.format("(%.2f, %.2f, %.2f)", position.x, position.y, position.z);
    }

    @Nullable
    public static String formatItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return null;
        }

        String tag = "";
        if (itemStack.hasTag()) tag += " " + itemStack.getTag();
        return itemStack + tag;
    }
}

package com.github.llytho.lootjs_test.tests;

import com.github.llytho.lootjs.core.Constants;
import com.github.llytho.lootjs.core.ILootContextData;
import com.github.llytho.lootjs.loot.condition.*;
import com.github.llytho.lootjs.loot.condition.builder.DistancePredicateBuilder;
import com.github.llytho.lootjs_test.AllTests;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConditionTests {

    public static void loadTests() {
        AllTests.add("AnyBiomeCheck & BiomeCheck", helper -> {
            BlockPos blockPos = helper.player.blockPosition().offset(1, 0, 1);
            Holder<Biome> biomeHolder = helper.level.getBiome(blockPos);
            LootContext ctx = helper.unknownContext(helper.player.position());

            ResourceKey<Biome> bKey = helper.biome(Objects.requireNonNull(biomeHolder.value().getRegistryName()));
            AnyBiomeCheck anyBiomeCheck = new AnyBiomeCheck(Collections.singletonList(bKey), new ArrayList<>());
            helper.shouldSucceed(anyBiomeCheck.test(ctx), "Biome " + bKey.location() + " should match");

            ResourceKey<Biome> oceanKey = helper.biome(new ResourceLocation("minecraft:deep_ocean"));
            AnyBiomeCheck anyBiomeCheckFail = new AnyBiomeCheck(Collections.singletonList(oceanKey), new ArrayList<>());
            helper.shouldFail(anyBiomeCheckFail.test(ctx), "Biome " + oceanKey.location() + " should not match");

            var types = biomeHolder.tags().toList();
            AnyBiomeCheck withAnyType = new AnyBiomeCheck(new ArrayList<>(), new ArrayList<>(types));
            helper.shouldSucceed(withAnyType.test(ctx),
                    "Biome " + bKey.location() + " should match any type: " + types);

            BiomeCheck withAllTypes = new BiomeCheck(new ArrayList<>(), new ArrayList<>(types));
            helper.shouldSucceed(withAllTypes.test(ctx), "Biome " + bKey.location() + " should match types: " + types);

            var invalidTags = Collections.singletonList(BiomeTags.IS_NETHER);
            AnyBiomeCheck withNoType = new AnyBiomeCheck(new ArrayList<>(), invalidTags);
            helper.shouldFail(withNoType.test(ctx),
                    "Biome " + bKey.location() + " should not match any type: " + invalidTags);

            var invalidVoidTags = new ArrayList<>(types);
            invalidVoidTags.add(BiomeTags.HAS_END_CITY);
            BiomeCheck withNoType2 = new BiomeCheck(new ArrayList<>(), invalidVoidTags);
            helper.shouldFail(withNoType2.test(ctx),
                    "Biome " + bKey.location() + " should not match types: " + invalidVoidTags);
        });

        AllTests.add("AnyDimension", helper -> {
            LootContext ctx = helper.unknownContext(helper.player.position());

            AnyDimension owDim = new AnyDimension(new ResourceLocation[]{
                    new ResourceLocation("overworld")
            });
            helper.shouldSucceed(owDim.test(ctx), "Is in overworld");

            AnyDimension netherDim = new AnyDimension(new ResourceLocation[]{
                    new ResourceLocation("nether")
            });
            helper.shouldFail(netherDim.test(ctx), "Not in nether");
        });

        AllTests.add("AnyStructure", helper -> {
            // ConfiguredStructureTags.EYE_OF_ENDER_LOCATED -> looks like this is for finding strongholds ._.'
            Registry<ConfiguredStructureFeature<?, ?>> registry = helper.level
                    .registryAccess()
                    .registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
            HolderSet.Direct<ConfiguredStructureFeature<?, ?>> holders = registry
                    .getHolder(BuiltinStructures.OCEAN_MONUMENT).map(HolderSet::direct).orElseThrow();

            Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> nearestMapFeature = helper.level
                    .getChunkSource()
                    .getGenerator()
                    .findNearestMapFeature(helper.level, holders,
                            helper.player.blockPosition(),
                            100,
                            false);

            if (nearestMapFeature != null) {
                Map<ConfiguredStructureFeature<?, ?>, LongSet> allStructuresAt = helper.level
                        .structureFeatureManager()
                        .getAllStructuresAt(nearestMapFeature.getFirst());
                List<StructureStart> structureStarts = helper.level
                        .structureFeatureManager()
                        .startsForFeature(SectionPos.of(nearestMapFeature.getFirst()),
                                nearestMapFeature.getSecond().value());
                StructurePiece structurePiece = structureStarts
                        .stream()
                        .map(StructureStart::getPieces)
                        .flatMap(Collection::stream)
                        .findFirst()
                        .orElseThrow();

                LootContext ctx = helper.unknownContext(new Vec3(structurePiece.getBoundingBox().minX(),
                        structurePiece.getBoundingBox().minY(),
                        // TODO is weird, i don't like but map feature provides a different position ...
                        structurePiece.getBoundingBox().minZ()));

                AnyStructure shStructure = new AnyStructure(List.of(BuiltinStructures.OCEAN_MONUMENT),
                        false);
                helper.shouldSucceed(shStructure.test(ctx), "StructureFeature is a stronghold");
            } else {
                helper.shouldFail(true, "Stronghold not found error. This should not happen");
            }

            LootContext ctx = helper.unknownContext(helper.player.position());
            AnyStructure shStructure = new AnyStructure(List.of(BuiltinStructures.BASTION_REMNANT),
                    false);
            helper.shouldFail(shStructure.test(ctx), "StructureFeature is not a nether bastion [exact = false]");

            shStructure = new AnyStructure(List.of(BuiltinStructures.BASTION_REMNANT), true);
            helper.shouldFail(shStructure.test(ctx), "StructureFeature is not a nether bastion [exact = true]");
        });

        AllTests.add("ContainsLootCondition", helper -> {
            LootContext ctx = helper.unknownContext(helper.player.position());
            ILootContextData data = ctx.getParamOrNull(Constants.DATA);
            if (data == null) {
                throw new RuntimeException("Should exist!");
            }

            data.setGeneratedLoot(Arrays.asList(new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND_HELMET)));

            ContainsLootCondition nonExact = new ContainsLootCondition(itemStack -> itemStack.getItem() ==
                                                                                    Items.DIAMOND, false);
            helper.shouldSucceed(nonExact.test(ctx), "Contains diamond");

            ContainsLootCondition exact = new ContainsLootCondition(itemStack -> itemStack.getItem() == Items.DIAMOND ||
                                                                                 itemStack.getItem() ==
                                                                                 Items.DIAMOND_HELMET, true);
            helper.shouldSucceed(exact.test(ctx), "Contains diamond & helmet");

            ContainsLootCondition nonExactNotExist = new ContainsLootCondition(itemStack -> itemStack.getItem() ==
                                                                                            Items.NETHER_BRICK, false);
            helper.shouldFail(nonExactNotExist.test(ctx), "No nether brick");

            ContainsLootCondition exactFailNotExist = new ContainsLootCondition(itemStack ->
                    itemStack.getItem() == Items.DIAMOND || itemStack.getItem() == Items.NETHER_BRICK, true);
            helper.shouldFail(exactFailNotExist.test(ctx), "No nether brick");

            ContainsLootCondition exactFail = new ContainsLootCondition(itemStack -> itemStack.getItem() ==
                                                                                     Items.DIAMOND, true);
            helper.shouldFail(exactFail.test(ctx), "Contains only diamond");
        });

        AllTests.add("IsLightLevel", helper -> {
            LootContext ctx = helper.unknownContext(helper.player.position());

            IsLightLevel isLightLevel = new IsLightLevel(0, 15);
            helper.shouldSucceed(isLightLevel.test(ctx), "Light level");

            isLightLevel = new IsLightLevel(0, 0);
            helper.shouldFail(isLightLevel.test(ctx), "Not dark enough");
        });

        AllTests.add("MatchEquipmentSlot", helper -> {
            LootContext.Builder builder = new LootContext.Builder(helper.level)
                    .withParameter(LootContextParams.ORIGIN, helper.player.position())
                    .withParameter(LootContextParams.THIS_ENTITY, helper.player);
            LootContext ctx = builder.create(LootContextParamSets.CHEST);

            helper.player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
            helper.player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.STICK));
            helper.player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            helper.player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            helper.player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            helper.player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));

            Function<Item, Predicate<ItemStack>> generator = item -> itemStack -> itemStack.getItem() == item;
            MatchEquipmentSlot slotCheck = new MatchEquipmentSlot(EquipmentSlot.MAINHAND,
                    generator.apply(Items.DIAMOND));
            helper.shouldSucceed(slotCheck.test(ctx), "Match slot " + EquipmentSlot.MAINHAND.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.OFFHAND, generator.apply(Items.STICK));
            helper.shouldSucceed(slotCheck.test(ctx), "Match slot " + EquipmentSlot.OFFHAND.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.HEAD, generator.apply(Items.DIAMOND_HELMET));
            helper.shouldSucceed(slotCheck.test(ctx), "Match slot " + EquipmentSlot.HEAD.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.CHEST, generator.apply(Items.DIAMOND_CHESTPLATE));
            helper.shouldSucceed(slotCheck.test(ctx), "Match slot " + EquipmentSlot.CHEST.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.LEGS, generator.apply(Items.DIAMOND_LEGGINGS));
            helper.shouldSucceed(slotCheck.test(ctx), "Match slot " + EquipmentSlot.MAINHAND.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.FEET, generator.apply(Items.DIAMOND_BOOTS));
            helper.shouldSucceed(slotCheck.test(ctx), "Match slot " + EquipmentSlot.FEET.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.MAINHAND, generator.apply(Items.APPLE));
            helper.shouldFail(slotCheck.test(ctx), "Not matching slot " + EquipmentSlot.MAINHAND.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.OFFHAND, generator.apply(Items.GREEN_CARPET));
            helper.shouldFail(slotCheck.test(ctx), "Not matching slot " + EquipmentSlot.OFFHAND.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.HEAD, generator.apply(Items.LEATHER_HELMET));
            helper.shouldFail(slotCheck.test(ctx), "Not matching slot " + EquipmentSlot.HEAD.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.CHEST, generator.apply(Items.LEATHER_CHESTPLATE));
            helper.shouldFail(slotCheck.test(ctx), "Not matching slot " + EquipmentSlot.CHEST.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.LEGS, generator.apply(Items.LEATHER_LEGGINGS));
            helper.shouldFail(slotCheck.test(ctx), "Not matching slot " + EquipmentSlot.MAINHAND.name());

            slotCheck = new MatchEquipmentSlot(EquipmentSlot.FEET, generator.apply(Items.LEATHER_BOOTS));
            helper.shouldFail(slotCheck.test(ctx), "Not matching slot " + EquipmentSlot.FEET.name());
        });

        AllTests.add("MatchKillerDistance", helper -> {
            Cow cow = helper.simpleEntity(EntityType.COW, helper.player.blockPosition().offset(5, 0, 0));
            LootContext ctx = cow
                    .createLootContext(true, DamageSource.playerAttack(helper.player))
                    .create(LootContextParamSets.ENTITY);

            double dist = helper.player.distanceTo(cow);
            MatchKillerDistance mkd = new MatchKillerDistance(new DistancePredicateBuilder()
                    .absolute(new MinMaxBounds.Doubles(dist - 1f, dist + 1f))
                    .build());
            helper.shouldSucceed(mkd.test(ctx), "Distance to mob is correct");

            mkd = new MatchKillerDistance(new DistancePredicateBuilder()
                    .absolute(new MinMaxBounds.Doubles(1000d, 1000d))
                    .build());
            helper.shouldFail(mkd.test(ctx), "Distance to mob is incorrect");

            helper.yeet(cow);
        });

        AllTests.add("AndCondition", helper -> {
            LootContext ctx = helper.unknownContext(helper.player.position());
            AndCondition ac = new AndCondition(new AnyDimension(new ResourceLocation[]{ new ResourceLocation("overworld") }),
                    new IsLightLevel(0, 15));
            helper.shouldSucceed(ac.test(ctx), "Matches all conditions");

            ac = new AndCondition(new AnyDimension(new ResourceLocation[]{ new ResourceLocation("overworld") }),
                    new IsLightLevel(0, 7),
                    new IsLightLevel(8, 15));
            helper.shouldFail(ac.test(ctx), "Fails on light check");
        });

        AllTests.add("OrCondition", helper -> {
            LootContext ctx = helper.unknownContext(helper.player.position());
            OrCondition or = new OrCondition(new AnyDimension(new ResourceLocation[]{ new ResourceLocation("overworld") }),
                    new IsLightLevel(0, 15));
            helper.shouldSucceed(or.test(ctx), "Matches");

            or = new OrCondition(new IsLightLevel(0, 7), new IsLightLevel(8, 15));
            helper.shouldSucceed(or.test(ctx), "Still match");

            or = new OrCondition(new AnyDimension(new ResourceLocation[]{ new ResourceLocation("nether") }),
                    new AnyDimension(new ResourceLocation[]{ new ResourceLocation("end") }));
            helper.shouldFail(or.test(ctx), "Not in nether or end");
        });

        AllTests.add("NotCondition", helper -> {
            LootContext ctx = helper.unknownContext(helper.player.position());

            NotCondition not = new NotCondition(new AnyDimension(new ResourceLocation[]{
                    new ResourceLocation("nether")
            }));
            helper.shouldSucceed(not.test(ctx), "Match because we are not in nether");

            not = new NotCondition(new AnyDimension(new ResourceLocation[]{
                    new ResourceLocation("overworld")
            }));
            helper.shouldFail(not.test(ctx), "We are in overworld but NotCondition");
        });
    }
}

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## Unreleased
- Fixed a bug where loot entries are not correctly removed from loot tables

## [3.2.2] - 2024-10-27

- Added `matchAnyInventorySlot(itemFilter)` and `matchAnyHotbarSlot(itemFilter)`

## [3.1.2] - 2024-10-14

- Fix loot modification being skipped when loot table doesn't contain any entries

## [3.1.1] - 2024-08-25

- Fix `print` method for loot tables. Will now print the entries correctly.
- Fix `matchCustomCondition` and `jsonFunction` using wrong codec.

## [3.1.0] - 2024-08-03

- Remove `addItem` for loot pools. Prefer to use `addEntry`
- Deprecate `addTypeModifier` in `LootJS.modifiers` and will be removed in future updates, use `addTableModifier` with a
  LootType instead.
- Deprecate `modifyLootTypeTables` in `LootJS.lootTables` and will be removed in future updates, use `modifyLootTables`
  with a LootType instead.

## [3.0.4] - 2024-07-25

- Fix `.create` in loot tables event

## [3.0.3] - 2024-07-22

- Bump kubejs version

## [3.0.2] - 2024-07-16

- Add quick way to negate simple `ItemFilter`s like tag or id filter by using `!`, e.g. `!#c:tools`
- Fix regex handling with id filters
- Rename `removeGlobalModifier` to `removeGlobalModifiers` in `LootJS.modifiers` event.

## [3.0.1] - 2024-07-07

### Fixes

- Fix error `Tag does not exist` when using ItemPredicates with tags

## [3.0.0] - 2024-07-06

### Changes

- New wiki https://docs.almostreliable.com/lootjs/
- Now on `neoforge`
- Added `LootJS.loot_tables` event for direct loot table modification
- Add `LootBucket` as wrapper class for loot for easier execute some helper functions
- `LootEntry` does not only represent single items anymore. It now represents all different loot entries vanilla
  minecraft has.
    - `LootEntry.of(item)`: Single item
    - `LootEntry.empty()`: Empty entry
    - `LootEntry.tag(tag)`: Tag entry
    - `LootEntry.reference(lootTable)`: Reference to another loot table. For
      example, `LootEntry.reference("minecraft:chests/abandoned_mineshaft")`
    - `LootEntry.alternative(lootEntries...)`: Alternative loot
    - `LootEntry.sequence(lootEntries...)`: Sequence loot
    - `LootEntry.group(lootEntries...)`: Grouped loot
- Changes to `LootJS.modifiers` event:
    - Loot modifiers are not executed in order anymore. It will now work like a loot table. First check for conditions
      and if it contains matching loot, then apply all actions and then apply loot functions.
    - `addLootTableModifier`, `addLootTypeModifier`, `addBlockLootModifier`, `addEntityLootModifier` renamed
      into `addTableModifier`, `addTypeModifier`, `addBlockModifier`, `addEntityModifier`
    - `.functions(itemFilter, (f) => {})` removed. Better to just use `group` now
    - Added `.group((group) => {...})` or `.group(itemFilter, (item) => {...})`. Second one will pre-filter current loot
      for further modifications.
        - `.group()` can use `.rolls(numberprovider)` to execute the group multiple times
    - `.pool()` now actually consumes a vanilla `LootPool`
    - Removed `addWeightedLoot()`, use `.pool()` instead now
    - `matchLoot` renamed into `containsLoot`
    - `.dropExperience` can now use a number provider instead of a fixed amount
    - `LootContextJS` wrapper removed. Using `LootContext` instead but added all helper methods from `LootContextJS` too
- Renamed some loot functions
- Renamed some loot conditions
- Changes to `ItemFilter`:
    - `.hasEnchantments(...)` only works for item enchantments now. For `stored` enchantments
      use `.hasStoredEnchantments(...)`
    - Removed some filters as they are no longer needed with the new data components.
    - Renamed `ALWAYS_TRUE` into `ALL` and `ALWAYS_FALSE` into `NONE`
- Added `vault`, `block_use` and `shearing` LootType
- And probably more stuff I may missed, sorry! :D Feel free to ask in our discord.

## [2.10.3] - 2023-09-27

### Fixed

- Fixed null pointer exception when using `addLootTableModifier` and an entity dies

## [2.10.2] - 2023-09-23

### Add

- Add custom loot function `customFunction(json)`

## [2.10.1] - 2023-09-18

### Fix

- Fix bug where loot does not drop for entities on fabric

## [2.10.0] - 2023-09-03

### Changed

- 1.20.1 Release

## [2.9.1] - 2023-08-17

### Changed

- Fix bug where loot does not drop if CraftTweaker is installed together with LootJS on fabric

## [2.9.0] - 2023-07-18

### Changed

- Update to KubeJS 6.1

## [2.8.0] - 2023-04-12

### Added

- Added `LootEntry.when()` and `blockEntityPredicate`

## [2.7.11] - 2023-04-03

### Fix

- Fix item filters on forge. Moved the specific filters from last update to `ForgeItemFilter.canPerformAnyAction()`
  and `ForgeItemFilter.canPerformAction()`.

## [2.7.10] - 2023-04-02

### Added

- Added `ItemFilter.canPerformAnyAction()` and `ItemFilter.canPerformAction()` for Forge to check if an item can perform
  a tool action.

### Fix

- Fix crash when trying to remove global modifiers from forge

## [2.7.9] - 2023-01-08

### Changed

- Add structure tags `#tag` support in `anyStructure()` condition
- Remove structure id validation in `anyStructure()` because of timing issues ... :')
- Fix load issue on fabric (Should fix weird null pointer exception on fabric)

## [2.7.8] - 2023-01-08

### Changed

- Fix ([#10](https://github.com/AlmostReliable/lootjs/issues/10))
- Remove tag validation because of timing issues

## [2.7.7] - 2022-11-23

### Added

- `disableWitherStarDrop()`, `disableCreeperHeadDrop()`, `disableSkeletonHeadDrop()`, `disableZombieHeadDrop()`

## [2.7.6] - 2022-11-14

### Changed

- Bump KubeJS Version ([#9](https://github.com/AlmostReliable/lootjs/issues/9))

## [2.7.5] - 2022-09-02

### Changed

- Fix `addBlockLootModifier` when using tags

## [2.7.4] - 2022-07-30

### Changed

- Fix invalid items result into an always true filter.
- Change `ItemFilter.hasEnchantment()` so it works with regex and enchanted books.

## [2.7.3] - 2022-07-05

### Changed

- Fix loot context check for params when loot is rolled multiple times.

## [2.7.2] - 2022-07-05

### Changed

- Fabric Fix: Old drop still dropped.

## [2.7.1] - 2022-07-04

### Changed

- Fabric Fix: Modifications did not trigger for entities.

## [2.7.0] - 2022-06-27

### Added

- Hello Fabric! LootJS is now for Fabric

## [2.6.0] - 2022-06-23

### Added

- `replaceLoot(filter, entry, preserveCount)` now exists with the third argument to preserve item count (use true or
  false)

### Changed

- `replaceLoot` now also takes an `LootEntry` as the second argument

## [2.5.0] - 2022-06-16

### Added

- Added `dropExperience(amount)` action to drop experience orbs

## [2.4.0] - 2022-06-15

### Added

- Added `LootEntry` to directly call loot functions on items.
- Added `limitCount` function with single argument.

### Changed

- `.pool()` action is now `.group()`. There was some misunderstanding for the user as LootJS pools work differently than
  vanilla pools.

### Removed

- Remove deprecated actions from 2.3.0

## [2.3.0-beta] - 2022-04-18

We are now on 1.18.2!

### Added

- [ItemFilter](https://github.com/AlmostReliable/lootjs/wiki/1.18.2-Types#itemfilters)
- [Functions](https://github.com/AlmostReliable/lootjs/wiki/1.18.2-Functions) to apply loot table functions
- [CustomPlayerAction](https://github.com/AlmostReliable/lootjs/wiki/1.18.2-Actions#playeractioncallback)

### Changed

- Refactoring the builders for loot modifications.
- Removing usage of kubejs `DamageSourceJS` and use the vanilla damage source.
- Rename actions. You can find the new names [here](https://github.com/AlmostReliable/lootjs/wiki/1.18.2-Actions)
- `biome()` & `anyBiome()` conditions now uses the new tag system

## [2.2.0] - 2022-03-15

### Added

- Action `thenRollPool`

### Changed

- Refactor loot logging

## [2.1.0] - 2022-03-04

### Added

- Action `thenAddWeighted`
- Conditions `hasAnyStage`, `playerPredicate`, `entityPredicate`, `directKillerPredicate`, `killerPredicate`

### Changed

- bump to newest KubeJS version

## [2.0.0] - 2022-01-25

- initial release for 1.18.1

<!-- Links -->

[keep a changelog]: https://keepachangelog.com/en/1.0.0/

[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->

[3.2.2]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21.1-neoforge-3.2.2
[3.1.2]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21.1-neoforge-3.1.2
[3.1.1]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.1.1
[3.1.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.1.0

[3.0.4]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.0.4

[3.0.3]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.0.3

[3.0.2]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.0.2

[3.0.1]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.0.1

[3.0.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.21-neoforge-3.0.0

[2.10.3]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.10.3

[2.10.2]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.10.2

[2.10.1]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.10.1

[2.9.1]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.9.1

[2.9.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.9.0

[2.7.11]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.11

[2.7.10]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.10

[2.7.9]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.9

[2.7.8]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.8

[2.7.7]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.7

[2.7.6]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.6

[2.7.5]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.5

[2.7.4]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.4

[2.7.3]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.3

[2.7.2]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.2

[2.7.1]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.1

[2.7.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.7.0

[2.6.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.6.0

[2.5.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.5.0

[2.4.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.4.0

[2.3.0-beta]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.3.0-beta

[2.2.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.2.0

[2.1.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.1.0

[2.0.0]: https://github.com/AlmostReliable/lootjs/releases/tag/v1.18-2.0.0

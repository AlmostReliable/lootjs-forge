plugins {
    id("net.neoforged.moddev") version "2.0.39-beta"
    id("com.almostreliable.almostgradle") version "1.1.+"
}

repositories {
    maven("https://maven.saps.dev/minecraft")
    maven("https://www.cursemaven.com")
}

almostgradle.setup {
    testMod = true
}

dependencies {
    val kubejsVersion: String by project
    implementation("dev.latvian.mods:kubejs-neoforge:${kubejsVersion}")
    testImplementation("dev.latvian.mods:kubejs-neoforge:${kubejsVersion}")

    testLocalRuntime(almostgradle.recipeViewers.emi.dependency)
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    application
}

group = "dev.alshakib.feeds"
version = "0.4-alpha"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.1.1")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")

    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")

    implementation("org.jsoup:jsoup:1.15.3")

    implementation("com.rometools:rome:1.18.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("app.MainKt")
}

tasks.jar {
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output
    from(contents)
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
}

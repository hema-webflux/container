plugins {
    id("java")
    id("maven-publish")
    signing
}

val userHome: String = System.getProperty("user.home")
val signingConfigureFile = findProperty("signing.configure.file")
apply(from = String.format("%s/.gradle/%s", userHome, signingConfigureFile))

group = findProperty("package.group") as String
version = findProperty("package.version") as String

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {

            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components["java"])

            pom {
                name = project.name
                description = property("pom.description") as String
                url = findProperty("pom.scm.url") as String

                licenses {
                    license {
                        name = findProperty("pom.license.name") as String
                        url = findProperty("pom.license.url") as String
                    }
                }

                developers {
                    developer {
                        id = findProperty("pom.developer.id") as String
                        name = findProperty("pom.developer.name") as String
                        email = findProperty("pom.developer.email") as String
                    }
                }

                scm {
                    connection = findProperty("pom.scm.connection") as String
                    developerConnection = findProperty("pom.scm.developer.connection") as String
                    url = findProperty("pom.scm.url") as String
                }

            }
        }
    }

    repositories {
        maven {

            url = uri(findProperty("nexus.release.url") as String)

            credentials {
                username = project.ext.get("sonaUsername") as String
                password = project.ext.get("sonaPassword") as String
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

dependencies {
    implementation("io.github.hema-webflux:inflector:1.1")
    implementation("org.springframework:spring-context:6.1.9")
    implementation("org.json:json:20240303")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.0") {
        exclude("com.vaadin.external.google", "android-json")
    }
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    val args = listOf("-XX:+EnableDynamicAgentLoading", "-Xshare:off", "-Dfile.encoding=UTF-8")

    test {
        jvmArgs(args)
    }
}
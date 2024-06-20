plugins {
    id("java")
    id("maven-publish")
    signing
}

val userHome: String = System.getProperty("user.home")
val signingConfigureFile = findProperty("SIGNING_CONFIGURE_FILE")
apply(from = String.format("%s/.gradle/%s", userHome, signingConfigureFile))

group = findProperty("PACKAGE_GROUP") as String
version = findProperty("PACKAGE_VERSION") as String

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
                description = property("POM_DESCRIPTION") as String
                url = findProperty("POM_SCM_URL") as String

                licenses {
                    license {
                        name = findProperty("POM_LICENSE_NAME") as String
                        url = findProperty("POM_LICENSE_URL") as String
                    }
                }

                developers {
                    developer {
                        id = findProperty("POM_DEVELOPER_ID") as String
                        name = findProperty("POM_DEVELOPER_NAME") as String
                        email = findProperty("POM_DEVELOPER_EMAIL") as String
                    }
                }

                scm {
                    connection = findProperty("POM_SCM_CONNECTION") as String
                    developerConnection = findProperty("POM_SCM_DEVELOPER_CONNECTION") as String
                    url = findProperty("POM_SCM_URL") as String
                }

            }
        }
    }

    repositories {
        maven {

            val isSnapshot = version.toString().endsWith("SNAPSHOT")

            val repositories: String = if (isSnapshot) "SNAPSHOT" else "RELEASE"

            url = uri(findProperty("NEXUS_${repositories}_URL") as String)

            credentials {
                username = project.ext.get("sonaUsername").toString()
                password = project.ext.get("sonaPassword").toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

dependencies {
    implementation("io.github.hema-webflux:inflector:1.1")
    // https://mvnrepository.com/artifact/org.springframework/spring-context
    implementation("org.springframework:spring-context:6.1.9")
    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20240303")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.0") {
        exclude("com.vaadin.external.google","android-json")
    }
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    val args = listOf("-XX:+EnableDynamicAgentLoading", "-Xshare:off", "-Dfile.encoding=UTF-8")

    test {
        jvmArgs(args)
    }
}
/*
 * Copyright 2019 Russell Wolf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.russhwolf.settings.build.standardConfiguration
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4-jetbrains-3"
}
apply(from = "../gradle/publish.gradle")

standardConfiguration(
    "android",
//    "androidNativeArm32",
//    "androidNativeArm64",
//    "androidNativeX64",
//    "androidNativeX86",
    "iosArm32",
    "iosArm64",
    "iosX64",
    "js",
    "jvm",
//    "linuxArm32Hfp",
//    "linuxArm64",
//    "linuxMips32",
//    "linuxMipsel32",
    "linuxX64",
    "macosX64",
    "mingwX64",
//    "mingwX86",
    "tvosArm64",
    "tvosX64",
//    "wasm32",
    "watchosArm32",
    "watchosArm64",
    "watchosX86"
)
kotlin {
    val coroutineVersion = "1.3.4"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":multiplatform-settings"))

                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutineVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":multiplatform-settings-test"))

                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        targets
            .withType<KotlinNativeTarget>()
            .matching { true }
            .configureEach {
                compilations["main"].defaultSourceSet.dependsOn(nativeMain)
                compilations["test"].defaultSourceSet.dependsOn(nativeTest)
            }

        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.12")
                implementation("androidx.test:core:1.2.0")
                implementation("androidx.test.ext:junit:1.1.1")
                implementation("org.robolectric:robolectric:4.3")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.12")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

android {
    testOptions.unitTests.isIncludeAndroidResources = true
}


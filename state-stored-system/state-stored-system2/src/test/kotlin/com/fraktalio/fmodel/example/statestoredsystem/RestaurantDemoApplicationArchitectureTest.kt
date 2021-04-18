/*
 * Copyright (c) 2021 Fraktalio D.O.O. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fraktalio.fmodel.example.statestoredsystem

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AnalyzeClasses(packagesOf = [RestaurantDemoApplicationArchitectureTest::class])
class RestaurantDemoApplicationArchitectureTest {

    @ArchTest
    val `there are no package cycles` =
        SlicesRuleDefinition.slices()
            .matching("$BASE_PACKAGE.(**)..")
            .should()
            .beFreeOfCycles()

    @ArchTest
    val `the domain does not have outgoing dependencies` =
        noClasses()
            .that()
            .resideInAPackage("$DOMAIN_PACKAGE..")
            .should()
            .accessClassesThat()
            .resideInAnyPackage("$ADAPTER_PACKAGE..", "$APPLICATION_PACKAGE..")

    @ArchTest
    val `the application does not access the infrastructure (adapters)` =
        noClasses()
            .that()
            .resideInAPackage("$APPLICATION_PACKAGE..")
            .should()
            .accessClassesThat()
            .resideInAPackage("$ADAPTER_PACKAGE..")

    companion object {
        private val BASE_PACKAGE = RestaurantDemoApplicationArchitectureTest::class.java.`package`.name

        private val DOMAIN_PACKAGE = "$BASE_PACKAGE.domain"
        private val APPLICATION_PACKAGE = "$BASE_PACKAGE.application"
        private val ADAPTER_PACKAGE = "$BASE_PACKAGE.adapter"
    }

}

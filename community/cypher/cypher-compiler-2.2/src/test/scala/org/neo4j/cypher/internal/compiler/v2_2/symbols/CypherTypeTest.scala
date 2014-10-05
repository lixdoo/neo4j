/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_2.symbols

import org.neo4j.cypher.internal.commons.CypherFunSuite

class CypherTypeTest extends CypherFunSuite {

  test("test parents") {
    assert(CTInteger.parents === Seq(CTNumber, CTAny))
    assert(CTNumber.parents === Seq(CTAny))
    assert(CTAny.parents === Seq())
    assert(CTCollection(CTString).parents === Seq(CTCollection(CTAny), CTAny))
  }

  test("test types are assignable") {
    assert(CTNumber.isAssignableFrom(CTInteger) === true)
    assert(CTAny.isAssignableFrom(CTString) === true)
    assert(CTCollection(CTString).isAssignableFrom(CTCollection(CTString)) === true)
    assert(CTCollection(CTNumber).isAssignableFrom(CTCollection(CTInteger)) === true)
    assert(CTInteger.isAssignableFrom(CTNumber) === false)
    assert(CTCollection(CTInteger).isAssignableFrom(CTCollection(CTString)) === false)
  }

  test("test type leastUpperBound") {
    assertLeastUpperBound(CTNumber, CTNumber, CTNumber)
    assertLeastUpperBound(CTNumber, CTAny, CTAny)
    assertLeastUpperBound(CTNumber, CTString, CTAny)
    assertLeastUpperBound(CTNumber, CTCollection(CTAny), CTAny)
    assertLeastUpperBound(CTInteger, CTFloat, CTNumber)
    assertLeastUpperBound(CTMap, CTFloat, CTAny)
  }

  test("test type greatestLowerBound") {
    assertGreatestLowerBound(CTNumber, CTNumber, Some(CTNumber))
    assertGreatestLowerBound(CTNumber, CTAny, Some(CTNumber))
    assertGreatestLowerBound(CTCollection(CTNumber), CTCollection(CTInteger), Some(CTCollection(CTInteger)))
    assertGreatestLowerBound(CTNumber, CTString, None)
    assertGreatestLowerBound(CTNumber, CTCollection(CTAny), None)
    assertGreatestLowerBound(CTInteger, CTFloat, None)
    assertGreatestLowerBound(CTMap, CTFloat, None)
    assertGreatestLowerBound(CTBoolean, CTCollection(CTAny), None)
  }

  private def assertGreatestLowerBound(a: CypherType, b: CypherType, result: Option[CypherType]) {
    val simpleMergedType: Option[CypherType] = a greatestLowerBound b
    assert(simpleMergedType === result)
    val collectionMergedType: Option[CypherType] = CTCollection(a) greatestLowerBound CTCollection(b)
    assert(collectionMergedType === (for (t <- result) yield CTCollection(t)))
  }

  private def assertLeastUpperBound(a: CypherType, b: CypherType, result: CypherType) {
    val simpleMergedType: CypherType = a leastUpperBound b
    assert(simpleMergedType === result)
    val collectionMergedType: CypherType = CTCollection(a) leastUpperBound CTCollection(b)
    assert(collectionMergedType === CTCollection(result))
  }

}
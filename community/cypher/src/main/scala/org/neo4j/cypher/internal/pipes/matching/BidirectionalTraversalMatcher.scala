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
package org.neo4j.cypher.internal.pipes.matching

import org.neo4j.graphdb._
import org.neo4j.cypher.internal.pipes.{ExecutionContext, QueryState}
import traversal._
import java.lang.{Iterable => JIterable}
import collection.JavaConverters._
import org.neo4j.kernel.{StandardBranchCollisionDetector, Uniqueness, Traversal}
import org.neo4j.kernel.impl.traversal.BranchCollisionPolicy
import org.neo4j.helpers.ThisShouldNotHappenError


class BidirectionalTraversalMatcher(steps: ExpanderStep,
                                    start: ExecutionContext => Iterable[Node],
                                    end: ExecutionContext => Iterable[Node]) extends TraversalMatcher {

  lazy val reversedSteps = steps.reverse()

  val initialStartStep = new InitialStateFactory[Option[ExpanderStep]] {
    def initialState(path: Path): Option[ExpanderStep] = Some(steps)
  }

  val initialEndStep = new InitialStateFactory[Option[ExpanderStep]] {
    def initialState(path: Path): Option[ExpanderStep] = Some(reversedSteps)
  }
  val baseTraversal: TraversalDescription = Traversal.traversal(Uniqueness.RELATIONSHIP_PATH)
  val collisionDetector = new StepCollisionDetector

  def findMatchingPaths(state: QueryState, context: ExecutionContext): Iterator[Path] = {
    val s = start(context).toList
    val e = end(context).toList

    def produceTraversalDescriptions() = {
      val startWithoutCutoff = baseTraversal.expand(new TraversalPathExpander(context), initialStartStep)
      val endWithoutCutOff = baseTraversal.expand(new TraversalPathExpander(context), initialEndStep)

      steps.size match {
        case None       => (startWithoutCutoff, endWithoutCutOff)
        case Some(size) => {
          val startDepth = atLeastOne(size / 2)
          val endDepth = atLeastOne(size - startDepth)
          (startWithoutCutoff.evaluator(Evaluators.toDepth(startDepth)),
            endWithoutCutOff.evaluator(Evaluators.toDepth(endDepth)))
        }
      }
    }

    val (startDescription, endDescription) = produceTraversalDescriptions()

    val result = Traversal.bidirectionalTraversal()
      .startSide(startDescription)
      .endSide(endDescription)
      .collisionPolicy(collisionDetector)
      .traverse(s.asJava, e.asJava).iterator()

    result.asScala
  }


  def atLeastOne(i: Int): Int = if (i < 1) 1 else i


  class StepCollisionDetector extends StandardBranchCollisionDetector(null) with BranchCollisionPolicy {
    override def includePath(path: Path, startPath: TraversalBranch, endPath: TraversalBranch): Boolean = {
      val s = startPath.state().asInstanceOf[Option[ExpanderStep]]
      val e = endPath.state().asInstanceOf[Option[ExpanderStep]]

      val (include, prune) = (s, e) match {
        case (Some(startStep), Some(endStep)) =>
          val foundEnd = endStep.id + 1 == startStep.id
          val includeButDoNotPrune = endStep.id == startStep.id && endStep.shouldInclude() || startStep.shouldInclude()
          (foundEnd || includeButDoNotPrune, foundEnd)

        case (Some(x), None) =>
          val result = startPath.length() == 0
          (result, true)

        case (None, Some(x)) =>
          val result = endPath.length() == 0
          (result, true)

        case _ => (false, false)
      }

      if (prune) {
        startPath.prune()
        endPath.prune()
      }

      include
    }

    def create(evaluator: Evaluator) = new StepCollisionDetector
  }

}
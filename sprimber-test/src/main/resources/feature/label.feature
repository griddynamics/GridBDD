# /*
# Copyright (c) 2010-2018 Grid Dynamics International, Inc. All Rights Reserved
# http://www.griddynamics.com
#
# This library is free software; you can redistribute it and/or modify it under the terms of
# the GNU Lesser General Public License as published by the Free Software Foundation; either
# version 2.1 of the License, or any later version.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# $Id:
# @Project:     Sprimber
# @Description: Framework that provide bdd engine and bridges for most popular BDD frameworks
# */

@labels
@epic:CustomEpic
@parentSuite:FirstSuite
@feature:SomeCopyPasteFeature
@suite:SuiteLikeAFeature
Feature: This is a test feature for echo application

  @echo
  @story:StoryThatLooksLikeAsAScenario
  Scenario: Simple scenario to check just steps

    This is possible to add additional long tasting description for each scenario

    Given test given action
    When test when action
    Then test then action

  @echo
  @story:MultipleLabelValues,somethingElse
  Scenario: Simple scenario to check steps with DataTable arguments
    Given the next author exist:
      | name     | surname     | book     |
      | testName | testSurname | testBook |

  @echo
  @story:StoryThatLooksLikeAsAScenario
  @subSuite:customSubSuite
  Scenario: Simple scenario to check steps arguments
    Given the next author long consumed '123'

  @echo
  @subSuite:customSubSuite
  @package:smoke
  Scenario: Scenario to check failed steps
    When some when action with param 'hi'
    Then every time failed action
    And test then action

  @echo
  @subSuite:criticalPart
  @story:criticalTests
  @severity:critical
  Scenario: Scenario to check broken steps
    When some when action with param 'hi'
    Then every time action with exception
    And test then action

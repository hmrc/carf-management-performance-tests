/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.simulation

import uk.gov.hmrc.performance.simulation.PerformanceTestRunner
import uk.gov.hmrc.perftests.requests.ManagementRequests._
import uk.gov.hmrc.perftests.requests._

class Simulation extends PerformanceTestRunner {

  setup("OrgAutomatched", "Automatched Organisation RCASP is user add journey").withChainedActions(
    getAuthLoginPage,
    postAuthLoginPage("automatched"),
    getManagementDashboardPage,
    getManageYourRcaspsPage,
    getReportForRegisteredBusinessPage,
    postReportForRegisteredBusinessPage,
    getIsThisYourBusinessNamePage,
    postIsThisYourBusinessNamePage,
    getHaveTradingNamePage,
    postHaveTradingNamePage("automatched","false"),
    getIsTheAddressCorrectPage,
    postIsTheAddressCorrectPage,
    getEndOfJourneyPage,
    getRegisteredBusinessCheckAnswersPage,
    postRegisteredBusinessCheckAnswersPage,
    getRcaspAddedPage
  )

  setup("OrgNonAutoMatched", "Organisation Non Automatched add RCASP journey").withChainedActions(
    getAuthLoginPage,
    postAuthLoginPage("otherOrg"),
    getManagementDashboardPage,
    getManageYourRcaspsPage,
    getOrganisationOrIndividualPage,
    postOrganisationOrIndividualPage("otherOrg"),
    getOrganisationNamePage,
    postOrganisationNamePage,
    getHaveTradingNamePage,
    postHaveTradingNamePage("otherOrg","true"),
    getTradingNamePage,
    postTradingNamePage,
    getUtrPage,
    postUtrPage,
    getFindAddressPage,
    postFindAddressPage(true),
    getReviewAddressPage,
    getReviewAddressSubmitPage,
    getContactNamePage,
    postContactNamePage,
    getEmailPage,
    postEmailPage,
    getHavePhonePage,
    postHavePhonePage,
    getHaveSecondContactPage,
    postHaveSecondContactPage,
    getSecondContactNamePage,
    postSecondContactNamePage,
    getSecondContactEmailPage,
    postSecondContactEmailPage,
    getSecondContactHavePhonePage,
    postSecondContactHavePhonePage,
    getSecondContactPhonePage,
    postSecondContactPhonePage,
    getEndOfJourneyPage,
    getCheckAnswersPage,
    postCheckAnswersPage,
    getRcaspAddedPage
  )

  setup("Individual", "Individual add RCASP journey").withChainedActions(
    getAuthLoginPage,
    postAuthLoginPage("individual"),
    getManagementDashboardPage,
    getManageYourRcaspsPage,
    getOrganisationOrIndividualPage,
    postOrganisationOrIndividualPage("individual"),
    getIndividualNamePage,
    postIndividualNamePage,
    getNiNumberPage,
    postNiNumberPage,
    getFindAddressPage,
    postFindAddressPage(false),
    getChooseAddressPage,
    postChooseAddressPage,
    getAddressPage,
    postAddressPage,
    getIndividualEmailPage,
    postIndividualEmailPage,
    getIndividualHavePhonePage,
    postIndividualHavePhonePage,
    getIndividualPhonePage,
    postIndividualPhonePage,
    getEndOfJourneyPage,
    getCheckAnswersPage,
    postCheckAnswersPage,
    getRcaspAddedPage
  )

  setup("RemoveRCASP", "Remove RCASP journey").withChainedActions(
    getAuthLoginPage,
    postAuthLoginPage("otherOrg"),
    getManagementDashboardPage,
    getYourRcaspsPage,
    getAmazonRemoveUserAccessPage,
    postAmazonRemoveUserAccessPage,
    getRemoveOtherAccessPage,
    postRemoveOtherAccessPage,
    getRemoveRCASPPage,
    postRemoveRCASPPage,
    getRCASPRemovedPage
  )

  runSimulation()
}

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

package uk.gov.hmrc.perftests.requests

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration

object ManagementRequests extends ServicesConfiguration {

  val baseUrl: String     = baseUrlFor("carf-management-frontend")
  val route: String       = "/manage-cryptoasset-reports"
  val baseUrlAuth: String = baseUrlFor("auth-frontend")

  def inputSelectorByName(name: String): Expression[String] = s"input[name='$name']"

  val getAuthLoginPage: HttpRequestBuilder =
    http("Get Auth login page")
      .get(baseUrlAuth + "/auth-login-stub/gg-sign-in")
      .check(status.is(200))

  def postAuthLoginPage(userType: String): HttpRequestBuilder = {
    val (requestName, affinityGroup) = userType match {
      case "automatched" => ("Post Auth login page for Auto matched Org", "Organisation")
      case "otherOrg" => ("Post Auth login page for Non Auto matched Org", "Organisation")
      case "individual" => ("Post Auth login page", "Individual")
      case _ => ("Post Auth login page", "Individual")
    }

    val baseRequest = http(requestName)
      .post(baseUrlAuth + "/auth-login-stub/gg-sign-in")
      .formParam("authorityId", "")
      .formParam("credentialStrength", "strong")
      .formParam("excludeGnapToken", "false")
      .formParam("confidenceLevel", "50")
      .formParam("credentialRole", "User")
      .formParam("additionalInfo.emailVerified", "N/A")
      .formParam("email", "user@test.com")
      .formParam("affinityGroup", affinityGroup)
      .formParam("redirectionUrl", baseUrl + route)

    val finalRequest = userType match {
      case "automatched" =>
        baseRequest
          .formParam("enrolment[0].name", "HMRC-CARF-ORG")
          .formParam("enrolment[0].taxIdentifier[0].name", "CARFID")
          .formParam("enrolment[0].taxIdentifier[0].value", "RK1111")
          .formParam("enrolment[0].state", "Activated")
          .formParam("enrolment[4].name", "IR-CT")
          .formParam("enrolment[4].taxIdentifier[0].name", "UTR")
          .formParam("enrolment[4].taxIdentifier[0].value", "12345")
          .formParam("enrolment[4].state", "Activated")

      case "otherOrg" =>
        baseRequest
          .formParam("enrolment[0].name", "HMRC-CARF-ORG")
          .formParam("enrolment[0].taxIdentifier[0].name", "CARFID")
          .formParam("enrolment[0].taxIdentifier[0].value", "RN1111")
          .formParam("enrolment[0].state", "Activated")

      case "individual" =>
        baseRequest
          .formParam("enrolment[0].name", "HMRC-CARF-ORG")
          .formParam("enrolment[0].taxIdentifier[0].name", "CARFID")
          .formParam("enrolment[0].taxIdentifier[0].value", "LJ1111")
          .formParam("enrolment[0].state", "Activated")

      case _ =>
        baseRequest
          .formParam("enrolment[0].name", "HMRC-CARF-ORG")
          .formParam("enrolment[0].taxIdentifier[0].name", "CARFID")
          .formParam("enrolment[0].taxIdentifier[0].value", "LJ1111")
          .formParam("enrolment[0].state", "Activated")
    }

    finalRequest
      .check(status.is(303))
      .check(header("Location").is(baseUrl + route).saveAs("AuthLoginForCarfManagement"))
  }

  val getManagementDashboardPage: HttpRequestBuilder =
    http("Get Management Dashboard Page")
      .get(baseUrl + route)
      .check(status.is(200))

  val getManageYourRcaspsPage: HttpRequestBuilder =
    http("Get Manage your Rcasps Page")
      .get(baseUrl + "/manage-your-rcasps")
      .check(status.is(303))

  val getReportForRegisteredBusinessPage: HttpRequestBuilder =
    http("Get Report For Registered Business Page")
      .get(baseUrl + "/manage-your-rcasps/report-for-registered-business")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postReportForRegisteredBusinessPage: HttpRequestBuilder =
    http("Post Report For Registered Business Page")
      .post(baseUrl + "/manage-your-rcasps/report-for-registered-business")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/registered-business/is-this-your-business-name").saveAs("IsThisYourBusinessName"))

  val getIsThisYourBusinessNamePage: HttpRequestBuilder =
    http("Get Is This Your Business Name Page")
      .get(baseUrl + "#{IsThisYourBusinessName}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postIsThisYourBusinessNamePage: HttpRequestBuilder =
    http("Post Is This Your Business Name Page")
      .post(baseUrl + "#{IsThisYourBusinessName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/have-trading-name").saveAs("HaveTradingName"))

  val getHaveTradingNamePage: HttpRequestBuilder =
    http("Get Have Trading Name Page")
      .get(baseUrl + "#{HaveTradingName}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postHaveTradingNamePage: HttpRequestBuilder =
    http("Post Have Trading Name Page")
      .post(baseUrl + "#{HaveTradingName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "false")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/registered-business/is-the-address-correct").saveAs("IsTheAddressCorrect"))

  val getIsTheAddressCorrectPage: HttpRequestBuilder =
    http("Get Is The Address Correct Page")
      .get(baseUrl + "#{IsTheAddressCorrect}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postIsTheAddressCorrectPage: HttpRequestBuilder =
    http("Post Is The Address Correct Page")
      .post(baseUrl + "#{IsTheAddressCorrect}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/end-of-journey").saveAs("EndOfJourney"))

  val getEndOfJourneyPage: HttpRequestBuilder =
    http("Get End Of Journey Page")
      .get(baseUrl + "#{EndOfJourney}")
      .check(status.is(303))

  val getRegisteredBusinessCheckAnswersPage: HttpRequestBuilder =
    http("Get Registered Business Check Answers Page")
      .get(baseUrl + "/manage-your-rcasps/registered-business/check-answers")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postRegisteredBusinessCheckAnswersPage: HttpRequestBuilder =
    http("Post Registered Business Check Answers Page")
      .post(baseUrl + "/manage-your-rcasps/registered-business/check-answers")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/rcasp-added").saveAs("RcaspAdded"))

  val getRcaspAddedPage: HttpRequestBuilder =
    http("Get Rcasp Added Page")
      .get(baseUrl + "#{RcaspAdded}")
      .check(status.is(200))

  val getOrganisationOrIndividualPage: HttpRequestBuilder =
    http("Get Organisation or Individual Page")
      .get(baseUrl + "/manage-your-rcasps/organisation-or-individual")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postOrganisationOrIndividualPage: HttpRequestBuilder =
    http("Post Organisation or Individual Page")
      .post(baseUrl + "/manage-your-rcasps/organisation-or-individual")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "Organisation")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/organisation-name").saveAs("OrganisationName"))

  // TODO: Continue this journey - Login as RCASPisUser = false then add Organisation
}

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

  def postHaveTradingNamePage(orgType: String, value: String): HttpRequestBuilder = {
    val (location, saveAsKey) = (orgType, value) match {
      case (_, "true")               => ("/manage-your-rcasps/trading-name", "TradingName")
      case ("automatched", "false")  => ("/manage-your-rcasps/registered-business/is-the-address-correct", "IsTheAddressCorrect")
      case ("otherOrg", "false")     => ("/manage-your-rcasps/utr", "Utr")
    }

    http("Post Have Trading Name Page")
      .post(baseUrl + "#{HaveTradingName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", value)
      .check(status.is(303))
      .check(header("Location").is(location).saveAs(saveAsKey))
  }

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

  val getOrganisationNamePage: HttpRequestBuilder =
    http("Get Organisation Name Page")
      .get(baseUrl + "#{OrganisationName}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postOrganisationNamePage: HttpRequestBuilder =
    http("Post Organisation Name Page")
      .post(baseUrl + "#{OrganisationName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "Test Org Ltd")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/have-trading-name").saveAs("HaveTradingName"))

  val getTradingNamePage: HttpRequestBuilder =
    http("Get Trading Name Page")
      .get(baseUrl + "#{TradingName}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postTradingNamePage: HttpRequestBuilder =
    http("Post Trading Name Page")
      .post(baseUrl + "#{TradingName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "Test Organisation Limited")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/utr").saveAs("Utr"))

  val getUtrPage: HttpRequestBuilder =
    http("Get Utr Page")
      .get(baseUrl + "#{Utr}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postUtrPage: HttpRequestBuilder =
    http("Post Utr Page")
      .post(baseUrl + "#{Utr}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "1234567890")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/find-address").saveAs("FindAddress"))

  val getFindAddressPage: HttpRequestBuilder =
    http("Get Find Address Page")
      .get(baseUrl + "#{FindAddress}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postFindAddressPage: HttpRequestBuilder =
    http("Post Find Address Page")
      .post(baseUrl + "#{FindAddress}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("postcode", "LU1 5JP")
      .formParam("propertyNameOrNumber", "7")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/review-address").saveAs("ReviewAddress"))

  val getReviewAddressPage: HttpRequestBuilder =
    http("Get Review Address Page")
      .get(baseUrl + "#{ReviewAddress}")
      .check(status.is(200))

  val getReviewAddressSubmitPage: HttpRequestBuilder =
    http("Get Review Address Submit Page")
      .get(baseUrl + "/manage-your-rcasps/review-address-submit")
      .check(status.is(303))

  val getContactNamePage: HttpRequestBuilder =
    http("Get Contact Name Page")
      .get(baseUrl + "/manage-your-rcasps/contact-name")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postContactNamePage: HttpRequestBuilder =
    http("Post Contact Name Page")
      .post(baseUrl + "/manage-your-rcasps/contact-name")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "John Doe")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/email").saveAs("Email"))

  val getEmailPage: HttpRequestBuilder =
    http("Get Email Page")
      .get(baseUrl + "#{Email}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postEmailPage: HttpRequestBuilder =
    http("Post Email Page")
      .post(baseUrl + "#{Email}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "John.Doe@test.com")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/have-phone").saveAs("HavePhone"))

  val getHavePhonePage: HttpRequestBuilder =
    http("Get Have Phone Page")
      .get(baseUrl + "#{HavePhone}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postHavePhonePage: HttpRequestBuilder =
    http("Post Have Phone Page")
      .post(baseUrl + "#{HavePhone}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "false")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/have-second-contact").saveAs("HaveSecondContact"))

  val getHaveSecondContactPage: HttpRequestBuilder =
    http("Get Have Second Contact Page")
      .get(baseUrl + "#{HaveSecondContact}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postHaveSecondContactPage: HttpRequestBuilder =
    http("Post Have Second Contact Page")
      .post(baseUrl + "#{HaveSecondContact}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/second-contact-name").saveAs("SecondContactName"))

  val getSecondContactNamePage: HttpRequestBuilder =
    http("Get Second Contact Name Page")
      .get(baseUrl + "#{SecondContactName}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postSecondContactNamePage: HttpRequestBuilder =
    http("Post Second Contact Name Page")
      .post(baseUrl + "#{SecondContactName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "Jane Smith")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/second-contact-email").saveAs("SecondContactEmail"))

  val getSecondContactEmailPage: HttpRequestBuilder =
    http("Get Second Contact Email Page")
      .get(baseUrl + "#{SecondContactEmail}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postSecondContactEmailPage: HttpRequestBuilder =
    http("Post Second Contact Email Page")
      .post(baseUrl + "#{SecondContactEmail}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "jane.smith@test.com")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/second-contact-have-phone").saveAs("SecondContactHavePhone"))

  val getSecondContactHavePhonePage: HttpRequestBuilder =
    http("Get Second Contact Have Phone Page")
      .get(baseUrl + "#{SecondContactHavePhone}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postSecondContactHavePhonePage: HttpRequestBuilder =
    http("Post Second Contact Have Phone Page")
      .post(baseUrl + "#{SecondContactHavePhone}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/second-contact-phone").saveAs("SecondContactPhone"))

  val getSecondContactPhonePage: HttpRequestBuilder =
    http("Get Second Contact Phone Page")
      .get(baseUrl + "#{SecondContactPhone}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postSecondContactPhonePage: HttpRequestBuilder =
    http("Post Second Contact Phone Page")
      .post(baseUrl + "#{SecondContactPhone}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "1234567890")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/end-of-journey").saveAs("EndOfJourney"))

  val getCheckAnswersPage: HttpRequestBuilder =
    http("Get Check Answers Page")
      .get(baseUrl + "/manage-your-rcasps/check-answers")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postCheckAnswersPage: HttpRequestBuilder =
    http("Post Check Answers Page")
      .post(baseUrl + "/manage-your-rcasps/check-answers")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/rcasp-added").saveAs("RcaspAdded"))

}

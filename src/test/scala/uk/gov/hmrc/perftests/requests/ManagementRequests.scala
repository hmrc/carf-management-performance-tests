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

  def postAuthLoginPage(userType: String, CARFID: String): HttpRequestBuilder = {
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
      .formParam("enrolment[0].name", "HMRC-CARF-ORG")
      .formParam("enrolment[0].taxIdentifier[0].name", "CARFID")
      .formParam("enrolment[0].taxIdentifier[0].value", CARFID)
      .formParam("enrolment[0].state", "Activated")

    val finalRequest = if(userType == "automatched") {
      baseRequest
        .formParam("enrolment[4].name", "IR-CT")
        .formParam("enrolment[4].taxIdentifier[0].name", "UTR")
        .formParam("enrolment[4].taxIdentifier[0].value", "12345")
        .formParam("enrolment[4].state", "Activated")
    } else {
      baseRequest
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

  def postOrganisationOrIndividualPage(affinityGroup: String): HttpRequestBuilder = {
    val (formValue, locationPath, saveAsName) = affinityGroup match {
      case "otherOrg" => ("Organisation", "/manage-your-rcasps/organisation-name", "OrganisationName")
      case "individual"   => ("Individual", "/manage-your-rcasps/individual-name", "IndividualName")
    }

    http("Post Organisation or Individual Page")
      .post(baseUrl + "/manage-your-rcasps/organisation-or-individual")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", formValue)
      .check(status.is(303))
      .check(header("Location").is(locationPath).saveAs(saveAsName))
  }

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

  def postFindAddressPage(includePropertyNameOrNumber: Boolean): HttpRequestBuilder = {
    val baseRequest = http("Post Find Address Page")
      .post(baseUrl + "#{FindAddress}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("postcode", "LU1 5JP")

    val requestWithExtraParams =
      if (includePropertyNameOrNumber) baseRequest.formParam("propertyNameOrNumber", "7")
      else baseRequest

    val (locationPath, saveAsName) =
      if (includePropertyNameOrNumber) ("/manage-your-rcasps/review-address", "ReviewAddress")
      else ("/manage-your-rcasps/choose-address", "ChooseAddress")

    requestWithExtraParams
      .check(status.is(303))
      .check(header("Location").is(locationPath).saveAs(saveAsName))
  }

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

  val getIndividualNamePage: HttpRequestBuilder =
    http("Get Individual Name Page")
      .get(baseUrl + "#{IndividualName}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postIndividualNamePage: HttpRequestBuilder =
    http("Post Individual Name Page")
      .post(baseUrl + "#{IndividualName}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("firstName", "John")
      .formParam("lastName", "Doe")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/ni-number").saveAs("NiNumber"))

  val getNiNumberPage: HttpRequestBuilder =
    http("Get Ni Number Page")
      .get(baseUrl + "#{NiNumber}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postNiNumberPage: HttpRequestBuilder =
    http("Post Ni Number Page")
      .post(baseUrl + "#{NiNumber}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "AB123456C")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/find-address").saveAs("FindAddress"))

  val getChooseAddressPage: HttpRequestBuilder =
    http("Get Choose Address Page")
      .get(baseUrl + "#{ChooseAddress}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postChooseAddressPage: HttpRequestBuilder =
    http("Post Choose Address Page")
      .post(baseUrl + "#{ChooseAddress}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "none")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/address").saveAs("Address"))

  val getAddressPage: HttpRequestBuilder =
    http("Get Address Page")
      .get(baseUrl + "#{Address}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postAddressPage: HttpRequestBuilder =
    http("Post Address Page")
      .post(baseUrl + "#{Address}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("addressLine1", "1 Test Street")
      .formParam("townOrCity", "Test town")
      .formParam("postcode", "AA1 1AA")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/individual-email").saveAs("IndividualEmail"))

  val getIndividualEmailPage: HttpRequestBuilder =
    http("Get Individual Email Page")
      .get(baseUrl + "#{IndividualEmail}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postIndividualEmailPage: HttpRequestBuilder =
    http("Post Individual Email Page")
      .post(baseUrl + "#{IndividualEmail}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "John.doe@example.com")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/individual-have-phone").saveAs("IndividualHavePhone"))

  val getIndividualHavePhonePage: HttpRequestBuilder =
    http("Get Individual Have Phone Page")
      .get(baseUrl + "#{IndividualHavePhone}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postIndividualHavePhonePage: HttpRequestBuilder =
    http("Post Individual Have Phone Page")
      .post(baseUrl + "#{IndividualHavePhone}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/individual-phone").saveAs("IndividualPhone"))

  val getIndividualPhonePage: HttpRequestBuilder =
    http("Get Individual Phone Page")
      .get(baseUrl + "#{IndividualPhone}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postIndividualPhonePage: HttpRequestBuilder =
    http("Post Individual Phone Page")
      .post(baseUrl + "#{IndividualPhone}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "1234567890")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/end-of-journey").saveAs("EndOfJourney"))

  val getYourRcaspsPage: HttpRequestBuilder =
    http("Get Your Rcasps Page")
      .get(baseUrl + "/manage-your-rcasps/your-rcasps")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val getAmazonRemoveUserAccessPage: HttpRequestBuilder =
    http("Get Remove User Access Page")
      .get(baseUrl + "/manage-your-rcasps/remove/user-access/ZMCAR0123456788")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postAmazonRemoveUserAccessPage: HttpRequestBuilder =
    http("Post Remove User Access Page")
      .post(baseUrl + "/manage-your-rcasps/remove/user-access/ZMCAR0123456788")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "false")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/remove/other-access").saveAs("OtherAccess"))

  val getRemoveOtherAccessPage: HttpRequestBuilder =
    http("Get Remove Other Access Page")
      .get(baseUrl + "#{OtherAccess}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postRemoveOtherAccessPage: HttpRequestBuilder =
    http("Post Remove Other Access Page")
      .post(baseUrl + "#{OtherAccess}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "false")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/remove/remove-rcasp").saveAs("RemoveRCASP"))

  val getRemoveRCASPPage: HttpRequestBuilder =
    http("Get Remove RCASP Page")
      .get(baseUrl + "#{RemoveRCASP}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postRemoveRCASPPage: HttpRequestBuilder =
    http("Post Remove RCASP Page")
      .post(baseUrl + "#{RemoveRCASP}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/remove/rcasp-removed").saveAs("RCASPRemoved"))

  val getRCASPRemovedPage: HttpRequestBuilder =
    http("Get RCASP Removed Page")
      .get(baseUrl + "#{RCASPRemoved}")
      .check(status.is(200))

  val getRCASPIsUserChangePage: HttpRequestBuilder =
    http("Get RCASP Change Page")
      .get(baseUrl + "/manage-your-rcasps/change/ZMCAR0123456787")
      .check(status.is(303))

  val getAmazonChangePage: HttpRequestBuilder =
    http("Get RCASP Change Page")
      .get(baseUrl + "/manage-your-rcasps/change/ZMCAR0123456788")
      .check(status.is(303))

  val getRegisteredBusinessChangeAnswersPage: HttpRequestBuilder =
    http("Get Registered Business Change Answers Page")
      .get(baseUrl + "/manage-your-rcasps/registered-business/change-answers/ZMCAR0123456787")
      .check(status.is(200))

  val getAmazonChangeAnswersPage: HttpRequestBuilder =
    http("Get RCASP Change Answers Page")
      .get(baseUrl + "/manage-your-rcasps/change-answers/ZMCAR0123456788")
      .check(status.is(200))

  val getRegisteredBusinessChangeIsTheAddressCorrectPage: HttpRequestBuilder =
    http("Get Registered Business Change Is The Address Correct Page")
      .get(baseUrl + "/manage-your-rcasps/registered-business/change-is-the-address-correct")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postRegisteredBusinessChangeIsTheAddressCorrectPage: HttpRequestBuilder =
    http("Post Registered Business Change Is The Address Correct Page")
      .post(baseUrl + "/manage-your-rcasps/registered-business/change-is-the-address-correct")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "false")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/change-find-address").saveAs("ChangeFindAddress"))

  val getChangeFindAddressPage: HttpRequestBuilder =
    http("Get Change Find Address Page")
      .get(baseUrl + "#{ChangeFindAddress}")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  def postChangeFindAddressPage(includePropertyNameOrNumber: Boolean): HttpRequestBuilder = {
    val baseRequest = http("Post Change Find Address Page")
      .post(baseUrl + "#{ChangeFindAddress}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("postcode", "LU1 5JP")

    val requestWithExtraParams =
      if (includePropertyNameOrNumber) baseRequest.formParam("propertyNameOrNumber", "7")
      else baseRequest

    val (locationPath, saveAsName) =
      if (includePropertyNameOrNumber) ("/manage-your-rcasps/change-review-address", "ChangeReviewAddress")
      else ("/manage-your-rcasps/change-choose-address", "ChangeChooseAddress")

    requestWithExtraParams
      .check(status.is(303))
      .check(header("Location").is(locationPath).saveAs(saveAsName))
  }

  val getChangeReviewAddressPage: HttpRequestBuilder =
    http("Get Change Review Address Page")
      .get(baseUrl + "#{ChangeReviewAddress}")
      .check(status.is(200))

  val getChangeReviewAddressSubmitPage: HttpRequestBuilder =
    http("Get Change Review Address Submit Page")
      .get(baseUrl + "/manage-your-rcasps/change-review-address-submit")
      .check(status.is(303))

  val postRegisteredBusinessChangeAnswersPage: HttpRequestBuilder =
    http("Post Registered Business Change Answers Page")
      .post(baseUrl + "/registered-business/change-answers/ZMCAR0123456787")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/details-updated").saveAs("DetailsUpdated"))

  val getDetailsUpdatedPage: HttpRequestBuilder =
    http("Get Details Updated Page")
      .get(baseUrl + "/manage-your-rcasps/details-updated")
      .check(status.is(200))

  val getChangeHavePhonePage: HttpRequestBuilder =
    http("Get Change Have Phone Page")
      .get(baseUrl + "/manage-your-rcasps/change-have-phone")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postChangeHavePhonePage: HttpRequestBuilder =
    http("Post Change Have Phone Page")
      .post(baseUrl + "/manage-your-rcasps/change-have-phone")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/change-phone").saveAs("ChangePhone"))

  val getChangePhonePage: HttpRequestBuilder =
    http("Get Change Phone Page")
      .get(baseUrl + "/manage-your-rcasps/change-phone")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postChangePhonePage: HttpRequestBuilder =
    http("Post Change Phone Page")
      .post(baseUrl + "/manage-your-rcasps/change-phone")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "1234567890")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/end-of-journey").saveAs("EndOfJourney"))

  val getChangeHaveSecondContactPage: HttpRequestBuilder =
    http("Get Change Have Second Contact Page")
      .get(baseUrl + "/manage-your-rcasps/change-have-second-contact")
      .check(status.is(200))
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))

  val postChangeHaveSecondContactPage: HttpRequestBuilder =
    http("Post Change Have Second Contact Page")
      .post(baseUrl + "/manage-your-rcasps/change-have-second-contact")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/second-contact-name").saveAs("SecondContactName"))
}

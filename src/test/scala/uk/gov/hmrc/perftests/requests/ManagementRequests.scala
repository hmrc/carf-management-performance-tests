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

  val postAuthLoginPage: HttpRequestBuilder =
    http("Post Auth login page for Non Auto matched Org")
      .post(baseUrlAuth + "/auth-login-stub/gg-sign-in")
      .formParam("authorityId", "")
      .formParam("credentialStrength", "strong")
      .formParam("excludeGnapToken", "false")
      .formParam("confidenceLevel", "50")
      .formParam("credentialRole", "User")
      .formParam("additionalInfo.emailVerified", "N/A")
      .formParam("email", "user@test.com")
      .formParam("affinityGroup", "Organisation")
      .formParam("redirectionUrl", baseUrl + route)
      .formParam("enrolment[0].name", "HMRC-CARF-ORG")
      .formParam("enrolment[0].taxIdentifier[0].name", "CARFID")
      .formParam("enrolment[0].taxIdentifier[0].value", "RR1111")
      .formParam("enrolment[0].state", "Activated")
      .check(status.is(303))
      .check(header("Location").is(baseUrl + route).saveAs("AuthLoginForCarfManagement"))

  val getManagementDashboardPage: HttpRequestBuilder =
    http("Get Management Dashboard Page")
      .get(baseUrl + route)
      .check(status.is(200))

  val getManageYourRcaspsPage: HttpRequestBuilder =
    http("Get Manage your Rcasps Page")
      .get(baseUrl + "/manage-your-rcasps")
      .check(status.is(303))
      .check(header("Location").is("/manage-your-rcasps/organisation-or-individual").saveAs("OrgOrInd"))

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

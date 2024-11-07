/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class OrganizationDiscoveryConfigRestClient extends RestBaseClient {

    private static final String ORGANIZATION_DISCOVERY_CONFIG_API_BASE_PATH =
            "/api/server/v1/organization-configs/discovery";
    private final String username;
    private final String password;
    private final String endpointUrl;

    public OrganizationDiscoveryConfigRestClient(String backendURL, Tenant tenantInfo) {

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.endpointUrl = backendURL + ISIntegrationTest.getTenantedRelativePath(
                ORGANIZATION_DISCOVERY_CONFIG_API_BASE_PATH, tenantDomain);
    }

    public void addOrganizationDiscoveryConfig(String requestBody) {

        String jsonRequestBody = toJSONString(requestBody);
        try (CloseableHttpResponse httpResponse = getResponseOfHttpPost(endpointUrl, jsonRequestBody, getHeaders())) {
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Failed to add organization discovery config.");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while adding organization discovery config.", e);
        }
    }

    public void deleteOrganizationDiscoveryConfig() {

        try (CloseableHttpResponse httpResponse = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Failed to delete organization discovery config.");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while deleting organization discovery config.", e);
        }
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }

    private Header[] getHeaders() {

        return new Header[] {
                new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON)),
                new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE  +
                        Base64.encodeBase64String((username + ":" + password).getBytes()).trim())
        };
    }
}

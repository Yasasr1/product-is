package org.wso2.identity.integration.test.organizationDiscovery;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.OrganizationDiscoveryConfigRestClient;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

public class OrganizationDiscoveryTestCase extends OIDCAbstractIntegrationTest {

    private static final String EMAIL_AS_USERNAME_TOML = "email_as_username.toml";
    private static final String LOCAL_CLAIM_DIALECT = "local";
    private static final String USERNAME_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ";
    private static final String EMAIL_AS_USERNAME_CLAIM_JSON = "email_as_username_request.json";
    private static final String REVERT_EMAIL_AS_USERNAME_CLAIM_JSON = "revert_email_as_username_request.json";
    private static final String ENABLE_EMAIL_DOMAIN_ORG_DISCOVERY_JSON = "enable_email_domain_org_discovery.json";
    private ServerConfigurationManager serverConfigurationManager;
    private ClaimManagementRestClient claimManagementRestClient;
    private OrganizationDiscoveryConfigRestClient organizationDiscoveryConfigRestClient;
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        OIDCApplication oidcApplication = new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName,
                OIDCUtilTest.playgroundAppOneAppContext,
                OIDCUtilTest.playgroundAppOneAppCallBackUri);

        createApplication(oidcApplication);
        // Apply toml configuration to set email as username.
        applyEmailAsUsernameConfig();
        // Init again after restart.
        super.init();

        // Update username claim
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        String emailAsUsernameRequestBody = RESTTestBase.readResource(EMAIL_AS_USERNAME_CLAIM_JSON, this.getClass());
        claimManagementRestClient.updateClaim(LOCAL_CLAIM_DIALECT, USERNAME_CLAIM_ID, emailAsUsernameRequestBody);

        // Enable email domain based org discovery for self-registration
        organizationDiscoveryConfigRestClient = new OrganizationDiscoveryConfigRestClient(serverURL, tenantInfo);
        String orgDiscoveryConfigRequestBody = RESTTestBase.readResource(
                ENABLE_EMAIL_DOMAIN_ORG_DISCOVERY_JSON, this.getClass());
        organizationDiscoveryConfigRestClient.addOrganizationDiscoveryConfig(orgDiscoveryConfigRequestBody);

    }
    
    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        
        serverConfigurationManager.restoreToLastConfiguration(false);
        String revertEmailAsUsernameClaimRequestBody =
                RESTTestBase.readResource(REVERT_EMAIL_AS_USERNAME_CLAIM_JSON, this.getClass());
        claimManagementRestClient.updateClaim(
                LOCAL_CLAIM_DIALECT, USERNAME_CLAIM_ID, revertEmailAsUsernameClaimRequestBody);
        organizationDiscoveryConfigRestClient.deleteOrganizationDiscoveryConfig();
        organizationDiscoveryConfigRestClient.closeHttpClient();
        claimManagementRestClient.closeHttpClient();
    }

    private void applyEmailAsUsernameConfig() throws Exception {
        
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File emailAsUsernameConfigFile = new File(getISResourceLocation() + File.separator + 
                "organizationDiscovery" + File.separator + EMAIL_AS_USERNAME_TOML);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailAsUsernameConfigFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();
        
    }
    

}

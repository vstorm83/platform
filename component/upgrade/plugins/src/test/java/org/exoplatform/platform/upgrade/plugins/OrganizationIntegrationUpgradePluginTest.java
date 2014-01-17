package org.exoplatform.platform.upgrade.plugins;

import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.commons.upgrade.UpgradeProductService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.platform.organization.integration.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.*;
import java.io.InputStream;

/**
 * Created by menzli on 17/01/14.
 */
public class OrganizationIntegrationUpgradePluginTest extends BaseCommonsTestCase {

    private static final Log LOG = ExoLogger.getLogger(OrganizationIntegrationUpgradePluginTest.class);

    private static final String ORGANIZATION_INITIALIZATIONS = "OrganizationIntegrationService";

    private static final String OLD_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/product_old2.properties";

    private static final String COLLABORATION_WS = "collaboration";

    protected ProductInformations productInformations;

    @Override
    public void setUp() throws Exception {

        super.setUp();

        productInformations = getService(ProductInformations.class);

        // replace PLF version by an old one in the jcr.
        Session session = null;
        try {
            InputStream oldVersionsContentIS = configurationManager.getInputStream(OLD_PRODUCT_INFORMATIONS_FILE);
            byte[] binaries = new byte[oldVersionsContentIS.available()];
            oldVersionsContentIS.read(binaries);
            String oldVersionsContent = new String(binaries);

            session = repositoryService.getCurrentRepository().getSystemSession(COLLABORATION_WS);

            Node plfVersionDeclarationNode = getProductVersionNode(session);
            Node plfVersionDeclarationContentNode = plfVersionDeclarationNode.getNode("jcr:content");
            plfVersionDeclarationContentNode.setProperty("jcr:data", oldVersionsContent);

            session.save();
            session.refresh(true);

        } catch (Exception E) {
            LOG.error("##### Cannot create Node OrganizationIntegrationService",E);

        } finally {
            if (session != null) {
                session.logout();
            }
        }
        importOrgIntegData();
    }
    public void testUpgrade() throws Exception {
        // replace PLF version by an old one in the jcr.
        Session session = null;
        try {
            session = repositoryService.getCurrentRepository().getSystemSession(COLLABORATION_WS);
            {
                assertTrue(session.itemExists(Util.HOME_PATH));
                Node homeNode = (Node) session.getItem(Util.HOME_PATH);
                assertTrue(homeNode.hasNode(Util.ORGANIZATION_INITIALIZATIONS));
                Node orgIntegrationParentNode = homeNode.getNode(Util.ORGANIZATION_INITIALIZATIONS);

                assertTrue(orgIntegrationParentNode.hasNode(Util.USERS_FOLDER));
                Node usersNode = orgIntegrationParentNode.getNode(Util.USERS_FOLDER);
                assertTrue(usersNode.hasNode(Util.MEMBERSHIPS_LIST_NODE_NAME));

                assertTrue(orgIntegrationParentNode.hasNode(Util.GROUPS_FOLDER));
                Node groupsNode = orgIntegrationParentNode.getNode(Util.GROUPS_FOLDER);
                assertTrue(groupsNode.hasNode(Util.MEMBERSHIPS_LIST_NODE_NAME));

                assertTrue(orgIntegrationParentNode.hasNode(Util.PROFILES_FOLDER));

                assertTrue(orgIntegrationParentNode.hasNode(Util.MEMBERSHIPS_FOLDER));
            }
            {
                assertTrue(session.itemExists(Util.HOME_PATH));
                Node homeNode = (Node) session.getItem(Util.HOME_PATH);
                LOG.info("======================================================================");
                LOG.info("==============> "+homeNode.getPath());
                LOG.info("======================================================================");
                //assertFalse(homeNode.hasNode(ORGANIZATION_INITIALIZATIONS));
            }
            session.save();
            session.refresh(true);
        } catch (Exception E) {
            LOG.error("",E);
        }
        finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    private void importOrgIntegData() throws Exception {
        Session session = null;
        try {
            session = repositoryService.getCurrentRepository().getSystemSession(COLLABORATION_WS);
            InputStream inputStream = configurationManager.getInputStream("classpath:/conf/data/OrgIntegData.xml");
            session.importXML("/", inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            session.save();

            configurationManager.addConfiguration("classpath:/conf/organization-integration-configuration.xml");
            InitParams params = configurationManager.getComponent(UpgradeOrganizationIntegrationDataPlugin.class).getInitParams();
            System.out.println ("======================> "+params.size());

            UpgradeOrganizationIntegrationDataPlugin organizationIntegrationDataPlugin = container.createComponent(UpgradeOrganizationIntegrationDataPlugin.class, params);
            organizationIntegrationDataPlugin.setName("Upgrade-OrganizationItegration");

            UpgradeProductService upgradeProductService = getService(UpgradeProductService.class);
            upgradeProductService.addUpgradePlugin(organizationIntegrationDataPlugin);

        } catch (Exception E) {
            LOG.error("", E);
        }finally {
            if (session != null) {
                session.logout();
            }
        }

        // Upgrade the OrganizationIntegrationService data
        //UpgradeProductService upgradeService = getService(UpgradeProductService.class);
        //productInformations.start();
        //upgradeService.start();
    }

    private Node getProductVersionNode(Session session) throws PathNotFoundException, RepositoryException {
        Node plfVersionDeclarationNodeContent = ((Node) session.getItem("/Application Data/"
                + ProductInformations.UPGRADE_PRODUCT_SERVICE_NODE_NAME                + "/"
                + ProductInformations.PRODUCT_VERSION_DECLARATION_NODE_NAME));
        return plfVersionDeclarationNodeContent;
    }
}

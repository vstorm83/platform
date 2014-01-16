package org.exoplatform.platform.component.organization.test;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.platform.organization.integration.*;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.impl.MembershipImpl;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by menzli on 16/01/14.
 */
public class OrganizationIntegrationTest extends BaseCommonsTestCase {

    private static final Log LOG = ExoLogger.getLogger(OrganizationIntegrationTest.class);
    OrganizationService organizationService = null;
    DataDistributionManager dataDistributionManager = null;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        organizationService = getService(OrganizationService.class);
        dataDistributionManager = getService(DataDistributionManager.class);

    }

    public void testIntegrationService() throws Exception {
        verifyFoldersCreation(false);
        //--- Init OrganizationIntegrationService
        OrganizationIntegrationService organizationIntegrationService = container.createComponent(OrganizationIntegrationService.class);
        container.registerComponentInstance(organizationIntegrationService);

        //--- Init organizationListener
        NewUserListener userListener = container.createComponent(NewUserListener.class);
        organizationIntegrationService.addListenerPlugin(userListener);
        NewProfileListener profileListener = container.createComponent(NewProfileListener.class);
        organizationIntegrationService.addListenerPlugin(profileListener);
        NewMembershipListener membershipListener = container.createComponent(NewMembershipListener.class);
        organizationIntegrationService.addListenerPlugin(membershipListener);
        NewGroupListener groupListener = container.createComponent(NewGroupListener.class);
        organizationIntegrationService.addListenerPlugin(groupListener);

        Session session = null;

        try {

            session = repositoryService.getCurrentRepository().getSystemSession(Util.WORKSPACE);

            //--- Membership synchronization
            //--- User "demo" not yet synchronized
            verifyMembershipFoldersCreation("demo", "/platform/guests", "member", false);
            //--- Use OrgIntegServ to synchronize USer "demo"
            organizationIntegrationService.syncMembership("demo", "/platform/guests",EventType.ADDED.toString());
            //--- Check if User "demo" is correctly synchronized
            verifyMembershipFoldersCreation("demo", "/platform/guests", "member", true);

            //--- Groups synchronization
            //--- Synchronize the group above "ADD"
            organizationIntegrationService.syncGroup("/organization/management/executive-board", EventType.ADDED.toString());
            //--- Synchronize the group above "DELETE"
            organizationIntegrationService.syncGroup("/organization/management/executive-board", EventType.DELETED.toString());

            assertTrue(Util.hasGroupFolder(dataDistributionManager, session, "/organization"));
            assertTrue(Util.hasGroupFolder(dataDistributionManager, session, "/organization/management"));
            assertTrue(Util.hasGroupFolder(dataDistributionManager, session, "/organization/management/executive-board"));


            //--- Synchronize all groups "DELETE"
            organizationIntegrationService.syncAllGroups(EventType.DELETED.toString());

            assertTrue(Util.hasGroupFolder(dataDistributionManager, session, "/organization"));
            assertTrue(Util.hasGroupFolder(dataDistributionManager, session, "/organization/management"));
            assertTrue(Util.hasGroupFolder(dataDistributionManager, session, "/organization/management/executive-board"));

            //--- User synchronization
            //--- User "root" not yet synchronized
            verifyUserFoldersCreation("root", false);

            //--- Use OrgIntegServ to synchronize USer "root"
            organizationIntegrationService.syncUser("root", EventType.ADDED.toString());

            //--- Get activated membership of User "root"
            List<Membership> rootUserMemberships = Util.getActivatedMembershipsRelatedToUser(dataDistributionManager, session, "root");
            assertNotNull(rootUserMemberships);
            assertEquals(rootUserMemberships.size(), 3);

            //--- User "root" Synchronization folder should be created
            verifyUserFoldersCreation("root", true);

            //--- Synchronize User "root" : "DELETE"
            organizationIntegrationService.syncUser("root", EventType.DELETED.toString());

            //---
            verifyUserFoldersCreation("root", true);

            deleteMembership("member:root:/organization/management/executive-board");
            verifyMembershipFoldersCreation("root", "/organization/management/executive-board", "member", true);
            organizationIntegrationService.syncMembership("root", "/organization/management/executive-board",
                    EventType.DELETED.toString());
            verifyMembershipFoldersCreation("root", "/organization/management/executive-board", "member", false);
            rootUserMemberships = Util.getActivatedMembershipsRelatedToUser(dataDistributionManager, session, "root");
            assertNotNull(rootUserMemberships);
            assertEquals(rootUserMemberships.size(), 2);
            deleteUser("root");
            organizationIntegrationService.syncUser("root", EventType.DELETED.toString());

            verifyUserFoldersCreation("root", false);

            deleteGroup("/organization");
            organizationIntegrationService.syncGroup("/organization", EventType.DELETED.toString());

            assertFalse(Util.hasGroupFolder(dataDistributionManager, session, "/organization"));
            assertFalse(Util.hasGroupFolder(dataDistributionManager, session, "/organization/management"));
            assertFalse(Util.hasGroupFolder(dataDistributionManager, session, "/organization/management/executive-board"));

            MembershipImpl membership = new MembershipImpl();
            {
                membership.setMembershipType("manager");
                membership.setUserName("john");
                membership.setGroupId("/organization/management/executive-board");
                membership.setId(Util.computeMembershipId(membership));

                assertFalse(Util.hasMembershipFolder(dataDistributionManager, session, membership));
            }
            deleteUser("mary");
            deleteGroup("/platform/guests");
            organizationIntegrationService.syncAll();
            verifyFoldersCreation(true);

            deleteAllGroups();
            deleteAllUsers();

            organizationIntegrationService.syncAll();

            assertEquals(Util.getActivatedGroups(session).size(), 0);
            assertEquals(Util.getActivatedUsers(session).size(), 0);

        } catch (Exception E) {

        } finally {

        }


    }

    private void verifyFoldersCreation(boolean creationAssertionValue) throws Exception {
        Session session = null;
        try {
            session = repositoryService.getCurrentRepository().getSystemSession(Util.WORKSPACE);

            if (organizationService instanceof ComponentRequestLifecycle) {
                ((ComponentRequestLifecycle) organizationService).startRequest(container);
            }

            ListAccess<User> usersListAccess = organizationService.getUserHandler().findAllUsers();

            int i = 0;

            while (i <= usersListAccess.getSize()) {
                int length = i + 10 <= usersListAccess.getSize() ? 10 : usersListAccess.getSize() - i;
                User[] users = usersListAccess.load(i, length);
                for (User user : users) {
                    assertEquals(creationAssertionValue, Util.hasUserFolder(dataDistributionManager, session, user.getUserName()));
                    UserProfile profile = organizationService.getUserProfileHandler().findUserProfileByName(user.getUserName());
                    assertEquals(creationAssertionValue, Util.hasProfileFolder(dataDistributionManager,session, profile.getUserName()));
                    Collection<?> memberships = organizationService.getMembershipHandler().findMembershipsByUser(user.getUserName());
                    for (Object objectMembership : memberships) {
                        assertEquals(creationAssertionValue, Util.hasMembershipFolder(dataDistributionManager, session, (Membership) objectMembership));
                    }
                }
                i += 10;
            }
            List<Group> groups = new ArrayList<Group>(organizationService.getGroupHandler().getAllGroups());

            Collections.sort(groups, OrganizationIntegrationService.GROUP_COMPARATOR);
            for (Group group : groups) {
                assertEquals(creationAssertionValue, Util.hasGroupFolder(dataDistributionManager, session, group.getId()));
            }
            if (organizationService instanceof ComponentRequestLifecycle) {
                ((ComponentRequestLifecycle) organizationService).endRequest(container);
            }


        } catch (Exception E) {
            LOG.error("######### Cannot create user folder for synchronization ",E);

        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }
    private void verifyMembershipFoldersCreation(String username, String groupId, String membershipType, boolean assertionCondition)
            throws Exception {
        Session session = null;
        try {
            session = repositoryService.getCurrentRepository().getSystemSession(Util.WORKSPACE);
            MembershipImpl membership = new MembershipImpl();
            {
                membership.setMembershipType(membershipType);
                membership.setUserName(username);
                membership.setGroupId(groupId);
                membership.setId(Util.computeMembershipId(membership));
            }
            assertEquals(assertionCondition, Util.hasMembershipFolder(dataDistributionManager, session, membership));
            session.save();
        } catch (Exception E) {
            E.printStackTrace();

        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    private void verifyUserFoldersCreation(String username, boolean creationAssertionValue) throws Exception {
        Session session = null;
        try {
            session = repositoryService.getCurrentRepository().getSystemSession(Util.WORKSPACE);
            if (organizationService instanceof ComponentRequestLifecycle) {
                ((ComponentRequestLifecycle) organizationService).startRequest(container);
            }
            assertEquals(creationAssertionValue, Util.hasUserFolder(dataDistributionManager, session, username));
            assertEquals(creationAssertionValue, Util.hasProfileFolder(dataDistributionManager, session, username));

            Collection<?> memberships = organizationService.getMembershipHandler().findMembershipsByUser(username);
            if (creationAssertionValue) {// Related groups has to be
                // integrated/added, but when deleting
                // user, the group could still exists

                for (Object objectMembership : memberships) {
                    assertEquals(creationAssertionValue, Util.hasMembershipFolder(dataDistributionManager, session, (Membership) objectMembership));
                }

                @SuppressWarnings("unchecked")
                List<Group> groups = new ArrayList<Group>(organizationService.getGroupHandler().findGroupsOfUser(username));
                Collections.sort(groups, OrganizationIntegrationService.GROUP_COMPARATOR);
                for (Group group : groups) {
                    assertEquals(creationAssertionValue, Util.hasGroupFolder(dataDistributionManager, session, group.getId()));
                }
            } else {
                for (Object objectMembership : memberships) {
                    assertEquals(creationAssertionValue, Util.hasMembershipFolder(dataDistributionManager, session, (Membership) objectMembership));
                }
            }
            if (organizationService instanceof ComponentRequestLifecycle) {
                ((ComponentRequestLifecycle) organizationService).endRequest(container);
            }
        } catch (Exception E) {

        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }
    private void deleteMembership(String membershipId) throws Exception {
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).startRequest(container);
        }
        organizationService.getMembershipHandler().removeMembership(membershipId, true);
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).endRequest(container);
        }
    }

    private void deleteUser(String username) throws Exception {
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).startRequest(container);
        }
        organizationService.getUserHandler().removeUser(username, true);
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).endRequest(container);
        }
    }

    private void deleteGroup(String groupId) throws Exception {
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).startRequest(container);
        }
        organizationService.getGroupHandler().removeGroup(organizationService.getGroupHandler().findGroupById(groupId), true);
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).endRequest(container);
        }
    }

    private void deleteAllUsers() throws Exception {
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).startRequest(container);
        }

        ListAccess<User> usersListAccess = organizationService.getUserHandler().findAllUsers();
        int i = 0;
        while (i <= usersListAccess.getSize()) {
            int length = i + 10 <= usersListAccess.getSize() ? 10 : usersListAccess.getSize() - i;
            User[] users = usersListAccess.load(i, length);
            for (User user : users) {
                organizationService.getUserHandler().removeUser(user.getUserName(), true);
            }
            i += 10;
        }
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).endRequest(container);
        }
    }
    private void deleteAllGroups() throws Exception {
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).startRequest(container);
        }
        @SuppressWarnings("unchecked")
        List<Group> groups = new ArrayList<Group>(organizationService.getGroupHandler().getAllGroups());
        Collections.sort(groups, OrganizationIntegrationService.GROUP_COMPARATOR);
        Collections.reverse(groups);

        for (Group group : groups) {
            organizationService.getGroupHandler().removeGroup(group, true);
        }
        if (organizationService instanceof ComponentRequestLifecycle) {
            ((ComponentRequestLifecycle) organizationService).endRequest(container);
        }
    }
}

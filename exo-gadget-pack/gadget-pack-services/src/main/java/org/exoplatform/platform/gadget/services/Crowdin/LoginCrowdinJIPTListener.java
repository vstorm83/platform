package org.exoplatform.platform.gadget.services.Crowdin;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.Constants;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;



public class LoginCrowdinJIPTListener extends Listener<ConversationRegistry, ConversationState> {
	private static final Log LOG = ExoLogger.getLogger(LoginCrowdinJIPTListener.class);

	/**
	 * Log the time when user logging in 
	 * 
	 * @throws Exception
	 */	
	@Override
	public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
		String userId = event.getData().getIdentity().getUserId();
		try {		
			
			/**
			 * JIPT feature sets default pseudo-language to System properties 
			 */
			OrganizationService svc = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
            // Don't rely on UserProfileLifecycle loaded UserProfile when doing
            // an update to avoid a potential overwrite of other changes
            UserProfile userProfile = svc.getUserProfileHandler().findUserProfileByName(userId);
            if (userProfile != null && userProfile.getUserInfoMap() != null) {
                // Only save if user's locale has not been set
                String currLocale = userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
                if (currLocale == null || currLocale.trim().equals("") || !currLocale.equals(System.getProperty("crowdin.jipt"))) {
                	//set pseudo-language from -Dpseudo.jipt
                    userProfile.getUserInfoMap().put(Constants.USER_LANGUAGE,System.getProperty("crowdin.jipt"));
                    svc.getUserProfileHandler().saveUserProfile(userProfile, false);
            		LOG.info("Set Crowdin JIPT user to "+System.getProperty("crowdin.jipt"));
                }
            }
			
		} catch (Exception e) {
			LOG.debug("Error while logging the login of user '" + userId + "': " + e.getMessage(), e);
		}
	}
}


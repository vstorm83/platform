package org.exoplatform.platform.component;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * @author <a href="rtouzi@exoplatform.com">rtouzi</a>
 * @date 01/11/12
 */
@ComponentConfig(
        lifecycle = UIApplicationLifecycle.class,
        template = "app:/groovy/platformNavigation/portlet/UINotificationPopoverToolbarPortlet/UINotificationPopoverToolbarPortlet.gtmpl"
)
public class UINotificationPopoverToolbarPortlet extends UIPortletApplication {

    private static final Log LOG = ExoLogger.getLogger(UINotificationPopoverToolbarPortlet.class);

    public UINotificationPopoverToolbarPortlet() throws Exception {

    }


}

/**
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.platform.common.service;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.PageNode;

/**
 * @author <a href="hzekri@exoplatform.com">hzekri</a>
 * @date 18/07/13
 */

public class MenuConfiguratorPlugin extends BaseComponentPlugin {

    private String navPath;
    private PageNode targetNav;
    private String isChild;


    public MenuConfiguratorPlugin(InitParams initParams) {
        if(initParams.containsKey("extended.setup.navigation.file"))
        navPath = initParams.getValueParam("extended.setup.navigation.file").getValue();
        if(initParams.containsKey("target.node.config"))
        targetNav = (PageNode) initParams.getObjectParam("target.node.config").getObject();
        if(initParams.containsKey("isChild"))
        isChild = initParams.getValueParam("isChild").getValue();
    }


    public String getNavPath()
    {
        return navPath;
    }

    public PageNode getTargetNav()
    {
        return targetNav;
    }

    public String getIsChild()
    {
        return isChild;
    }
}

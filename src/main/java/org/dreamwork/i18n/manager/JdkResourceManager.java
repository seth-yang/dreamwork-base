package org.dreamwork.i18n.manager;

import org.dreamwork.i18n.AbstractResourceManager;
import org.dreamwork.i18n.IResourceAdapter;
import org.dreamwork.i18n.IResourceManager;
import org.dreamwork.i18n.adapters.JdkResourceAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-19
 * Time: 下午5:17
 */
public class JdkResourceManager extends AbstractResourceManager {

    @Override
    protected IResourceAdapter createResourceAdapter (String baseName) {
        return new JdkResourceAdapter ();
    }
}

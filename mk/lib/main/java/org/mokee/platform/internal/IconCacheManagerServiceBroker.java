/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2016 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mokee.platform.internal;

import android.annotation.NonNull;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;

import mokee.app.MKContextConstants;

import org.mokee.internal.themes.IIconCacheManager;
import org.mokee.platform.internal.common.BrokeredServiceConnection;

/**
 * Icon cache service broker for connecting clients to a backing icon cache manager service.
 *
 * @hide
 */
public class IconCacheManagerServiceBroker extends BrokerableMKSystemService<IIconCacheManager> {

    private static final ComponentName SERVICE_COMPONENT =
            new ComponentName("org.mokee.themeservice",
                    "org.mokee.themeservice.IconCacheManagerService");

    private final IIconCacheManager mServiceStubForFailure = new IIconCacheManager.Stub() {
        @Override
        public boolean cacheComposedIcon(Bitmap icon, String path) throws RemoteException {
            return false;
        }
    };

    private BrokeredServiceConnection mServiceConnection = new BrokeredServiceConnection() {
        @Override
        public void onBrokeredServiceConnected() {
        }

        @Override
        public void onBrokeredServiceDisconnected() {
        }
    };

    private final class BinderService extends IIconCacheManager.Stub {
        @Override
        public boolean cacheComposedIcon(Bitmap icon, String path) throws RemoteException {
            return getBrokeredService().cacheComposedIcon(icon, path);
        }
    }

    public IconCacheManagerServiceBroker(Context context) {
        super(context);
        setBrokeredServiceConnection(mServiceConnection);
    }

    @Override
    public String getFeatureDeclaration() {
        return MKContextConstants.Features.THEMES;
    }

    @Override
    protected IIconCacheManager getIBinderAsIInterface(@NonNull IBinder service) {
        return IIconCacheManager.Stub.asInterface(service);
    }

    @Override
    protected IIconCacheManager getDefaultImplementation() {
        return mServiceStubForFailure;
    }

    @Override
    protected ComponentName getServiceComponent() {
        return SERVICE_COMPONENT;
    }

    @Override
    public void onStart() {
        publishBinderService(MKContextConstants.MK_ICON_CACHE_SERVICE, new BinderService());
    }

    @Override
    protected String getComponentFilteringPermission() {
        return mokee.platform.Manifest.permission.ACCESS_THEME_MANAGER;
    }
}

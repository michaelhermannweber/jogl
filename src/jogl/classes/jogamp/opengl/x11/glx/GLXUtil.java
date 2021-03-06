/*
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */

package jogamp.opengl.x11.glx;

import javax.media.opengl.GLException;

import jogamp.opengl.Debug;

import com.jogamp.common.util.VersionNumber;
import com.jogamp.nativewindow.x11.X11GraphicsDevice;

public class GLXUtil {
    public static final boolean DEBUG = Debug.debug("GLXUtil");
    
    public static synchronized boolean isGLXAvailableOnServer(X11GraphicsDevice x11Device) {
        if(null == x11Device) {
            throw new IllegalArgumentException("null X11GraphicsDevice");
        }
        if(0 == x11Device.getHandle()) {
            throw new IllegalArgumentException("null X11GraphicsDevice display handle");
        }        
        boolean glXAvailable = false;
        x11Device.lock();
        try {
            glXAvailable = GLX.glXQueryExtension(x11Device.getHandle(), null, 0, null, 0);
        } catch (Throwable t) { /* n/a */
        } finally {
            x11Device.unlock();
        }        
        return glXAvailable;        
    }
    
    public static String getGLXClientString(X11GraphicsDevice x11Device, int name) {
        x11Device.lock();
        try {
            return GLX.glXGetClientString(x11Device.getHandle(), name);
        } finally {
            x11Device.unlock();
        }        
    }
    public static String queryGLXServerString(X11GraphicsDevice x11Device, int screen_idx, int name) {
        x11Device.lock();
        try {
            return GLX.glXQueryServerString(x11Device.getHandle(), screen_idx, name);
        } finally {
            x11Device.unlock();
        }        
    }    
    public static String queryGLXExtensionsString(X11GraphicsDevice x11Device, int screen_idx) {
        x11Device.lock();
        try {
            return GLX.glXQueryExtensionsString(x11Device.getHandle(), screen_idx);
        } finally {
            x11Device.unlock();
        }        
    }
    
    public static VersionNumber getGLXServerVersionNumber(X11GraphicsDevice x11Device) {
        int[] major = new int[1];
        int[] minor = new int[1];
        
        x11Device.lock();
        try {
            if (!GLX.glXQueryVersion(x11Device.getHandle(), major, 0, minor, 0)) {
              throw new GLException("glXQueryVersion failed");
            }
    
            // Work around bugs in ATI's Linux drivers where they report they
            // only implement GLX version 1.2 on the server side
            if (major[0] == 1 && minor[0] == 2) {
              String str = GLX.glXGetClientString(x11Device.getHandle(), GLX.GLX_VERSION);
              try {
                  // e.g. "1.3"
                  major[0] = Integer.valueOf(str.substring(0, 1)).intValue();
                  minor[0] = Integer.valueOf(str.substring(2, 3)).intValue();
              } catch (Exception e) {
                  major[0] = 1;
                  minor[0] = 2;
              }
            }
        } finally {
            x11Device.unlock();
        }
        return new VersionNumber(major[0], minor[0], 0);
    }
    
    public static boolean isMultisampleAvailable(String extensions) {
        if (extensions != null) {
            return (extensions.indexOf("GLX_ARB_multisample") >= 0);
        }
        return false;
    }

    public static boolean isVendorNVIDIA(String vendor) {
        return vendor != null && vendor.startsWith("NVIDIA") ;
    }

    public static boolean isVendorATI(String vendor) {
        return vendor != null && vendor.startsWith("ATI") ;
    }

    public static boolean isClientMultisampleAvailable() {
        return clientMultisampleAvailable;
    }
    public static String getClientVendorName() {
        return clientVendorName;
    }
    public static VersionNumber getClientVersionNumber() {
        return clientVersionNumber;
    }
    
    public static synchronized void initGLXClientDataSingleton(X11GraphicsDevice x11Device) { 
        if(null != clientVendorName) {
            return; // already initialized
        }
        if(null == x11Device) {
            throw new IllegalArgumentException("null X11GraphicsDevice");
        }
        if(0 == x11Device.getHandle()) {
            throw new IllegalArgumentException("null X11GraphicsDevice display handle");
        }        
        clientMultisampleAvailable = isMultisampleAvailable(GLX.glXGetClientString(x11Device.getHandle(), GLX.GLX_EXTENSIONS));
        clientVendorName = GLX.glXGetClientString(x11Device.getHandle(), GLX.GLX_VENDOR);
        
        int[] major = new int[1];
        int[] minor = new int[1];
        final String str = GLX.glXGetClientString(x11Device.getHandle(), GLX.GLX_VERSION);
        try {
              // e.g. "1.3"
              major[0] = Integer.valueOf(str.substring(0, 1)).intValue();
              minor[0] = Integer.valueOf(str.substring(2, 3)).intValue();
        } catch (Exception e) {
              major[0] = 1;
              minor[0] = 2;
        }
        clientVersionNumber = new VersionNumber(major[0], minor[0], 0);
    }
    private static boolean clientMultisampleAvailable = false;
    private static String clientVendorName = null;
    private static VersionNumber clientVersionNumber = null;
}

/*
 * Copyright (c) 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.media.nativewindow;

import java.lang.reflect.*;
import java.util.*;

import com.sun.nativewindow.impl.*;

/**
 * Provides the mechanism by which the graphics configuration for a
 * given window can be chosen before the window is created. On some
 * window systems (X11 in particular) the graphics configuration
 * decides parameters related to hardware accelerated rendering such
 * as the OpenGL pixel format. On these platforms it is necessary to
 * choose the graphics configuration early. Note that the selection of
 * the graphics configuration is an algorithm which does not have
 * strong dependencies on the particular Java window toolkit in use
 * (e.g., AWT) and therefore it is strongly desirable to factor this
 * functionality out of the core {@link NativeWindowFactory} so that
 * new window toolkits can replace just the {@link
 * NativeWindowFactory} and reuse the graphics configuration selection
 * algorithm provided by, for example, an OpenGL binding.
 */

public abstract class GraphicsConfigurationFactory {
    private static Map/*<Class, NativeWindowFactory>*/ registeredFactories =
        Collections.synchronizedMap(new HashMap());
    private static Class abstractGraphicsDeviceClass;

    static {
        initialize();
    }

    /** Creates a new NativeWindowFactory instance. End users do not
        need to call this method. */
    protected GraphicsConfigurationFactory() {
    }

    private static void initialize() {
        String osName = System.getProperty("os.name");
        String osNameLowerCase = osName.toLowerCase();
        String factoryClassName = null;

        abstractGraphicsDeviceClass = javax.media.nativewindow.AbstractGraphicsDevice.class;
        
        if (!osNameLowerCase.startsWith("wind") &&
            !osNameLowerCase.startsWith("mac os x")) {
            // Assume X11 platform -- should probably test for these explicitly
            try {
                GraphicsConfigurationFactory factory = (GraphicsConfigurationFactory)
                    NWReflection.createInstance("com.sun.nativewindow.impl.x11.X11GraphicsConfigurationFactory", new Object[] {});
                registerFactory(javax.media.nativewindow.x11.X11GraphicsDevice.class, factory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Register the default no-op factory for arbitrary
        // AbstractGraphicsDevice implementations, including
        // AWTGraphicsDevice instances -- the OpenGL binding will take
        // care of handling AWTGraphicsDevices on X11 platforms (as
        // well as X11GraphicsDevices in non-AWT situations)
        registerFactory(abstractGraphicsDeviceClass, new GraphicsConfigurationFactoryImpl());
    }

    /** Returns the factory for use with the given type of
        AbstractGraphicsDevice. */
    public static GraphicsConfigurationFactory getFactory(AbstractGraphicsDevice device) {
        if (device == null) {
            return getFactory(AbstractGraphicsDevice.class);
        }
        return getFactory(device.getClass());
    }

    /**
     * Returns the graphics configuration factory for use with the
     * given class, which must implement the {@link
     * AbstractGraphicsDevice} interface.
     *
     * @throws IllegalArgumentException if the given class does not implement AbstractGraphicsDevice
     */
    public static GraphicsConfigurationFactory getFactory(Class abstractGraphicsDeviceImplementor)
        throws IllegalArgumentException, NativeWindowException
    {
        if (!(abstractGraphicsDeviceClass.isAssignableFrom(abstractGraphicsDeviceImplementor))) {
            throw new IllegalArgumentException("Given class must implement AbstractGraphicsDevice");
        }
        Class clazz = abstractGraphicsDeviceImplementor;
        while (clazz != null) {
            GraphicsConfigurationFactory factory =
                (GraphicsConfigurationFactory) registeredFactories.get(clazz);
            if (factory != null) {
                return factory;
            }
            clazz = clazz.getSuperclass();
        }
        // Return the default
        return (GraphicsConfigurationFactory) registeredFactories.get(abstractGraphicsDeviceClass);
    }

    /** Registers a GraphicsConfigurationFactory handling graphics
     * device objects of the given class. This does not need to be
     * called by end users, only implementors of new
     * GraphicsConfigurationFactory subclasses.
     *
     * @throws IllegalArgumentException if the given class does not implement AbstractGraphicsDevice
     */
    protected static void registerFactory(Class abstractGraphicsDeviceImplementor, GraphicsConfigurationFactory factory)
        throws IllegalArgumentException
    {
        if (!(abstractGraphicsDeviceClass.isAssignableFrom(abstractGraphicsDeviceImplementor))) {
            throw new IllegalArgumentException("Given class must implement AbstractGraphicsDevice");
        }
        registeredFactories.put(abstractGraphicsDeviceImplementor, factory);
    }

    /**
     * <P> Selects a graphics configuration on the specified graphics
     * device compatible with the supplied {@link Capabilities}. Some
     * platforms (specifically X11) require the graphics configuration
     * to be specified when the native window is created. This method
     * is mainly intended to be both used and implemented by the
     * OpenGL binding, and may return null on platforms on which the
     * OpenGL pixel format selection process is performed later or in
     * other unspecified situations. </P>
     *
     * <P> The concrete data type of the passed graphics device and
     * returned graphics configuration must be specified in the
     * documentation binding this particular API to the underlying
     * window toolkit. The Reference Implementation accepts {@link
     * AWTGraphicsDevice AWTGraphicsDevice} objects and returns {@link
     * AWTGraphicsConfiguration AWTGraphicsConfiguration} objects. On
     * X11 platforms where the AWT is not in use, it also accepts
     * {@link javax.media.nativewindow.x11.X11GraphicsDevice
     * X11GraphicsDevice} objects and returns {@link
     * javax.media.nativewindow.x11.X11GraphicsConfiguration
     * X11GraphicsConfiguration} objects.</P>
     *
     * @throws IllegalArgumentException if the data type of the passed
     *         AbstractGraphicsDevice is not supported by this
     *         NativeWindowFactory.
     * @throws NativeWindowException if any window system-specific errors caused
     *         the selection of the graphics configuration to fail.
     */
    public abstract AbstractGraphicsConfiguration
        chooseGraphicsConfiguration(Capabilities capabilities,
                                    CapabilitiesChooser chooser,
                                    AbstractGraphicsDevice device)
        throws IllegalArgumentException, NativeWindowException;
}
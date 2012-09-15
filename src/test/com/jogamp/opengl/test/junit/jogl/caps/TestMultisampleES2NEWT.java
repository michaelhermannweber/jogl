/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
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
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package com.jogamp.opengl.test.junit.jogl.caps;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import org.junit.Test;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.test.junit.jogl.demos.es2.MultisampleDemoES2;
import com.jogamp.opengl.test.junit.util.MiscUtils;
import com.jogamp.opengl.test.junit.util.UITestCase;
import com.jogamp.opengl.util.GLReadBufferUtil;
import com.jogamp.opengl.util.texture.TextureIO;

public class TestMultisampleES2NEWT extends UITestCase {
  static long durationPerTest = 60; // ms

  public static void main(String[] args) {
     for(int i=0; i<args.length; i++) {
        if(args[i].equals("-time")) {
            durationPerTest = MiscUtils.atoi(args[++i], 500);
        }
     }
     System.out.println("durationPerTest: "+durationPerTest);
     String tstname = TestMultisampleES2NEWT.class.getName();
     org.junit.runner.JUnitCore.main(tstname);
  }

  @Test
  public void testOnscreenMultiSampleAA0() throws InterruptedException {
    testMultiSampleAAImpl(false, false, 0);
  }

  @Test
  public void testOnscreenMultiSampleAA8() throws InterruptedException {
    testMultiSampleAAImpl(false, false, 8);
  }

  @Test
  public void testOffscreenPBufferMultiSampleAA0() throws InterruptedException {
    testMultiSampleAAImpl(false, true, 0);
  }

  @Test(timeout = 3000) // 3s timeout
  public void testOffsreenPBufferMultiSampleAA8() throws InterruptedException {
    testMultiSampleAAImpl(false, true, 8);
  }

  @Test(timeout = 3000) // 3s timeout
  public void testOffscreenFBOMultiSampleAA0() throws InterruptedException {
    testMultiSampleAAImpl(true, false, 0);
  }

  @Test(timeout = 3000) // 3s timeout
  public void testOffsreenFBOMultiSampleAA8() throws InterruptedException {
    testMultiSampleAAImpl(true, false, 8);
  }

  private void testMultiSampleAAImpl(boolean useFBO, boolean usePBuffer, int reqSamples) throws InterruptedException {
    if(useFBO) {
        System.err.println("NEWT offscreen FBO Window n/a yet");
        return;
    }
    final GLReadBufferUtil screenshot = new GLReadBufferUtil(true, false);
    GLProfile glp = GLProfile.getGL2ES2();
    GLCapabilities caps = new GLCapabilities(glp);
    GLCapabilitiesChooser chooser = new MultisampleChooser01();

    caps.setAlphaBits(1);
    caps.setFBO(useFBO);
    caps.setPBuffer(usePBuffer);
    
    if(reqSamples>0) {
        caps.setSampleBuffers(true);
        caps.setNumSamples(reqSamples);
    }

    final GLWindow window = GLWindow.create(caps);
    window.setCapabilitiesChooser(chooser);
    window.addGLEventListener(new MultisampleDemoES2(reqSamples>0?true:false));
    window.addGLEventListener(new GLEventListener() {
        int displayCount = 0;
        public void init(GLAutoDrawable drawable) {}
        public void dispose(GLAutoDrawable drawable) {}
        public void display(GLAutoDrawable drawable) {
            snapshot(displayCount++, null, drawable.getGL(), screenshot, TextureIO.PNG, null);
        }
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }
    });
    window.setSize(512, 512);
    window.setVisible(true);
    window.requestFocus();

    Thread.sleep(durationPerTest);

    window.destroy();
  }

}

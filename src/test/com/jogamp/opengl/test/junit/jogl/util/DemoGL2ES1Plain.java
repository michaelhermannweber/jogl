package com.jogamp.opengl.test.junit.jogl.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.fixedfunc.GLPointerFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2es1.GLUgl2es1;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.GLArrayDataWrapper;
import com.jogamp.opengl.util.GLBuffers;

class DemoGL2ES1Plain implements GLEventListener {
    final boolean useArrayData;
    final boolean useVBO;
    final GLU glu;
    
    final float[] vertices = new float[] { 0,          0,       0,
                                           TestImmModeSinkES1NEWT.iWidth,     0,       0,
                                           TestImmModeSinkES1NEWT.iWidth / 2, TestImmModeSinkES1NEWT.iHeight, 0 };
    
    final float[] colors = new float[] { 1, 0, 0, 
                                         0, 1, 0, 
                                         0, 0, 1 };
    
    final ByteBuffer bufferAll;
    final int bufferVOffset, bufferCOffset;
    final int bufferVSize, bufferCSize;
    final FloatBuffer bufferC, bufferV;
    final int[] vboName = new int[] { 0 };
    final GLArrayDataWrapper arrayC, arrayV;
    
    DemoGL2ES1Plain(boolean useArrayData, boolean useVBO) {
        this.useArrayData = useArrayData;
        this.useVBO = useVBO;            
        this.glu = new GLUgl2es1();
        
        bufferAll = Buffers.newDirectByteBuffer( ( colors.length + vertices.length ) * Buffers.SIZEOF_FLOAT );
        
        bufferVOffset = 0;
        bufferVSize = 3*3*GLBuffers.sizeOfGLType(GL.GL_FLOAT);
        bufferCOffset = bufferVSize;
        bufferCSize = 3*3*GLBuffers.sizeOfGLType(GL.GL_FLOAT);
        
        bufferV = (FloatBuffer) GLBuffers.sliceGLBuffer(bufferAll, bufferVOffset, bufferVSize, GL.GL_FLOAT);
        bufferV.put(vertices, 0, vertices.length).rewind();
        bufferC = (FloatBuffer) GLBuffers.sliceGLBuffer(bufferAll, bufferCOffset, bufferCSize, GL.GL_FLOAT);
        bufferC.put(colors, 0, colors.length).rewind();
        
        System.err.println("bufferAll: "+bufferAll+", byteOffset "+Buffers.getDirectBufferByteOffset(bufferAll));
        System.err.println("bufferV: off "+bufferVOffset+", size "+bufferVSize+": "+bufferV+", byteOffset "+Buffers.getDirectBufferByteOffset(bufferV));
        System.err.println("bufferC: off "+bufferCOffset+", size "+bufferCSize+": "+bufferC+", byteOffset "+Buffers.getDirectBufferByteOffset(bufferC));
    
        if(useArrayData) {            
            arrayV = GLArrayDataWrapper.createFixed(GLPointerFunc.GL_VERTEX_ARRAY, 3, GL.GL_FLOAT, false, 0,
                                                    bufferV, 0, bufferVOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
            
            arrayC = GLArrayDataWrapper.createFixed(GLPointerFunc.GL_COLOR_ARRAY, 3, GL.GL_FLOAT, false, 0,
                                                    bufferC, 0, bufferCOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
        } else {
            arrayV = null;
            arrayC = null;                
        }
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        System.err.println("GL_VENDOR   "+gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER "+gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION  "+gl.glGetString(GL.GL_VERSION));
        if(useVBO) {
            gl.glGenBuffers(1, vboName, 0);
            if(0 == vboName[0]) {
                throw new GLException("glGenBuffers didn't return valid VBO name");
            }
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2ES1 gl = drawable.getGL().getGL2ES1();
        
        gl.glMatrixMode( GL2ES1.GL_PROJECTION );
        gl.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        glu.gluOrtho2D( 0.0f, width, 0.0f, height );

        gl.glMatrixMode( GL2ES1.GL_MODELVIEW );
        gl.glLoadIdentity();

        gl.glViewport( 0, 0, width, height );        
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2ES1 gl = drawable.getGL().getGL2ES1();
        
        gl.glClear( GL.GL_COLOR_BUFFER_BIT );

        // draw a triangle filling the window
        gl.glLoadIdentity();

        if(useVBO) {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, bufferAll.limit(), bufferAll, GL.GL_STATIC_DRAW);
            if(useArrayData) {
                arrayV.setVBOName(vboName[0]);
                arrayC.setVBOName(vboName[0]);
            }
        }
        
        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        if(useArrayData) {
            gl.glVertexPointer(arrayV);
        } else {
            if(useVBO) {            
                gl.glVertexPointer(3, GL.GL_FLOAT, 0, bufferVOffset);
            } else {
                gl.glVertexPointer(3, GL.GL_FLOAT, 0, bufferV);
            }
        }
                    
        gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);            
        if(useArrayData) {
            gl.glColorPointer(arrayC);
        } else {
            if(useVBO) {            
                gl.glColorPointer(3, GL.GL_FLOAT, 0, bufferCOffset);
            } else {
                gl.glColorPointer(3, GL.GL_FLOAT, 0, bufferC);
            }
        }
                    
        if(useVBO) {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
        
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        gl.glFlush();
        
        gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
        gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        if(0 != vboName[0]) {
            gl.glDeleteBuffers(1, vboName, 0);
            vboName[0] = 0;
        }
    }        
}
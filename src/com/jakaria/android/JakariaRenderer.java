/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
//.........new comment
package com.jakaria.android;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;

import com.jakaria.android.R;
import com.jakaria.android.util.LoggerConfig;
import com.jakaria.android.util.ShaderHelper;
import com.jakaria.android.util.TextResourceReader;

public class JakariaRenderer implements Renderer {                       
    private static final String U_MATRIX = "u_Matrix";
    
    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";
    
    private static final int BYTES_PER_FLOAT = 4;  
    
    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final FloatBuffer mCubeNormals;
    private final Context context;
    
    private float[] ViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] mvMatrix = new float[16];
    private final float[] FprojectionMatrix = new float[16];
    
    private final float[] modelMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];

    private int program;
    private int uMatrixLocation;
    private int mPositionHandle;
	
	/** This will be used to pass in model color information. */
	private int mColorHandle;
	
	/** This will be used to pass in the light position. */
	private int mLightPosHandle;
	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;	
	
	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;	
	
	/** Size of the normal data in elements. */
	private final int mNormalDataSize = 3;
	
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];


    public JakariaRenderer(Context context) {
        this.context = context;
     
        
        final float[] cubePositionData =
    		{
    				// In OpenGL counter-clockwise winding is default. This means that when we look at a triangle, 
    				// if the points are counter-clockwise we are looking at the "front". If not we are looking at
    				// the back. OpenGL has an optimization where all back-facing triangles are culled, since they
    				// usually represent the backside of an object and aren't visible anyways.
    				
    				// Front face
    				-1.0f, 1.0f, 1.0f,				
    				-1.0f, -1.0f, 1.0f,
    				1.0f, 1.0f, 1.0f, 
    				-1.0f, -1.0f, 1.0f, 				
    				1.0f, -1.0f, 1.0f,
    				1.0f, 1.0f, 1.0f,
    				
    				// Right face
    				1.0f, 1.0f, 1.0f,				
    				1.0f, -1.0f, 1.0f,
    				1.0f, 1.0f, -1.0f,
    				1.0f, -1.0f, 1.0f,				
    				1.0f, -1.0f, -1.0f,
    				1.0f, 1.0f, -1.0f,
    				
    				// Back face
    				1.0f, 1.0f, -1.0f,				
    				1.0f, -1.0f, -1.0f,
    				-1.0f, 1.0f, -1.0f,
    				1.0f, -1.0f, -1.0f,				
    				-1.0f, -1.0f, -1.0f,
    				-1.0f, 1.0f, -1.0f,
    				
    				// Left face
    				-1.0f, 1.0f, -1.0f,				
    				-1.0f, -1.0f, -1.0f,
    				-1.0f, 1.0f, 1.0f, 
    				-1.0f, -1.0f, -1.0f,				
    				-1.0f, -1.0f, 1.0f, 
    				-1.0f, 1.0f, 1.0f, 
    				
    				// Top face
    				-1.0f, 1.0f, -1.0f,				
    				-1.0f, 1.0f, 1.0f, 
    				1.0f, 1.0f, -1.0f, 
    				-1.0f, 1.0f, 1.0f, 				
    				1.0f, 1.0f, 1.0f, 
    				1.0f, 1.0f, -1.0f,
    				
    				// Bottom face
    				1.0f, -1.0f, -1.0f,				
    				1.0f, -1.0f, 1.0f, 
    				-1.0f, -1.0f, -1.0f,
    				1.0f, -1.0f, 1.0f, 				
    				-1.0f, -1.0f, 1.0f,
    				-1.0f, -1.0f, -1.0f,
    		};	
    		
    		// R, G, B, A
    		final float[] cubeColorData =
    		{				
    				// Front face (red)
    				1.0f, 0.0f, 0.0f, 1.0f,				
    				1.0f, 0.0f, 0.0f, 1.0f,
    				1.0f, 0.0f, 0.0f, 1.0f,
    				1.0f, 0.0f, 0.0f, 1.0f,				
    				1.0f, 0.0f, 0.0f, 1.0f,
    				1.0f, 0.0f, 0.0f, 1.0f,
    				
    				// Right face (green)
    				0.0f, 1.0f, 0.0f, 1.0f,				
    				0.0f, 1.0f, 0.0f, 1.0f,
    				0.0f, 1.0f, 0.0f, 1.0f,
    				0.0f, 1.0f, 0.0f, 1.0f,				
    				0.0f, 1.0f, 0.0f, 1.0f,
    				0.0f, 1.0f, 0.0f, 1.0f,
    				
    				// Back face (blue)
    				0.0f, 0.0f, 1.0f, 1.0f,				
    				0.0f, 0.0f, 1.0f, 1.0f,
    				0.0f, 0.0f, 1.0f, 1.0f,
    				0.0f, 0.0f, 1.0f, 1.0f,				
    				0.0f, 0.0f, 1.0f, 1.0f,
    				0.0f, 0.0f, 1.0f, 1.0f,
    				
    				// Left face (yellow)
    				1.0f, 1.0f, 0.0f, 1.0f,				
    				1.0f, 1.0f, 0.0f, 1.0f,
    				1.0f, 1.0f, 0.0f, 1.0f,
    				1.0f, 1.0f, 0.0f, 1.0f,				
    				1.0f, 1.0f, 0.0f, 1.0f,
    				1.0f, 1.0f, 0.0f, 1.0f,
    				
    				// Top face (cyan)
    				0.0f, 1.0f, 1.0f, 1.0f,				
    				0.0f, 1.0f, 1.0f, 1.0f,
    				0.0f, 1.0f, 1.0f, 1.0f,
    				0.0f, 1.0f, 1.0f, 1.0f,				
    				0.0f, 1.0f, 1.0f, 1.0f,
    				0.0f, 1.0f, 1.0f, 1.0f,
    				
    				// Bottom face (magenta)
    				1.0f, 0.0f, 1.0f, 1.0f,				
    				1.0f, 0.0f, 1.0f, 1.0f,
    				1.0f, 0.0f, 1.0f, 1.0f,
    				1.0f, 0.0f, 1.0f, 1.0f,				
    				1.0f, 0.0f, 1.0f, 1.0f,
    				1.0f, 0.0f, 1.0f, 1.0f
    		};
    		
    		// X, Y, Z
    		// The normal is used in light calculations and is a vector which points
    		// orthogonal to the plane of the surface. For a cube model, the normals
    		// should be orthogonal to the points of each face.
    		final float[] cubeNormalData =
    		{												
    				// Front face
    				0.0f, 0.0f, 1.0f,				
    				0.0f, 0.0f, 1.0f,
    				0.0f, 0.0f, 1.0f,
    				0.0f, 0.0f, 1.0f,				
    				0.0f, 0.0f, 1.0f,
    				0.0f, 0.0f, 1.0f,
    				
    				// Right face 
    				1.0f, 0.0f, 0.0f,				
    				1.0f, 0.0f, 0.0f,
    				1.0f, 0.0f, 0.0f,
    				1.0f, 0.0f, 0.0f,				
    				1.0f, 0.0f, 0.0f,
    				1.0f, 0.0f, 0.0f,
    				
    				// Back face 
    				0.0f, 0.0f, -1.0f,				
    				0.0f, 0.0f, -1.0f,
    				0.0f, 0.0f, -1.0f,
    				0.0f, 0.0f, -1.0f,				
    				0.0f, 0.0f, -1.0f,
    				0.0f, 0.0f, -1.0f,
    				
    				// Left face 
    				-1.0f, 0.0f, 0.0f,				
    				-1.0f, 0.0f, 0.0f,
    				-1.0f, 0.0f, 0.0f,
    				-1.0f, 0.0f, 0.0f,				
    				-1.0f, 0.0f, 0.0f,
    				-1.0f, 0.0f, 0.0f,
    				
    				// Top face 
    				0.0f, 1.0f, 0.0f,			
    				0.0f, 1.0f, 0.0f,
    				0.0f, 1.0f, 0.0f,
    				0.0f, 1.0f, 0.0f,				
    				0.0f, 1.0f, 0.0f,
    				0.0f, 1.0f, 0.0f,
    				
    				// Bottom face 
    				0.0f, -1.0f, 0.0f,			
    				0.0f, -1.0f, 0.0f,
    				0.0f, -1.0f, 0.0f,
    				0.0f, -1.0f, 0.0f,				
    				0.0f, -1.0f, 0.0f,
    				0.0f, -1.0f, 0.0f
    		};
    		
    		// Initialize the buffers.
    		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();							
    		mCubePositions.put(cubePositionData).position(0);		
    		
    		mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();							
    		mCubeColors.put(cubeColorData).position(0);
    		
    		mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();							
    		mCubeNormals.put(cubeNormalData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		
		// Position the eye in front of the origin.
				final float eyeX = 0.0f;
				final float eyeY = 8.0f;
				final float eyeZ = 3.5f;

				// We are looking toward the distance
				final float lookX = 0.0f;
				final float lookY = 0.0f;
				final float lookZ = -15.0f;

				// Set our up vector. This is where our head would be pointing were we holding the camera.
				final float upX = 0.0f;
				final float upY = 1.0f;
				final float upZ = 0.0f;

				// Set the view matrix. This matrix can be said to represent the camera position.
				// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
				// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
				Matrix.setLookAtM(ViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        String vertexShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper
            .compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }

        glUseProgram(program);
        
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        
    }


    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
        
        final float ratio = (float) height/ width;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 20.0f;
		
		Matrix.frustumM(projectionMatrix, 0, bottom, top, left, right, near, far);
        
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        long time = SystemClock.uptimeMillis() % 10000L;        
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        glUseProgram(program);
                        
        // Assign the matrix
                        
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, -4.0f, 0.0f, -7.0f);     
        Matrix.scaleM(modelMatrix, 0, 0.5f, 2.0f, 2.0f);
        drawCube();
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -7.0f);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);  
        drawCube();
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.scaleM(modelMatrix, 0, 0.5f, 2.0f, 2.0f);
        drawCube();
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -9.0f);
        Matrix.scaleM(modelMatrix, 0, 4.0f, 2.0f, 0.5f);
        drawCube();
    }
    
    
    private void drawCube()
	{		
    	
    	mPositionHandle = glGetAttribLocation(program, A_POSITION);
        mColorHandle = glGetAttribLocation(program, A_COLOR);
         
		// Pass in the position information
		mCubePositions.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		0, mCubePositions);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		0, mCubeColors);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);    
        
        Matrix.multiplyMM(mvMatrix, 0, ViewMatrix, 0, modelMatrix, 0);  
        multiplyMM(FprojectionMatrix, 0, projectionMatrix, 0, mvMatrix, 0);        
        glUniformMatrix4fv(uMatrixLocation, 1, false, FprojectionMatrix, 0);
	}
}

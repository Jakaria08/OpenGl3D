uniform mat4 u_Matrix;
uniform vec3 u_LightPos;

attribute vec4 a_Position;  
attribute vec4 a_Color;
attribute vec3 a_Normal; 

varying vec4 v_Color;

void main()                    
{                            
    v_Color = a_Color;
	  	  
    gl_Position = u_Matrix * a_Position;
          
}          
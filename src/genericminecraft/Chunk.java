package genericminecraft;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author ameer
 */
public class Chunk 
{
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private Block[][][] blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private Texture texture;
    private int startX, startZ;
    private Random r;
    private float[][] noise;
    
    public Chunk(int startX, int startZ)
    {
        // open texture
        try
        {
            texture = TextureLoader.getTexture("PNG",
                    ResourceLoader.getResourceAsStream("terrain.png"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        // generate perlin noise
        r = new Random();
        blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for(int x = 0; x < CHUNK_SIZE; x++)
        {
            for(int z = 0; z < CHUNK_SIZE; z++)
            {
                for(int y = 0; y  < CHUNK_SIZE; y++)
                {
                    if(r.nextFloat()>0.7f)
                        blocks[x][y][z] = new Block(Block.BlockType.GRASS);
                    else if(r.nextFloat() > 0.4f)
                        blocks[x][y][z] = new Block(Block.BlockType.DIRT);
                    else if(r.nextFloat() > 0.2f)
                        blocks[x][y][z] = new Block(Block.BlockType.WATER);
                    else
                        blocks[x][y][z] = new Block(Block.BlockType.NOT_DEFINED);
                }
            }
        }
        
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        this.startX = startX*CHUNK_SIZE;
        this.startZ = startZ*CHUNK_SIZE;
        rebuildMesh();
    }
    
    public void render()
    {
        glPushMatrix();
            glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
            glColorPointer(4, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
            glBindTexture(GL_TEXTURE_2D, 1);
            glTexCoordPointer(2, GL_FLOAT, 0, 0L);
            glDrawArrays(GL_QUADS, 0,
                        CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE*24);
        glPopMatrix();
    }
    
    public void rebuildMesh()
    {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers(); // placed among the other VBOs
        
        FloatBuffer vertexPositionData = 
                BufferUtils.createFloatBuffer(
                (CHUNK_SIZE*CHUNK_SIZE*
                        CHUNK_SIZE)*6*4*3);
        
        FloatBuffer vertexColorData = 
                BufferUtils.createFloatBuffer(
                (CHUNK_SIZE*CHUNK_SIZE*
                        CHUNK_SIZE)*6*4*4);
        
        FloatBuffer vertexTextureData = 
                BufferUtils.createFloatBuffer(
                        (CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE)*6*4*2
                );
        
        for(float x = 0; x < CHUNK_SIZE; x++)
        {
            for(float z = 0; z < CHUNK_SIZE; z++)
            {
                for(float y = 0; y < CHUNK_SIZE; y++)
                {
                    // vertex data
                    vertexPositionData.put
                    (
                    createCube
                    (
                            (float)(startX + x*CUBE_LENGTH),
                            (float)(y*CUBE_LENGTH),
                            (float)(startZ + z*CUBE_LENGTH))
                    );
                    
                    // color data
                    vertexColorData.put
                    (
                        createCubeVertexColor
                        (
                            getCubeColor(blocks[(int)x][(int)y][(int)z])
                        )
                    );
                    
                    // texture data
                    vertexTextureData.put
                    (
                            createTextureCube
                            (
                                0f, 0f, blocks[(int)x][(int)y][(int)z]
                            )
                    );
                }
            }
        }
        
        vertexColorData.flip();
        vertexPositionData.flip();
        vertexTextureData.flip();
        
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexTextureData,
                GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER,
                VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER,
                vertexPositionData,
                GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER,
                VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER,
                vertexColorData,
                GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    private float[] getCubeColor(Block block)
    {
        float rTop, gTop, bTop, aTop,
             rBot, gBot, bBot, aBot,
             rFront, gFront, bFront, aFront,
             rBack, gBack, bBack, aBack,
             rLeft, gLeft, bLeft, aLeft,
             rRight, gRight, bRight, aRight;
        
        aTop = 1f;
        aBot = 1f;
        aFront = 1f;
        aBack = 1f;
        aLeft = 1f;
        aRight = 1f;
        
        switch(block.getType())
        {
            case GRASS:
                rTop =      0;
                gTop =      1;
                bTop =      0;
                rBot =      1;
                gBot =      1;
                bBot =      1;
                rFront =    1;
                gFront =    1;
                bFront =    1;
                rBack =     1;
                gBack =     1;
                bBack =     1;
                rLeft =     1;
                gLeft =     1;
                bLeft =     1;
                rRight =    1;
                gRight =    1;
                bRight =    1;
                break;
            default:
                rTop =      1;
                gTop =      1;
                bTop =      1;
                rBot =      1;
                gBot =      1;
                bBot =      1;
                rFront =    1;
                gFront =    1;
                bFront =    1;
                rBack =     1;
                gBack =     1;
                bBack =     1;
                rLeft =     1;
                gLeft =     1;
                bLeft =     1;
                rRight =    1;
                gRight =    1;
                bRight =    1;
                break;
        }
         return new float[] 
         {
             rBot, gBot, bBot, aBot,
             rTop, gTop, bTop, aTop,
             rFront, gFront, bFront, aFront,
             rBack, gBack, bBack, aBack,
             rLeft, gLeft, bLeft, aLeft,
             rRight, gRight, bRight, aRight
         };
    }
    
    private float[] createCubeVertexColor(float[] cubeColorArray)
    {
        float[] cubeColors = new float[cubeColorArray.length*4];
        
        for(int i = 0; i < cubeColors.length; i++)
        {
            cubeColors[i] = cubeColorArray[i%cubeColorArray.length];
        }
        
        return cubeColors;
    }
    
    private float[] createCube(float x, float y, float z)
    {
        int offset = CUBE_LENGTH / 2;
        
        // vertices are relative to the center of the cube
        
        return new float[]
        {
            // TOP QUAD
            x+offset, y+offset, z+offset,
            x-offset, y+offset, z+offset,
            x-offset, y+offset, z-offset,
            x+offset, y+offset, z-offset,
            // BOTTOM QUAD
            x+offset, y-offset, z-offset,
            x-offset, y-offset, z-offset,
            x-offset, y-offset, z+offset,
            x+offset, y-offset, z+offset,
            // FRONT QUAD
            x+offset, y+offset, z-offset,
            x-offset, y+offset, z-offset,
            x-offset, y-offset, z-offset,
            x+offset, y-offset, z-offset,
            // BACK QUAD
            x+offset, y-offset, z+offset,
            x-offset, y-offset, z+offset,
            x-offset, y+offset, z+offset,
            x+offset, y+offset, z+offset,
            // LEFT QUAD
            x-offset, y+offset, z-offset, 
            x-offset, y+offset, z+offset,
            x-offset, y-offset, z+offset,
            x-offset, y-offset, z-offset,
            // RIGHT QUAD
            x+offset, y+offset, z+offset,
            x+offset, y+offset, z-offset,
            x+offset, y-offset, z-offset,
            x+offset, y-offset, z+offset
        };
    }

    private float[] createTextureCube(float x, float y, Block block) 
    {
        float topX, topY;
        float botX, botY;
        float frontX, frontY;
        float backX, backY;
        float leftX, leftY;
        float rightX, rightY;
        
        float offset = (1024f/16)/1024;
                
        switch(block.getType())
        {
            case WATER:
                topX =      13;
                topY =      12;
                botX =      13;
                botY =      12;
                frontX =    13;
                frontY =    12;
                backX =     13;
                backY =     12;
                leftX =     13;
                leftY =     12;
                rightX =    13;
                rightY =    12; 
                break;
            case BEDROCK:
                topX =      1;
                topY =      1;
                botX =      1;
                botY =      1;
                frontX =    1;
                frontY =    1;
                backX =     1;
                backY =     1;
                leftX =     1;
                leftY =     1;
                rightX =    1;
                rightY =    1; 
                break;
            case STONE:
                topX =      1;
                topY =      0;
                botX =      1;
                botY =      0;
                frontX =    1;
                frontY =    0;
                backX =     1;
                backY =     0;
                leftX =     1;
                leftY =     0;
                rightX =    1;
                rightY =    0; 
                break;
            case SAND:
                topX =      2;
                topY =      1;
                botX =      2;
                botY =      1;
                frontX =    2;
                frontY =    1;
                backX =     2;
                backY =     1;
                leftX =     2;
                leftY =     1;
                rightX =    2;
                rightY =    1; 
                break;
            case GRAVEL:
                topX =      3;
                topY =      1;
                botX =      3;
                botY =      1;
                frontX =    3;
                frontY =    1;
                backX =     3;
                backY =     1;
                leftX =     3;
                leftY =     1;
                rightX =    3;
                rightY =    1; 
                break;
            case GRASS:
                topX =      0;
                topY =      0;
                botX =      2;
                botY =      0;
                frontX =    3;
                frontY =    0;
                backX =     3;
                backY =     0;
                leftX =     3;
                leftY =     0;
                rightX =    3;
                rightY =    0; 
                break;
            case DIRT:
                topX =      2;
                topY =      0;
                botX =      2;
                botY =      0;
                frontX =    2;
                frontY =    0;
                backX =     2;
                backY =     0;
                leftX =     2;
                leftY =     0;
                rightX =    2;
                rightY =    0; 
                break;
            default:
                topX =      3 ;
                topY =      13;
                botX =      3 ;
                botY =      13;
                frontX =    3 ;
                frontY =    13;
                backX =     3 ;
                backY =     13;
                leftX =     3 ;
                leftY =     13;
                rightX =    3 ;
                rightY =    13; 
                break;
        }
         return new float[] {
            // TOP
            x + offset*(topX+1), y + offset*(topY+1),
            x + offset*topX, y + offset*(topY+1),
            x + offset*topX, y + offset*topY,
            x + offset*(topX+1), y + offset*topY,
            // BOT QUAD
            x + offset*(botX+1), y + offset*(botY+1),
            x + offset*botX, y + offset*(botY+1),
            x + offset*botX, y + offset*botY,
            x + offset*(botX+1), y + offset*botY,
            // FRONT QUAD
            x + offset*frontX, y + offset*frontY,
            x + offset*(frontX+1), y + offset*frontY,
            x + offset*(frontX+1), y + offset*(frontY+1),
            x + offset*frontX, y + offset*(frontY+1),
            // BACK QUAD
            x + offset*(backX+1), y + offset*(backY+1),
            x + offset*backX, y + offset*(backY+1),
            x + offset*backX, y + offset*backY,
            x + offset*(backX+1), y + offset*backY,
            // LEFT QUAD
            x + offset*leftX, y + offset*leftY,
            x + offset*(leftX+1), y + offset*leftY,
            x + offset*(leftX+1), y + offset*(leftY+1),
            x + offset*leftX, y + offset*(leftY+1),
            // RIGHT QUAD
            x + offset*rightX, y + offset*rightY,
            x + offset*(rightX+1), y + offset*rightY,
            x + offset*(rightX+1), y + offset*(rightY+1),
            x + offset*rightX, y + offset*(rightY+1)};       
    }
}

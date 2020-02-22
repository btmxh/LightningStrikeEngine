package lightningstike.engine.data;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import lightningstike.engine.util.OBJLoader;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class GObject {
    public GObject(String objModelName, Vector3f pos, Vector3f rot, Vector3f scale, GMaterial material) {
        try {
            this.model = OBJLoader.load(objModelName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pos = pos;
        this.rot = rot;
        this.scale = scale;
        GMaterial mat = material;
        this.mat = mat;
        initMesh();
    }

    public int vao, pbo, ibo, tbo;
    public int indsCount;

    public Vector3f pos, rot, scale;
    public GMaterial mat;
    public Obj model;

    public void selectForRendering() {
        SelectedData.obj = this;
    }

    public void initMesh() {
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(this.model.getNumVertices() * 3);
        float[] positionData = new float[this.model.getNumVertices()* 3];
        for (int i = 0; i < this.model.getNumVertices(); i++) {
            positionData[i * 3] = this.model.getVertex(i).getX();
            positionData[i * 3 + 1] = this.model.getVertex(i).getY();
            positionData[i * 3 + 2] = this.model.getVertex(i).getZ();
        }
        positionBuffer.put(positionData).flip();

        pbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, pbo);
        GL15.glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        ArrayList<Integer> inds = new ArrayList<>();

        for (int i = 0; i < this.model.getNumFaces(); i++) {
            for (int j = 0; j < this.model.getFace(i).getNumVertices(); j++) {
                inds.add(this.model.getFace(i).getVertexIndex(j));
                indsCount++;
            }
        }

        IntBuffer indsBuffer = MemoryUtil.memAllocInt(indsCount);
        indsBuffer.put(inds.stream().mapToInt(i -> i).toArray());
        indsBuffer.flip();

        ibo = glGenBuffers();
        glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indsBuffer, GL15.GL_STATIC_DRAW);
        glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        float[] texcord = new float[this.model.getNumTexCoords()];

        for (int i = 0; i < this.model.getNumTexCoords()*2; i+=2) {
            texcord[i/2] = (this.model.getTexCoord(i/2).getX());
            texcord[i/2] = (this.model.getTexCoord(i/2).getY());
        }

        tbo = glGenBuffers();
        FloatBuffer textCoordsBuffer = MemoryUtil.memAllocFloat(this.model.getNumTexCoords()*4);
        textCoordsBuffer.put(texcord).flip();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    }

}
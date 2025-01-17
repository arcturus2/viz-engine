package org.gephi.viz.engine.lwjgl.pipeline.common;

import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.lwjgl.models.NodeDiskModel;
import org.gephi.viz.engine.lwjgl.util.gl.GLBuffer;
import org.gephi.viz.engine.lwjgl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.util.gl.Constants;
import static org.gephi.viz.engine.util.gl.Constants.*;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import org.lwjgl.opengl.GLCapabilities;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractNodeData {

    protected static final float BORDER_SIZE = 0.16f;
    protected static final float INSIDE_CIRCLE_SIZE = 1 - BORDER_SIZE;

    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_64 = 128;
    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_32 = 16;
    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_16 = 2;

    protected GLBuffer vertexGLBuffer;
    protected GLBuffer attributesGLBuffer;
    protected final NodesCallback nodesCallback = new NodesCallback();

    protected static final int ATTRIBS_STRIDE = NodeDiskModel.TOTAL_ATTRIBUTES_FLOATS;

    protected final boolean instanced;

    public AbstractNodeData(boolean instanced) {
        this.instanced = instanced;
    }

    protected int fillNodeAttributesData(final float[] buffer, final Node node, final int index, final boolean someSelection, final boolean selected) {
        final float x = node.x();
        final float y = node.y();
        final float size = node.size();
        final int rgba = node.getRGBA();

        //Outside circle:
        {
            //Position:
            buffer[index + 0] = x;
            buffer[index + 1] = y;

            //Color:
            buffer[index + 2] = Float.intBitsToFloat(rgba);
            //Bias, multiplier and lighten factor:
            buffer[index + 3] = 0;
            if (someSelection) {
                if (selected) {
                    buffer[index + 4] = 1;
                } else {
                    buffer[index + 4] = Constants.NODER_BORDER_DARKEN_FACTOR;//Darken the color
                }
            } else {
                buffer[index + 4] = Constants.NODER_BORDER_DARKEN_FACTOR;//Darken the color
            }

            //Size:
            buffer[index + 5] = size;
        }

        final int nextIndex = index + ATTRIBS_STRIDE;

        //Inside circle:
        {
            //Position:
            buffer[nextIndex + 0] = x;
            buffer[nextIndex + 1] = y;

            //Color:
            buffer[nextIndex + 2] = Float.intBitsToFloat(rgba);
            //Bias and multiplier:
            if (someSelection) {
                if (selected) {
                    buffer[nextIndex + 3] = 0.5f;
                    buffer[nextIndex + 4] = 0.5f;
                } else {
                    buffer[nextIndex + 3] = 0;
                    buffer[nextIndex + 4] = 1;
                }
            } else {
                buffer[nextIndex + 3] = 0;
                buffer[nextIndex + 4] = 1;
            }

            //Size:
            buffer[nextIndex + 5] = size * INSIDE_CIRCLE_SIZE;
        }

        return nextIndex + ATTRIBS_STRIDE;
    }

    private NodesVAO nodesVAO;

    public void setupVertexArrayAttributes(VizEngine engine) {
        if (nodesVAO == null) {
            nodesVAO = new NodesVAO(
                    engine.getLookup().lookup(GLCapabilities.class),
                    engine.getLookup().lookup(OpenGLOptions.class)
            );
        }

        nodesVAO.use();
    }

    public void unsetupVertexArrayAttributes() {
        nodesVAO.stopUsing();
    }

    public void dispose() {
        if (vertexGLBuffer != null) {
            vertexGLBuffer.destroy();
        }

        if (attributesGLBuffer != null) {
            attributesGLBuffer.destroy();
        }

        nodesCallback.reset();
    }

    private class NodesVAO extends GLVertexArrayObject {

        public NodesVAO(GLCapabilities capabilities, OpenGLOptions openGLOptions) {
            super(capabilities, openGLOptions);
        }

        @Override
        protected void configure() {
            vertexGLBuffer.bind();
            {
                glVertexAttribPointer(SHADER_VERT_LOCATION, NodeDiskModel.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            }
            vertexGLBuffer.unbind();

            if (instanced) {
                attributesGLBuffer.bind();
                {
                    final int stride = ATTRIBS_STRIDE * Float.BYTES;
                    int offset = 0;

                    glVertexAttribPointer(SHADER_POSITION_LOCATION, NodeDiskModel.POSITION_FLOATS, GL_FLOAT, false, stride, offset);
                    offset += NodeDiskModel.POSITION_FLOATS * Float.BYTES;

                    glVertexAttribPointer(SHADER_COLOR_LOCATION, NodeDiskModel.COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                    offset += NodeDiskModel.COLOR_FLOATS * Float.BYTES;

                    glVertexAttribPointer(SHADER_COLOR_BIAS_LOCATION, NodeDiskModel.COLOR_BIAS_FLOATS, GL_FLOAT, false, stride, offset);
                    offset += NodeDiskModel.COLOR_BIAS_FLOATS * Float.BYTES;

                    glVertexAttribPointer(SHADER_COLOR_MULTIPLIER_LOCATION, NodeDiskModel.COLOR_MULTIPLIER_FLOATS, GL_FLOAT, false, stride, offset);
                    offset += NodeDiskModel.COLOR_MULTIPLIER_FLOATS * Float.BYTES;

                    glVertexAttribPointer(SHADER_SIZE_LOCATION, NodeDiskModel.SIZE_FLOATS, GL_FLOAT, false, stride, offset);
                }
                attributesGLBuffer.unbind();
            }
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            if (instanced) {
                return new int[]{
                    SHADER_VERT_LOCATION,
                    SHADER_POSITION_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_COLOR_BIAS_LOCATION,
                    SHADER_COLOR_MULTIPLIER_LOCATION,
                    SHADER_SIZE_LOCATION
                };
            } else {
                return new int[]{
                    SHADER_VERT_LOCATION
                };
            }
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instanced) {
                return new int[]{
                    SHADER_POSITION_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_COLOR_BIAS_LOCATION,
                    SHADER_COLOR_MULTIPLIER_LOCATION,
                    SHADER_SIZE_LOCATION
                };
            } else {
                return null;
            }
        }

    }
}

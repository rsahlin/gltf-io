
package org.gltfio;

import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Matrix.MatrixStack;

public class DepthFirstNodeIterator extends NodeIterator {

    MatrixStack matrixStack = new MatrixStack();
    // float[] currentMatrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);

    /**
     * Creates a new depth first node iterator
     * 
     * @param start
     * @throws IllegalArgumentException If start is null
     */
    public DepthFirstNodeIterator(JSONNode start) {
        super(start);
    }

    public DepthFirstNodeIterator(RenderableScene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene may not be null");
        }
        JSONNode<?>[] nodes = scene.getNodes();
        if (nodes == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", Nodes in scenes are not resolved");
        }
        if (nodes != null) {
            for (JSONNode<?> n : nodes) {
                // These nodes should be FIFO - ie use addLast()
                stack.addLast(new TraverseData(n));
            }
        }
    }

    @Override
    public boolean hasNext() {
        while (stack.size() > 0) {
            TraverseData<JSONNode<? extends JSONMesh<JSONPrimitive>>> t = stack.peekFirst();
            if (t != null) {
                boolean hasNext = t.hasNext();
                if (hasNext) {
                    return true;
                }
                stack.removeFirst();
            }
        }
        return false;
    }

    @Override
    public JSONNode next() {
        JSONNode child = getNextChild();
        return child;
    }

    /**
     * Returns the next child or null
     * 
     * @return
     */
    protected JSONNode<? extends JSONMesh<JSONPrimitive>> getNextChild() {
        TraverseData<JSONNode<? extends JSONMesh<JSONPrimitive>>> t = stack.peekFirst();
        if (t != null) {
            JSONNode<? extends JSONMesh<JSONPrimitive>> node = getNextChild(t);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    private JSONNode<? extends JSONMesh<JSONPrimitive>> getNextChild(TraverseData<JSONNode<? extends JSONMesh<JSONPrimitive>>> traverse) {
        JSONNode<? extends JSONMesh<JSONPrimitive>> child = traverse.next(stack);
        if (child == null) {
            // matrixStack.pop(currentMatrix, 0);
            if (stack.size() > 0) {
                stack.removeFirst();
            }
            TraverseData<JSONNode<? extends JSONMesh<JSONPrimitive>>> parent = stack.peekFirst();
            return parent != null ? getNextChild(parent) : null;
        }
        // matrixStack.push(currentMatrix, 0);
        // MatrixUtils.copy(child.concatModelMatrix(currentMatrix), 0, currentMatrix, 0);
        return child;
    }
}

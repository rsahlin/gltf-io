
package org.gltfio;

import java.util.ArrayDeque;
import java.util.Iterator;

import org.gltfio.gltf2.JSONNode;

import org.gltfio.lib.MatrixUtils;

/**
 * Superclass for gltf node iterator
 *
 */
public abstract class NodeIterator implements Iterator<JSONNode> {

    protected static class TraverseData<T extends JSONNode<?>> {
        protected T node;
        protected int childIndex = 0;
        protected T[] children;
        protected float[] matrix = MatrixUtils.createMatrix();

        TraverseData(T n) {
            this.node = n;
        }

        protected TraverseData(T[] childs) {
            children = childs;
        }

        /**
         * Increments childIndex and returns the node if it's the first index, otherwise the child
         * is returned if valid - if so null is returned.
         * 
         * @param stack The traverse stack
         * @return
         */
        protected T next(ArrayDeque<TraverseData<T>> stack) {
            if (node != null) {
                children = (T[]) node.getChildren();
                T n = node;
                node = null;
                return n;
            }
            if (children == null) {
                return null;
            }
            T child = (childIndex) >= children.length ? null : children[childIndex++];
            if (child != null) {
                stack.push(new TraverseData(child.getChildren()));
            }
            return child;
        }

        /**
         * Returns true if there is one or more nodes to traverse, false otherwise
         * 
         * @return
         */
        protected boolean hasNext() {
            return node != null ? true : (children == null) ? false : (childIndex < children.length);
        }

    }

    protected ArrayDeque<TraverseData<JSONNode>> stack = new ArrayDeque<TraverseData<JSONNode>>();
    protected JSONNode current = null;
    protected int currentChild = -1;

    /**
     * Internal constructor
     */
    protected NodeIterator() {

    }

    /**
     * Creates a new node iterator, for the nodetree starting with node
     * 
     * @param start
     * @throws IllegalArgumentException If start is null
     */
    protected NodeIterator(JSONNode<?> start) {
        if (start == null) {
            throw new IllegalArgumentException("Start node may not be null");
        }
        stack.push(new TraverseData(start.getChildren()));
    }

}

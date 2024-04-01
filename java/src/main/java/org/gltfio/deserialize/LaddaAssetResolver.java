
package org.gltfio.deserialize;

import java.util.ArrayList;

import org.gltfio.deserialize.Ladda.AssetResolver;
import org.gltfio.gltf2.Gltf2TransientDelegator;
import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.glxf.Glxf;
import org.gltfio.glxf.GlxfTransientDelegator;

public class LaddaAssetResolver implements AssetResolver {

    @Override
    public void resolveSceneGraph(JSONGltf glTF) {
        resolveNodes(glTF);
        resolveTransientObjects(glTF);
    }

    @Override
    public void resolveTransientValues(Glxf glXF) {
        GlxfTransientDelegator delegator = GlxfTransientDelegator.getInstance();
        delegator.resolveTransientObjects(glXF);
    }

    /**
     * Creates the runtime values needed by node / gltf objects
     * This method shall only be called once.
     */
    protected void resolveTransientObjects(JSONGltf glTF) {
        Gltf2TransientDelegator delegator = Gltf2TransientDelegator.getInstance();
        delegator.resolveTransientObjects(glTF);
    }

    private void resolveNodes(JSONGltf glTF) {
        JSONNode[] nodes = glTF.getNodes();
        JSONCamera[] cameras = glTF.getCameras();
        if (nodes != null) {
            for (JSONNode n : nodes) {
                n.resolveTransientValues();
                if (n.getMeshIndex() >= 0) {
                    n.setMeshRef(glTF.getMeshes()[n.getMeshIndex()]);
                }
                int ci = n.getCameraIndex();
                if (ci != -1) {
                    JSONCamera camera = n.resolveCameraRef(cameras);
                    glTF.addInstanceCamera(camera);
                }
                int[] children = n.getChildIndexes();
                ArrayList<JSONNode<?>> childNodes = new ArrayList<JSONNode<?>>();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) {
                        JSONNode child = nodes[children[i]];
                        childNodes.add(child);
                    }
                }
                n.setChildrenRef(childNodes);
            }
        }
    }

}

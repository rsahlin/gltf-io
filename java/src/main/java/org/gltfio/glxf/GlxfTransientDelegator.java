
package org.gltfio.glxf;

public class GlxfTransientDelegator {

    private static GlxfTransientDelegator delegator = null;

    private GlxfTransientDelegator() {
    }

    public static GlxfTransientDelegator getInstance() {
        if (delegator == null) {
            delegator = new GlxfTransientDelegator();
        }
        return delegator;
    }

    public void resolveTransientObjects(Glxf glXF) {
        glXF.resolveTransientValues();
    }
}

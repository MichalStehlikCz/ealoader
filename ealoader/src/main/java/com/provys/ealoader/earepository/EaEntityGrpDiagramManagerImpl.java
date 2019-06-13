package com.provys.ealoader.earepository;

import org.sparx.Diagram;

public class EaEntityGrpDiagramManagerImpl {
    private void export(Diagram diagram, String fileName) {
        diagram.SaveImagePage(0, 0, 0, 0, fileName, 0);
    }
}

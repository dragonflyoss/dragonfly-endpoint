package org.pytorch.serve.plugins.dragonfly.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface FileLoadUtils {
    void copyURLToFile(String fileName, File modelLocation) throws IOException;

}

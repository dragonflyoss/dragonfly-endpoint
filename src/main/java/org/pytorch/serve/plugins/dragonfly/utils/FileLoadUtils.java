package org.pytorch.serve.plugins.dragonfly.utils;

import java.io.File;
import java.io.IOException;
import org.pytorch.serve.archive.model.ModelException;

public interface FileLoadUtils {
  void copyURLToFile(String fileName, File location) throws IOException, ModelException;
}

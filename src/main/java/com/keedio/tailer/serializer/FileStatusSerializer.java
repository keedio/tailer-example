package com.keedio.tailer.serializer;

import com.keedio.tailer.io.Tailer;
import com.keedio.tailer.io.TailerInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public class FileStatusSerializer  implements StatusSerializer {
  private transient final static Logger LOGGER = LogManager.getLogger(TailerInitializer.class);

  @Value("${status.file.path:}")
  private String statusfile;

  @Override
  public void serializeStatus(Collection<Tailer> tailerInitializers) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(statusfile))) {

      List<TailerInitializer> initializers = new ArrayList<>();
      for (Tailer tailer : tailerInitializers) {
        initializers.add(tailer.getInitializer());
      }

      oos.writeObject(initializers);
    } catch (IOException e) {
      LOGGER.error("Cannot serialize tailer initializers",e);
    }

  }

  @Override
  public List<Tailer> deserializeStatus() {
    File status = new File(statusfile);

    if (status.exists()){

      try {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(status));

        List<TailerInitializer> intializers = (List<TailerInitializer>) ois.readObject();

        List<Tailer> tailers = new ArrayList<>();

        for (TailerInitializer initializer : intializers) {
          tailers.add(new Tailer(initializer));
        }

        return tailers;
      } catch (Exception e) {
        LOGGER.error("Cannot deserialize tailer initializers", e);
      }
    }

    return null;
  }
}

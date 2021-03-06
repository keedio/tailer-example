package com.keedio.example.serializer;

import com.keedio.example.io.Tailer;
import com.keedio.example.io.TailerInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public class FileStatusSerializer  implements StatusSerializer {
  private transient final static Logger LOGGER = LogManager.getLogger(TailerInitializer.class);

  private static final String statusfile = "/Users/luca/tmp/test/tailer-status.ser";

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

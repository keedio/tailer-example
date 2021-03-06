package com.keedio.example;

import com.keedio.example.rotation.UnixLogRotationPolicy;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;

import java.io.File;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
public class Main {
  private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(KeedioTailerListenerAdapter.class);

  public static void main(String[] args) throws Exception {

    IOFileFilter fileFilter =
            FileFilterUtils.or(DirectoryFileFilter.DIRECTORY,
            FileFilterUtils.and(
                    HiddenFileFilter.VISIBLE,
                    FileFilterUtils.suffixFileFilter(".log")));

    File directory = new File(args[0]);
    FileAlterationObserver observer = new FileAlterationObserver(directory, fileFilter);
    observer.addListener(new KeedioFileAlterationListenerAdaptor(new UnixLogRotationPolicy()));

    final FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);

    monitor.start();

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        try {
          LOGGER.info("Stopping monitor");
          monitor.stop(0);
        } catch (Exception e) {
          LOGGER.error("Exception while stopping monitor",e);
        }
      }
    });
  }
}

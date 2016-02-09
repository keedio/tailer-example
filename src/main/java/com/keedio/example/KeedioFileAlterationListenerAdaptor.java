package com.keedio.example;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
public class KeedioFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {
  private final static Logger LOGGER = LogManager.getLogger(KeedioFileAlterationListenerAdaptor.class);

  private ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Override
  public void onStart(FileAlterationObserver observer) {
    super.onStart(observer);
  }

  @Override
  public void onDirectoryCreate(File directory) {
    super.onDirectoryCreate(directory);

    LOGGER.info("onDirectoryCreate: "+directory);
  }

  @Override
  public void onDirectoryChange(File directory) {
    super.onDirectoryChange(directory);

    LOGGER.info("onDirectoryChange: "+directory);
  }

  @Override
  public void onDirectoryDelete(File directory) {
    super.onDirectoryDelete(directory);

    LOGGER.info("onDirectoryDelete: "+directory);
  }

  @Override
  public void onFileCreate(File file) {
    super.onFileCreate(file);

    LOGGER.info("onFileCreate: "+file);

    Tailer tailer = new Tailer(file, new KeedioTailerListenerAdapter(), 50);
    executorService.submit(tailer);
  }

  @Override
  public void onFileChange(File file) {
    super.onFileChange(file);

    LOGGER.info("onFileChange: "+file);
  }

  @Override
  public void onFileDelete(File file) {
    super.onFileDelete(file);

    LOGGER.info("onFileDelete: "+file);
  }

  @Override
  public void onStop(FileAlterationObserver observer) {
    super.onStop(observer);
  }
}

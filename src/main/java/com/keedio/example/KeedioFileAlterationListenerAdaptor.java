package com.keedio.example;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.keedio.example.io.Tailer;
import com.keedio.example.rotation.RotationPolicy;
import com.keedio.example.serializer.FileStatusSerializer;
import com.keedio.example.serializer.StatusSerializer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
public class KeedioFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {
  private final static Logger LOGGER = LogManager.getLogger(KeedioFileAlterationListenerAdaptor.class);

  final static ListeningExecutorService executorService =
          MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

  final static Map<String, Tailer> tailers = Collections.synchronizedMap(new HashMap<String, Tailer>());

  private RotationPolicy rotationPolicy;

  public KeedioFileAlterationListenerAdaptor(RotationPolicy rotationPolicy) {
    this.rotationPolicy = rotationPolicy;
  }

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

    initTailer(file);
  }

  private Tailer initTailer(final File file) {
    String isEnd = System.getProperty("start.from.end");

    Tailer tailer = new Tailer(file, new KeedioTailerListenerAdapter(rotationPolicy), 100,
            isEnd == null||"".equals(isEnd) ? false : Boolean.valueOf(isEnd));

    doInitTailer(file, tailer);

    return tailer;
  }

  private void doInitTailer(final File file, Tailer tailer) {
    tailers.put(file.getAbsolutePath(),tailer);

    ListenableFuture<?> tailerFuture = executorService.submit(Executors.callable(tailer));

    tailerFuture.addListener(new Runnable() {
      public void run() {
        LOGGER.warn("Forgetting file: " + file.getAbsolutePath());
        forgetFile(file);
      }
    }, MoreExecutors.directExecutor());
  }

  @Override
  public void onFileChange(File file) {
    super.onFileChange(file);

    Tailer tailer = tailers.get(file.getAbsolutePath());

    if (tailer == null){
      initTailer(file);
    }

    LOGGER.info("onFileChange: "+file);
  }

  @Override
  public void onFileDelete(File file) {
    super.onFileDelete(file);

    forgetFile(file);

    LOGGER.info("onFileDelete: "+file);
  }

  void forgetFile(File file) {
    Tailer tailer = tailers.get(file.getAbsolutePath());

    if (tailer != null){
      tailer.stop();
      tailers.remove(file.getAbsolutePath());
    }
  }

  @Override
  public void onStop(FileAlterationObserver observer) {
    super.onStop(observer);
  }
}

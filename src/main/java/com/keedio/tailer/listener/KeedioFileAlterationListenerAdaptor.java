package com.keedio.tailer.listener;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.keedio.tailer.LRTailer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
@Component
public class KeedioFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {
  private final static Logger LOGGER = LogManager.getLogger(KeedioFileAlterationListenerAdaptor.class);

  final static ListeningExecutorService executorService =
          MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

  final static Map<String, LRTailer> tailers = Collections.synchronizedMap(new HashMap<String, LRTailer>());

  @Autowired
  private FileEventListener keedioTailerListener;

  @Value("${start.from.end:false}")
  private boolean startFromEnd;

  @Value("${tail.delay.millis:100}")
  private long tailDelayMillis;

  private LRTailer initTailer(final File file) {
    LRTailer tailer = new LRTailer(keedioTailerListener, tailDelayMillis, file.getAbsolutePath());

    doInitTailer(file, tailer);

    return tailer;
  }

  private void doInitTailer(final File file, LRTailer tailer) {
    tailers.put(file.getAbsolutePath(), tailer);

    ListenableFuture<?> tailerFuture = executorService.submit(Executors.callable(tailer));

    tailerFuture.addListener(new Runnable() {
      public void run() {
        LOGGER.warn("Forgetting file: " + file.getAbsolutePath());
        forgetFile(file);
      }
    }, MoreExecutors.directExecutor());
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

  @Override
  public void onFileChange(File file) {
    super.onFileChange(file);

    LRTailer tailer = tailers.get(file.getAbsolutePath());

    if (tailer == null){
      initTailer(file);
    }

    LOGGER.trace("onFileChange: "+file);
  }

  @Override
  public void onFileDelete(File file) {
    super.onFileDelete(file);

    forgetFile(file);

    LOGGER.info("onFileDelete: "+file);
  }

  void forgetFile(File file) {
    LRTailer tailer = tailers.get(file.getAbsolutePath());

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

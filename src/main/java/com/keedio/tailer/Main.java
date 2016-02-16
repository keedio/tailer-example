package com.keedio.tailer;

import com.keedio.tailer.listener.KeedioFileEventListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
public class Main {
  private final static org.apache.logging.log4j.Logger LOGGER =
          LogManager.getLogger(KeedioFileEventListener.class);

  static FileAlterationMonitor monitor;
  static AnnotationConfigApplicationContext ctx;
  public static void main(String[] args) throws Exception {

    ctx = new AnnotationConfigApplicationContext("com.keedio.tailer");

    monitor =  ctx.getBean(FileAlterationMonitor.class);
    monitor.start();
  }
}

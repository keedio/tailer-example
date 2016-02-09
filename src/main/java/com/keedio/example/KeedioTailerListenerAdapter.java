package com.keedio.example;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
public class KeedioTailerListenerAdapter extends TailerListenerAdapter {
  private final static Logger LOGGER = LogManager.getLogger(KeedioTailerListenerAdapter.class);

  private final static Logger OUT_LOGGER = LogManager.getLogger("com.keedio.out");
  private Tailer instance;
  private int countedLines = 0;

  @Override
  public void handle(Exception ex) {
    super.handle(ex);

    LOGGER.error("Exception caught", ex);
  }

  @Override
  public void init(Tailer tailer) {
    super.init(tailer);

    this.instance = tailer;

    LOGGER.info("Initializing tailer on file: " + tailer.getFile().getAbsolutePath());
  }

  @Override
  public void fileNotFound() {
    super.fileNotFound();

    LOGGER.info("File not found: " + instance.getFile().getAbsolutePath());
  }

  @Override
  public void fileRotated() {
    super.fileRotated();

    LOGGER.info("File rotated: " + instance.getFile().getAbsolutePath());
  }

  @Override
  public void handle(String line) {
    super.handle(line);

    countedLines++;
    LOGGER.info("[" + instance.getFile().getAbsolutePath() + "], lines: " + countedLines);
    OUT_LOGGER.trace(line);
  }
}

package com.keedio.tailer.io;

import com.keedio.tailer.listener.DummyKeedioTailListener;
import com.keedio.tailer.listener.KeedioTailerListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
public class Tailer extends org.apache.commons.io.input.Tailer {

  private static final int DEFAULT_DELAY_MILLIS = 1000;

  private static final String RAF_MODE = "r";

  private static final int DEFAULT_BUFSIZE = 4096;

  private long position = 0; // position within the file

  private long lastTimestamp = 0;


  /**
   * Buffer on top of RandomAccessFile.
   */
  private final byte inbuf[];

  /**
   * The file which will be tailed.
   */
  private final File file;

  /**
   * The amount of time to wait for the file to be updated.
   */
  private final long delayMillis;

  /**
   * Whether to tail from the end or start of file
   */
  private final boolean end;

  /**
   * The listener to notify of events when tailing.
   */
  private KeedioTailerListener listener;
  private final KeedioTailerListener dummyListener = new DummyKeedioTailListener();
  /**
   * Whether to close and reopen the file whilst waiting for more input.
   */
  private final boolean reOpen;

  /**
   * The tailer will run as long as this value is true.
   */
  private volatile boolean run = true;

  public Tailer(TailerInitializer initializer){
    this(new File(initializer.getFilename()), initializer.getKeedioTailerListener(), initializer.getPosition());
    this.lastTimestamp = initializer.getLastTimestamp();
  }

  public TailerInitializer getInitializer(){
    return new TailerInitializer(
            file.getAbsolutePath(),
            position,
            lastTimestamp,
            listener.getClass().getCanonicalName(),
            listener.getRotationPolicy().getClass().getCanonicalName());
  }

  /**
   * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
   * @param file The file to follow.
   * @param listener the TailerListener to use.
   */
  public Tailer(File file, KeedioTailerListener listener) {
    this(file, listener, DEFAULT_DELAY_MILLIS);
  }

  /**
   * Creates a Tailer for the given file, starting from the beginning.
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   */
  public Tailer(File file, KeedioTailerListener listener, long delayMillis) {
    this(file, listener, delayMillis, false);
  }

  /**
   * Creates a Tailer for the given file, with a delay other than the default 1.0s.
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   */
  public Tailer(File file, KeedioTailerListener listener, long delayMillis, boolean end) {
    this(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
  }

  /**
   * Creates a Tailer for the given file, with a delay other than the default 1.0s.
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @param reOpen if true, close and reopen the file between reading chunks
   */
  public Tailer(File file, KeedioTailerListener listener, long delayMillis, boolean end, boolean reOpen) {
    this(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
  }

  /**
   * Creates a Tailer for the given file, with a specified buffer size.
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @param bufSize Buffer size
   */
  public Tailer(File file, KeedioTailerListener listener, long delayMillis, boolean end, int bufSize) {
    this(file, listener, delayMillis, end, false, bufSize);
  }

  /**
   * Creates a Tailer for the given file, with a specified buffer size.
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @param reOpen if true, close and reopen the file between reading chunks
   * @param bufSize Buffer size
   */
  public Tailer(File file, KeedioTailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
    super(file, listener, delayMillis, end, reOpen, bufSize);

    this.file = file;
    this.delayMillis = delayMillis;
    this.end = end;

    this.inbuf = new byte[bufSize];

    // Save and prepare the listener
    this.listener = listener;
    listener.init(this);
    this.reOpen = reOpen;
  }

  /**
   * Creates and starts a Tailer for the given file.
   *
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @param bufSize buffer size.
   * @return The new tailer
   */
  public static Tailer create(File file, KeedioTailerListener listener, long delayMillis, boolean end, int bufSize) {
    Tailer tailer = new Tailer(file, listener, delayMillis, end, bufSize);
    Thread thread = new Thread(tailer);
    thread.setDaemon(true);
    thread.start();
    return tailer;
  }

  /**
   * Creates and starts a Tailer for the given file.
   *
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @param reOpen whether to close/reopen the file between chunks
   * @param bufSize buffer size.
   * @return The new tailer
   */
  public static Tailer create(File file, KeedioTailerListener listener, long delayMillis, boolean end, boolean reOpen,
                              int bufSize) {
    Tailer tailer = new Tailer(file, listener, delayMillis, end, reOpen, bufSize);
    Thread thread = new Thread(tailer);
    thread.setDaemon(true);
    thread.start();
    return tailer;
  }

  /**
   * Creates and starts a Tailer for the given file with default buffer size.
   *
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @return The new tailer
   */
  public static Tailer create(File file, KeedioTailerListener listener, long delayMillis, boolean end) {
    return create(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
  }

  /**
   * Creates and starts a Tailer for the given file with default buffer size.
   *
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
   * @param reOpen whether to close/reopen the file between chunks
   * @return The new tailer
   */
  public static Tailer create(File file, KeedioTailerListener listener, long delayMillis, boolean end, boolean reOpen) {
    return create(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
  }

  /**
   * Creates and starts a Tailer for the given file, starting at the beginning of the file
   *
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @return The new tailer
   */
  public static Tailer create(File file, KeedioTailerListener listener, long delayMillis) {
    return create(file, listener, delayMillis, false);
  }

  /**
   * Creates and starts a Tailer for the given file, starting at the beginning of the file
   * with the default delay of 1.0s
   *
   * @param file the file to follow.
   * @param listener the TailerListener to use.
   * @return The new tailer
   */
  public static Tailer create(File file, KeedioTailerListener listener) {
    return create(file, listener, DEFAULT_DELAY_MILLIS, false);
  }

  /**
   * Return the file.
   *
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * Return the delay in milliseconds.
   *
   * @return the delay in milliseconds.
   */
  public long getDelay() {
    return delayMillis;
  }

  public long getPosition() {
    return position;
  }

  /**
   * Follows changes in the file, calling the TailerListener's handle method for each new line.
   */
  public void run() {
    RandomAccessFile reader = null;
    try {
      //long lastTimestamp = 0; // The lastTimestamp time the file was checked for changes
      // Open the file
      while (run && reader == null) {
        try {
          reader = new RandomAccessFile(file, RAF_MODE);
        } catch (FileNotFoundException e) {
          listener.fileNotFound();
        }

        if (reader == null) {
          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException e) {
          }
        } else {
          // The current position in the file
          if (end){
            position = file.length();
          }
          //position = end ? file.length() : ();
          lastTimestamp = System.currentTimeMillis();
          reader.seek(position);
          if (end) {
            rollbackToLineStart(reader);
          }
        }
      }

      while (run) {

        boolean newer = FileUtils.isFileNewer(file, lastTimestamp); // IO-279, must be done first

        // Check the file length to see if it was rotated
        long length = file.length();

        if (length < position) {

          // File was rotated
          listener.fileRotated();

          // Reopen the reader after rotation
          try {
            // Ensure that the old file is closed iff we re-open it successfully
            RandomAccessFile save = reader;
            reader = new RandomAccessFile(file, RAF_MODE);
            position = 0;
            // close old file explicitly rather than relying on GC picking up previous RAF
            IOUtils.closeQuietly(save);
          } catch (FileNotFoundException e) {
            // in this case we continue to use the previous reader and position values
            listener.fileNotFound();
          }
          continue;
        } else {

          // File was not rotated

          // See if the file needs to be read again
          if (length > position) {

            // The file has more content than it did lastTimestamp time
            position = readLines(reader);
            lastTimestamp = System.currentTimeMillis();

          } else if (newer) {

            /*
             * This can happen if the file is truncated or overwritten with the exact same length of
             * information. In cases like this, the file position needs to be reset
             */
            position = 0;
            reader.seek(position); // cannot be null here

            // Now we can read new lines
            position = readLines(reader);
            lastTimestamp = System.currentTimeMillis();
          }
        }
        if (reOpen) {
          IOUtils.closeQuietly(reader);
        }
        try {
          Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
        }
        if (run && reOpen) {
          reader = new RandomAccessFile(file, RAF_MODE);
          reader.seek(position);
        }
      }

    } catch (Exception e) {

      listener.handle(e);

    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  private void rollbackToLineStart(RandomAccessFile reader) throws IOException {
    final char cr = '\n';

    final int numBytesRead = 1;

    do {

      position = Math.max(0, position - numBytesRead);
      reader.seek(position);

    } while (position > 0 && (char) reader.readByte() != cr);

    if (position > 0)
      reader.seek(++position);
  }

    /**
   * Allows the tailer to complete its current loop and return.
   */
  public void stop() {
    this.run = false;
  }

  /**
   * Read new lines.
   *
   * @param reader The file to read
   * @return The new position after the lines have been read
   * @throws java.io.IOException if an I/O error occurs.
   */
  private long readLines(RandomAccessFile reader) throws IOException {
    StringBuilder sb = new StringBuilder();

    long pos = reader.getFilePointer();
    long rePos = pos; // position to re-read



    int num;
    boolean seenCR = false;
    while (run && ((num = reader.read(inbuf)) != -1)) {
      for (int i = 0; i < num; i++) {
        byte ch = inbuf[i];
        switch (ch) {
          case '\n':
            seenCR = false; // swallow CR before LF
            listener.handle(sb.toString());
            sb.setLength(0);
            rePos = pos + i + 1;
            break;
          case '\r':
            if (seenCR) {
              sb.append('\r');
            }
            seenCR = true;
            break;
          default:
            if (seenCR) {
              seenCR = false; // swallow final CR
              listener.handle(sb.toString());
              sb.setLength(0);
              rePos = pos + i + 1;
            }
            sb.append((char) ch); // add character, not its ascii value
        }
      }

      pos = reader.getFilePointer();
    }

    reader.seek(rePos); // Ensure we can re-read if necessary
    return rePos;
  }

  public long getLastTimestamp() {
    return lastTimestamp;
  }
}

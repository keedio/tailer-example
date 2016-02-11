package com.keedio.tailer.listener;

import com.keedio.tailer.io.Tailer;
import com.keedio.tailer.rotation.RotationPolicy;
import org.apache.commons.io.input.TailerListener;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public interface KeedioTailerListener extends TailerListener {
  /**
   * The tailer will call this method during construction,
   * giving the listener a method of stopping the tailer.
   * @param tailer the tailer.
   */
  void init(Tailer tailer);

  RotationPolicy getRotationPolicy();
}

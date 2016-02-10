package com.keedio.example;

import com.keedio.example.io.Tailer;
import com.keedio.example.rotation.RotationPolicy;
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

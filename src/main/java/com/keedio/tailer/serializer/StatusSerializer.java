package com.keedio.tailer.serializer;

import com.keedio.tailer.io.Tailer;

import java.util.Collection;
import java.util.List;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public interface StatusSerializer {
  void serializeStatus(Collection<Tailer> tailerInitializers);

  List<Tailer> deserializeStatus();
}

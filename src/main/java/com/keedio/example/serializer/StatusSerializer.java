package com.keedio.example.serializer;

import com.keedio.example.io.Tailer;
import com.keedio.example.io.TailerInitializer;

import java.util.Collection;
import java.util.List;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public interface StatusSerializer {
  void serializeStatus(Collection<Tailer> tailerInitializers);

  List<Tailer> deserializeStatus();
}

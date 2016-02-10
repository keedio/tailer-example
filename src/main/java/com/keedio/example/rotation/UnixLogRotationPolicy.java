package com.keedio.example.rotation;

import java.io.File;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public class UnixLogRotationPolicy implements RotationPolicy {
  @Override
  public String rotatedFileName(String path, String fileName) {
    return path + File.separator + fileName + ".1";
  }
}

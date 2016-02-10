package com.keedio.example.rotation;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public interface RotationPolicy {

  String rotatedFileName(String path, String fileName);
}

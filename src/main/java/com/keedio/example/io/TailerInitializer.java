package com.keedio.example.io;

import com.keedio.example.KeedioTailerListener;
import com.keedio.example.rotation.RotationPolicy;
import com.keedio.example.serializer.StatusSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 10/2/16.
 */
public class TailerInitializer implements Serializable{
  private transient final static Logger LOGGER = LogManager.getLogger(TailerInitializer.class);

  private String filename;
  private long position;
  private long lastTimestamp;

  private String keedioTailListenerClazz;
  private String rotationPolicyClazz;
  private String statusSerializerClazz;

  public TailerInitializer(String filename,
                           long position,
                           long lastTimestamp,
                           String keedioTailListenerClazz,
                           String rotationPolicyClazz) {
    this.filename = filename;
    this.position = position;
    this.keedioTailListenerClazz=keedioTailListenerClazz;
    this.rotationPolicyClazz = rotationPolicyClazz;
    this.lastTimestamp = lastTimestamp;
  }

  public String getFilename() {
    return filename;
  }

  public long getPosition() {
    return position;
  }

  public RotationPolicy getRotationPolicy(){

    try {
      return (RotationPolicy)Class.forName(rotationPolicyClazz).newInstance();
    } catch (Exception e) {
      LOGGER.error("Cannot instantiate rotation policy", e);

      return null;
    }
  }

  public long getLastTimestamp() {
    return lastTimestamp;
  }

  public KeedioTailerListener getKeedioTailerListener(){
    try {
      Constructor c = Class.forName(keedioTailListenerClazz).getConstructor(
              RotationPolicy.class,
              StatusSerializer.class);

      return (KeedioTailerListener)c.newInstance(getRotationPolicy());

    } catch (Exception e) {
      LOGGER.error("Cannot instantiate KeedioTailerListener", e);
      return null;
    }


  }
}

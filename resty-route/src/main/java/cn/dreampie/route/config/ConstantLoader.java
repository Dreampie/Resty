package cn.dreampie.route.config;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.route.holder.ExceptionHolder;
import cn.dreampie.route.render.RenderFactory;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;

/**
 * The constant for Resty runtime.
 */
final public class ConstantLoader {

  public void setDefaultForward(String url) {
    ExceptionHolder.setDefaultForward(url);
  }

  public void setDefaultRedirect(String url) {
    ExceptionHolder.setDefaultRedirect(url);
  }

  public void addFoward(HttpStatus status, String url) {
    ExceptionHolder.addFoward(status, url);
  }

  public void addRedirect(HttpStatus status, String url) {
    ExceptionHolder.addRedirect(status, url);
  }

  //render
  public void addRender(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public void addDefaultRender(String extension, Render render) {
    RenderFactory.addDefault(extension, render);
  }

  public void addJsonConfig(Type type, ObjectSerializer serializer, ObjectDeserializer deserializer) {
    addJsonSerializer(type, serializer);
    addJsonDeserializer(type, deserializer);
  }

  public void addJsonSerializerFeature(SerializerFeature... features) {
    Jsoner.addSerializerFeature(features);
  }

  public void addJsonDeserializerFeature(Feature... features) {
    Jsoner.addDeserializerFeature(features);
  }

  public void addJsonSerializer(Type type, ObjectSerializer serializer) {
    Jsoner.addSerializer(type, serializer);
  }

  public void addJsonDeserializer(Type type, ObjectDeserializer deserializer) {
    Jsoner.addDeserializer(type, deserializer);
  }
}








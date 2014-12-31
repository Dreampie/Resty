package cn.dreampie.util.json;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by ice on 14-12-31.
 */
public class ModelSerializer implements ObjectSerializer {
  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
    if (object == null) {
      serializer.getWriter().writeNull();
      return;
    }

  }
}

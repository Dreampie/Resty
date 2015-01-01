package cn.dreampie.util.json;

import cn.dreampie.common.Entity;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by ice on 14-12-31.
 */
public enum ModelSerializer implements ObjectSerializer {
  INSTANCE;

  public static ModelSerializer instance() {
    return INSTANCE;
  }


  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
    SerializeWriter write = serializer.getWriter();
    if (object == null) {
      write.writeNull();
      return;
    }

    if (object instanceof Entity) {
      serializer.write(((Entity) object).getAttrs());
    }

  }
}

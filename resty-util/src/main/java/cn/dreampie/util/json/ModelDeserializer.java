package cn.dreampie.util.json;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

/**
 * Created by ice on 14-12-31.
 */
public class ModelDeserializer implements ObjectDeserializer{
  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {

    JSONLexer lexer = parser.getLexer();


    return null;
  }

  public int getFastMatchToken() {
    return 0;
  }
}

package cn.dreampie.orm.meta;

import cn.dreampie.common.generate.Generator;
import cn.dreampie.orm.repository.GenerateType;

import java.lang.reflect.Field;

public class IdMeta extends FieldMeta {

  private final GenerateType generate;
  private final Generator generator;


  public IdMeta(String column, Field field, GenerateType generate, Generator generator) {
    super(column, field);
    this.generate = generate;
    this.generator = generator;
  }


  public GenerateType getGenerate() {
    return generate;
  }

  public Generator getGenerator() {
    return generator;
  }
}

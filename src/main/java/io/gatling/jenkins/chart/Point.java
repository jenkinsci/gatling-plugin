/**
 * Copyright 2011-2020 GatlingCorp (http://gatling.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.jenkins.chart;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;

public class Point<X extends Number, Y extends Number> implements JsonSerializable {
  private final X x;
  private final Y y;
  private final String name;

  public Point(X x, Y y, String name) {
    this.x = x;
    this.y = y;
    this.name = name;
  }

  @SuppressWarnings("unused")
  public X getX() {
    return x;
  }

  @SuppressWarnings("unused")
  public Y getY() {
    return y;
  }

  public String getName() {
    return name;
  }

  public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeStartArray();
    jgen.writeObject(x);
    jgen.writeObject(y);
    jgen.writeObject(name);
    jgen.writeEndArray();
  }

  public void serializeWithType(
      JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) {
    throw new UnsupportedOperationException();
  }
}

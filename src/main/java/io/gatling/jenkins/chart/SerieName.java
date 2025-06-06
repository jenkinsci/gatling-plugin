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
import java.util.Objects;

public class SerieName implements JsonSerializable, Comparable<SerieName> {
  private final String name;
  private final String path;

  public SerieName(String name, String path) {
    this.name = name;
    this.path = path;
  }

  public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
    jgen.writeStartObject();
    jgen.writeStringField("label", name);
    jgen.writeStringField("path", path);
    jgen.writeEndObject();
  }

  public void serializeWithType(
      JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) {
    throw new UnsupportedOperationException();
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public int compareTo(SerieName o) {
    return this.name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SerieName serieName = (SerieName) o;
    return Objects.equals(name, serieName.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}

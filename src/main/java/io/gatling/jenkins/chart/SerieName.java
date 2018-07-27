/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.jenkins.chart;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class SerieName implements JsonSerializable, Comparable<SerieName> {
  final String name;
  final String path;

  public SerieName(String name, String path) {
    this.name = name;
    this.path = path;
  }

  public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
    jgen.writeStartObject();
    jgen.writeStringField("label", path);
    jgen.writeEndObject();
  }

  public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SerieName serieName = (SerieName) o;

    if (name != null ? !name.equals(serieName.name) : serieName.name != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  public int compareTo(SerieName o) {
    return this.name.compareTo(o.name);
  }
}

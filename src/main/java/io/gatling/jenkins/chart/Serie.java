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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Serie<X extends Number, Y extends Number> implements JsonSerializable {
  private final List<Point<X, Y>> points = new ArrayList<>();

  public void addPoint(X x, Y y, String name) {
    points.add(new Point<>(x, y, name));
  }

  public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
    List<Point<X, Y>> reversePoints = points.subList(0, points.size());
    Collections.reverse(reversePoints);
    jgen.writeObject(reversePoints);
  }

  public void serializeWithType(
      JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) {
    throw new UnsupportedOperationException();
  }
}

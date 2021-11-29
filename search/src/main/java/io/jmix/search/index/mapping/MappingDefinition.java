/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search.index.mapping;

import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.index.annotation.ManualMappingDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about indexed properties defined within index definition interface
 * marked with {@link JmixEntitySearchIndex}
 * <p>
 * It can be created automatically according to field-mapping annotations used in index definition
 * or manually within mapping method implementation.
 * Such method should fulfil the following requirements:
 * <ul>
 *     <li>Annotated with {@link ManualMappingDefinition}</li>
 *     <li>default</li>
 *     <li>With return type - {@link MappingDefinition}</li>
 *     <li>With Spring beans as parameters</li>
 * </ul>
 * <p>
 * {@link MappingDefinition#builder()} and {@link MappingDefinitionElement#builder()} should be used to create content.
 * <p>
 * <b>Note:</b> if definition method has implementation any field-mapping annotations on it will be ignored
 */
public class MappingDefinition {

    protected List<MappingDefinitionElement> elements;

    protected MappingDefinition(MappingDefinitionBuilder builder) {
        this.elements = builder.elements;
    }

    /**
     * Gets all {@link MappingDefinitionElement}
     *
     * @return List of {@link MappingDefinitionElement}
     */
    public List<MappingDefinitionElement> getElements() {
        return elements;
    }

    public static MappingDefinitionBuilder builder() {
        return new MappingDefinitionBuilder();
    }

    public static class MappingDefinitionBuilder {
        private final List<MappingDefinitionElement> elements = new ArrayList<>();

        public MappingDefinitionBuilder addElement(MappingDefinitionElement element) {
            elements.add(element);
            return this;
        }

        public MappingDefinition build() {
            return new MappingDefinition(this);
        }
    }
}

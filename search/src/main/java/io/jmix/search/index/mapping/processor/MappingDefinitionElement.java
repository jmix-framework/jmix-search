/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.index.mapping.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.strategy.FieldConfiguration;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.search.index.mapping.strategy.NativeFieldConfiguration;
import io.jmix.search.index.mapping.strategy.PropertyValueExtractor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MappingDefinitionElement {
    protected final String[] includedProperties;
    protected final String[] excludedProperties;
    protected final Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
    protected final FieldMappingStrategy fieldMappingStrategy;
    protected final FieldConfiguration explicitFieldConfiguration;
    protected final PropertyValueExtractor explicitPropertyValueExtractor;
    protected final Integer explicitOrder;
    protected final Map<String, Object> parameters;

    protected MappingDefinitionElement(MappingDefinitionElementBuilder builder) {
        this.includedProperties = builder.includedProperties;
        this.excludedProperties = builder.excludedProperties;
        this.fieldMappingStrategyClass = builder.fieldMappingStrategyClass;
        this.fieldMappingStrategy = builder.fieldMappingStrategy;
        this.explicitFieldConfiguration = builder.explicitFieldConfiguration;
        this.explicitPropertyValueExtractor = builder.explicitPropertyValueExtractor;
        this.explicitOrder = builder.explicitOrder;
        this.parameters = builder.parameters == null ? Collections.emptyMap() : builder.parameters;
    }

    /**
     * Provides full name of properties that should be indexed.
     *
     * @return property names
     */
    public String[] getIncludedProperties() {
        return includedProperties;
    }

    /**
     * Provides full name of properties that should NOT be indexed.
     *
     * @return property names
     */
    public String[] getExcludedProperties() {
        return excludedProperties;
    }

    /**
     * Provides {@link FieldMappingStrategy} implementation class that should be used to map properties.
     *
     * @return {@link FieldMappingStrategy} implementation class
     */
    @Nullable
    public Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return fieldMappingStrategyClass;
    }

    @Nullable
    public FieldMappingStrategy getFieldMappingStrategy() {
        return fieldMappingStrategy;
    }

    @Nullable
    public FieldConfiguration getExplicitFieldConfiguration() {
        return explicitFieldConfiguration;
    }

    @Nullable
    public PropertyValueExtractor getExplicitPropertyValueExtractor() {
        return explicitPropertyValueExtractor;
    }

    @Nullable
    public Integer getExplicitOrder() {
        return explicitOrder;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static MappingDefinitionElementBuilder builder() {
        return new MappingDefinitionElementBuilder();
    }

    public static class MappingDefinitionElementBuilder {

        private static final ObjectMapper mapper = new ObjectMapper();

        private String[] includedProperties = new String[0];
        private String[] excludedProperties = new String[0];
        private Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
        private FieldMappingStrategy fieldMappingStrategy;
        private FieldConfiguration explicitFieldConfiguration;
        private PropertyValueExtractor explicitPropertyValueExtractor;
        private Integer explicitOrder = null;
        private Map<String, Object> parameters = null;

        private MappingDefinitionElementBuilder() {
        }

        public MappingDefinitionElementBuilder includeProperties(String... properties) {
            this.includedProperties = properties;
            return this;
        }

        public MappingDefinitionElementBuilder excludeProperties(String... properties) {
            this.excludedProperties = properties;
            return this;
        }

        public MappingDefinitionElementBuilder withFieldMappingStrategyClass(Class<? extends FieldMappingStrategy> fieldMappingStrategyClass) {
            this.fieldMappingStrategyClass = fieldMappingStrategyClass;
            return this;
        }

        public MappingDefinitionElementBuilder withFieldMappingStrategy(FieldMappingStrategy fieldMappingStrategy) {
            this.fieldMappingStrategy = fieldMappingStrategy;
            return this;
        }

        public MappingDefinitionElementBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }

        public MappingDefinitionElementBuilder addParameter(String parameterName, Object parameterValue) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(parameterName, parameterValue);
            return this;
        }

        public MappingDefinitionElementBuilder withNativeConfiguration(String configuration) {
            try {
                ObjectNode configNode = mapper.readValue(configuration, ObjectNode.class);
                return withNativeConfiguration(configNode);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to parse native configuration", e);
            }
        }

        public MappingDefinitionElementBuilder withNativeConfiguration(ObjectNode configuration) {
            this.explicitFieldConfiguration = new NativeFieldConfiguration(configuration);
            return this;
        }

        public MappingDefinitionElementBuilder withPropertyValueExtractor(PropertyValueExtractor propertyValueExtractor) {
            this.explicitPropertyValueExtractor = propertyValueExtractor;
            return this;
        }

        public MappingDefinitionElementBuilder withExplicitOrder(int order) {
            this.explicitOrder = order;
            return this;
        }

        public MappingDefinitionElement build() {
            return new MappingDefinitionElement(this);
        }
    }
}

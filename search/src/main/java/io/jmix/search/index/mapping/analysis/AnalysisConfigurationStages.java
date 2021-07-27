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

package io.jmix.search.index.mapping.analysis;

public interface AnalysisConfigurationStages {

    interface DefineAnalyzer {
        SetupParameters configureBuiltInAnalyzer(String analyzerTypeName);

        SetupTokenizer createCustom();

        void withNativeConfiguration(String nativeConfiguration);
    }

    interface DefineNormalizer {
        SetupParameters configureBuiltInNormalizer(String normalizerTypeName);

        SetupFilters createCustom();

        void withNativeConfiguration(String nativeConfiguration);
    }

    interface DefineTokenizer {
        SetupParameters configureBuiltInTokenizer(String tokenizerTypeName);

        void withNativeConfiguration(String nativeConfiguration);
    }

    interface DefineCharacterFilter {
        SetupParameters configureBuiltInCharacterFilter(String characterFilterTypeName);

        void withNativeConfiguration(String nativeConfiguration);
    }

    interface DefineTokenFilter {
        SetupParameters configureBuiltInTokenFilter(String tokenFilterTypeName);

        void withNativeConfiguration(String nativeConfiguration);
    }

    interface SetupParameters {
        SetupParameters withParameter(String key, Object value);
    }

    interface SetupTokenizer {
        SetupFilters withTokenizer(String tokenizerName);
    }

    interface SetupCharacterFilters {
        SetupFilters withCharacterFilters(String... charFilterNames);
    }

    interface SetupTokenFilters {
        SetupFilters withTokenFilters(String... tokenFilterNames);
    }

    interface SetupFilters extends SetupCharacterFilters, SetupTokenFilters {
    }
}

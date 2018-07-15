/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.internal.provider;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.internal.validator.DefaultJsonValidatorFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonParser;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.JsonSchemaResolver;
import org.leadpony.justify.core.JsonValidatorFactory;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.spi.JsonValidationServiceProvider;
import org.leadpony.justify.internal.base.ProblemPrinter;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.io.BasicSchemaReader;
import org.leadpony.justify.internal.schema.io.ValidatingSchemaReader;

/**
 * Default implementation of {@link JsonValidationServiceProvider}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidationServiceProvider 
        extends JsonValidationServiceProvider implements JsonSchemaResolver {
    
    private JsonProvider jsonProvider;
    
    private JsonSchema metaschema;
    private JsonValidatorFactory schemaParserFactory;
    
    private static final String METASCHEMA_NAME = "metaschema-draft-07.json";
    
    public DefaultJsonValidationServiceProvider() {
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        Objects.requireNonNull(in, "in must not be null.");
        ValidatingJsonParser parser = (ValidatingJsonParser)this.schemaParserFactory.createParser(
                in, metaschema, problem->{});
        return createValidatingSchemaReader(parser);
    }
  
    @Override
    public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        Objects.requireNonNull(in, "in must not be null.");
        Objects.requireNonNull(charset, "charset must not be null.");
        ValidatingJsonParser parser = (ValidatingJsonParser)this.schemaParserFactory.createParser(
                in, charset, metaschema, problem->{});
        return createValidatingSchemaReader(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null.");
        ValidatingJsonParser parser = (ValidatingJsonParser)this.schemaParserFactory.createParser(
                reader, metaschema, problem->{});
        return createValidatingSchemaReader(parser);
    }
    
    @Override
    public JsonSchemaBuilderFactory createSchemaBuilderFactory() {
        return createBasicSchemaBuilderFactory();
    }

    @Override
    public JsonValidatorFactory createValidatorFactory() {
        return createDefaultJsonValidatorFactory();
    }
   
    @Override
    public Consumer<List<Problem>> createProblemPrinter(Consumer<String> lineConsumer) {
        Objects.requireNonNull(lineConsumer, "lineConsumer must not be null.");
        return new ProblemPrinter(lineConsumer);
    }
    
    @Override
    protected void initialize(JsonProvider jsonProvider) {
        Objects.requireNonNull(jsonProvider, "jsonProvider must not be null.");
        this.jsonProvider = jsonProvider;
        this.metaschema = loadMetaschema(METASCHEMA_NAME);
        this.schemaParserFactory = createDefaultJsonValidatorFactory();
    }
    
    @Override
    public JsonSchema resolveSchema(URI id) {
        Objects.requireNonNull(id, "id must not be null.");
        if (id.equals(metaschema.id())) {
            return metaschema;
        } else {
            return null;
        }
    }
    
    private JsonSchema loadMetaschema(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        JsonParser parser = this.jsonProvider.createParser(in);
        try (JsonSchemaReader reader = new BasicSchemaReader(parser, createBasicSchemaBuilderFactory())) {
            return reader.read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @SuppressWarnings("resource")
    private JsonSchemaReader createValidatingSchemaReader(ValidatingJsonParser parser) {
        return new ValidatingSchemaReader(parser, createBasicSchemaBuilderFactory())
                .withSchemaResolver(this);
    }
    
    private DefaultJsonValidatorFactory createDefaultJsonValidatorFactory() {
        return new DefaultJsonValidatorFactory(this.jsonProvider);
    }

    private BasicSchemaBuilderFactory createBasicSchemaBuilderFactory() {
        JsonBuilderFactory builderFactory = this.jsonProvider.createBuilderFactory(null);
        return new BasicSchemaBuilderFactory(builderFactory);
    }
}

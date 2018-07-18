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

package org.leadpony.justify.internal.keyword.assertion;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Assertion specified with "type" validation keyword.
 * 
 * @author leadpony
 */
class Type implements Assertion, Evaluator {
    
    protected final Set<InstanceType> typeSet;
    
    Type(Set<InstanceType> types) {
        this.typeSet = new LinkedHashSet<>(types);
    }
    
    @Override
    public String name() {
        return "type";
    }
    
    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        appender.append(this);
    }

    @Override
    public Assertion negate() {
        return new Negated(this.typeSet);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonArrayBuilder arrayBuilder = builderFactory.createArrayBuilder();
        typeSet.stream()
            .map(InstanceType::name)
            .map(String::toLowerCase)
            .forEach(arrayBuilder::add);
        builder.add("type", arrayBuilder);
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        if (type != null) {
            return testType(type, parser, reporter);
        } else {
            return Result.TRUE;
        }
    }

    protected boolean contains(InstanceType type) {
        return typeSet.contains(type) ||
               (type == InstanceType.INTEGER && typeSet.contains(InstanceType.NUMBER));
    }
    
    protected Result testType(InstanceType type, JsonParser parser, Reporter reporter) {
        if (contains(type)) {
            return Result.TRUE;
        } else {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage("instance.problem.type")
                    .withParameter("actual", type)
                    .withParameter("expected", this.typeSet)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
    
    private static class Negated extends Type {

        Negated(Set<InstanceType> types) {
            super(types);
        }
        
        @Override
        public Assertion negate() {
            return new Type(this.typeSet);
        }
        
        @Override
        protected Result testType(InstanceType type, JsonParser parser, Reporter reporter) {
            if (contains(type)) {
                Problem p = ProblemBuilder.newBuilder(parser)
                        .withMessage("instance.problem.not.type")
                        .withParameter("actual", type)
                        .withParameter("expected", this.typeSet)
                        .build();
                reporter.reportProblem(p);
                return Result.FALSE;
            } else {
                return Result.TRUE;
            }
        }
    }
}
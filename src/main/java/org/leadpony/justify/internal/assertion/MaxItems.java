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

package org.leadpony.justify.internal.assertion;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;

/**
 * Assertion specified with "maxItems" keyword.
 * 
 * @author leadpony
 */
class MaxItems extends AbstractAssertion {

    private final int bound;
    
    public MaxItems(int bound) {
        this.bound = bound;
    }

    @Override
    public String name() {
        return "maxItems";
    }
    
    @Override
    public boolean canApplyTo(InstanceType type) {
        return type == InstanceType.ARRAY;
    }
    
    @Override
    public Evaluator createEvaluator(InstanceType type) {
        assert type == InstanceType.ARRAY;
        return new ItemCountEvaluator();
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write("maxItems", bound);
    }
    
    @Override
    protected AbstractAssertion createNegatedAssertion() {
        return new MinItems(bound + 1);
    }

    private class ItemCountEvaluator implements ShallowEvaluator { 

        private int currentCount;

        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (depth == 1) {
                return testSize(++currentCount, parser, reporter);
            } else if (depth == 0 && event == Event.END_ARRAY) {
                return Result.TRUE;
            } else {
                return Result.PENDING;
            }
        }

        private Result testSize(int size, JsonParser parser, Reporter reporter) {
            if (size <= bound) {
                return Result.PENDING;
            } else {
                Problem p = ProblemBuilder.newBuilder(parser)
                        .withMessage("instance.problem.max.items")
                        .withParameter("actual", size)
                        .withParameter("bound", bound)
                        .build();
                reporter.reportProblem(p);
                return Result.FALSE;
            }
        }
    }
}
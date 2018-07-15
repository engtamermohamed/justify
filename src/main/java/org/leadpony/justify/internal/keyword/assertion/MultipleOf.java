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

import java.math.BigDecimal;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Assertion specified with "multipleOf" validation keyword.
 * 
 * @author leadpony
 */
class MultipleOf extends AbstractNumericAssertion {
    
    protected final BigDecimal divisor;
    
    MultipleOf(BigDecimal divisor) {
        this.divisor = divisor;
    }

    @Override
    public String name() {
        return "multipleOf";
    }
    
    @Override
    public Assertion negate() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), divisor);
    }
    
    @Override
    protected Result evaluateAgainstNumber(BigDecimal value, JsonParser parser, Reporter reporter) {
        BigDecimal remainder = value.remainder(divisor);
        if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            return Result.TRUE;
        } else {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage("instance.problem.multiple.of")
                    .withParameter("actual", value)
                    .withParameter("divisor", divisor)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
}

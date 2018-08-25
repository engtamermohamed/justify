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

package org.leadpony.justify.internal.evaluator;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;

/**
 * @author leadpony
 */
class LongDisjunctiveEvaluator extends DisjunctiveEvaluator {

    private final Event stopEvent;
    
    LongDisjunctiveEvaluator(InstanceType type) {
        this.stopEvent = ParserEvents.lastEventOf(type);
    }
    
    LongDisjunctiveEvaluator(Stream<JsonSchema> children, InstanceType type, boolean affirmative) {
        super(children, type, affirmative);
        this.stopEvent = ParserEvents.lastEventOf(type);
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        Iterator<RetainingEvaluator> it = children.iterator();
        while (it.hasNext()) {
            RetainingEvaluator current = it.next();
            Result result = invokeChildEvaluator(current, event, parser, depth, reporter);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addBadEvaluator(current);
                }
                it.remove();
            }
        }
        if (depth == 0 && event == this.stopEvent) {
            return reportProblems(parser, reporter);
        }
        return Result.PENDING;
    }

    protected Result invokeChildEvaluator(Evaluator evaluator, Event event, JsonParser parser, int depth,
            Consumer<Problem> reporter) {
        return evaluator.evaluate(event, parser, depth, reporter);
    }
}
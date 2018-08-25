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

package org.leadpony.justify.core;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Handler of problems found by a JSON validator.
 * 
 * @author leadpony
 */
@FunctionalInterface
public interface ProblemHandler {

    /**
     * Handles the problems found while validating a JSON document.
     * 
     * @param problems the problems found, cannot be {@code null}.
     */
    void handleProblems(List<Problem> problems);
    
    /**
     * Creates a problem handler which will store problems into the specified collection.
     * 
     * @param collection the collection into which problems will be stored.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the specified {@code collection} was {@code null}.
     */
    static ProblemHandler collectingTo(Collection<Problem> collection) {
        Objects.requireNonNull(collection, "collection must not be null.");
        return collection::addAll;
    }
   
    /**
     * Creates a problem handler which will print problems 
     * with the aid of the specified line consumer.
     * 
     * @param lineConsumer the object which will output the line to somewhere.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the specified {@code lineConsumer} was {@code null}.
     */
    static ProblemHandler printingWith(Consumer<String> lineConsumer) {
        Objects.requireNonNull(lineConsumer, "lineConsumer must not be null.");
        return problems->problems.forEach(p->p.printAll(lineConsumer));
    }
}
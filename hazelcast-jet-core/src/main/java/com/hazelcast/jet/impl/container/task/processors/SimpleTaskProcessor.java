/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.impl.container.task.processors;

import com.hazelcast.jet.container.ProcessorContext;
import com.hazelcast.jet.data.io.ProducerInputStream;
import com.hazelcast.jet.impl.container.task.TaskProcessor;
import com.hazelcast.jet.impl.data.io.ObjectIOStream;
import com.hazelcast.jet.processor.ContainerProcessor;

import static com.hazelcast.util.Preconditions.checkNotNull;

public class SimpleTaskProcessor implements TaskProcessor {
    private static final Object[] DUMMY_CHUNK = new Object[0];

    protected boolean finalizationStarted;
    protected boolean producersWriteFinished;
    private final ContainerProcessor processor;
    private final ObjectIOStream pairInputStream;
    private final ObjectIOStream pairOutputStream;
    private boolean finalized;
    private final ProcessorContext processorContext;

    public SimpleTaskProcessor(ContainerProcessor processor,
                               ProcessorContext processorContext) {
        checkNotNull(processor);
        this.processor = processor;
        this.processorContext = processorContext;
        this.pairInputStream = new ObjectIOStream<>(DUMMY_CHUNK);
        this.pairOutputStream = new ObjectIOStream<>(DUMMY_CHUNK);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean process() throws Exception {
        if (!finalizationStarted) {
            if (producersWriteFinished) {
                return true;
            }
            processor.process(pairInputStream, pairOutputStream, null, processorContext);
            return true;
        } else {
            finalized = processor.finalizeProcessor(pairOutputStream, processorContext);
            return true;
        }
    }

    @Override
    public boolean isFinalized() {
        return finalized;
    }

    @Override
    public void reset() {
        finalized = false;
        pairInputStream.reset();
        pairOutputStream.reset();
        finalizationStarted = false;
        producersWriteFinished = false;
    }

    @Override
    public void startFinalization() {
        finalizationStarted = true;
    }

    @Override
    public void onProducersWriteFinished() {
        producersWriteFinished = true;
    }

    @Override
    public void onReceiversClosed() {

    }

    @Override
    public boolean producersReadFinished() {
        return true;
    }

    @Override
    public boolean onChunk(ProducerInputStream pairOutputStream) throws Exception {
        return true;
    }

    @Override
    public boolean produced() {
        return false;
    }

    @Override
    public boolean consumed() {
        return false;
    }

    @Override
    public void onOpen() {
        reset();
    }

    @Override
    public void onClose() {

    }
}

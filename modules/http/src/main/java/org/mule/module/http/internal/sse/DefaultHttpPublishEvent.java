/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.http.internal.sse;

import static org.mule.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import org.mule.AbstractAnnotatedObject;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHttpPublishEvent extends AbstractAnnotatedObject implements Initialisable, MessageProcessor
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpPublishEvent.class);

    private DefaultHttpEventPublisherConfig streamPublisher;
    private HttpSseEventBuilder eventBuilder;
    
    @Override
    public void initialise() throws InitialisationException
    {
        initialiseIfNeeded(eventBuilder);
    }
    
    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        if(eventBuilder != null)
        {
            streamPublisher.publishEvent(eventBuilder.evaluateEventType(event), eventBuilder.evaluateData(event));
        }
        else 
        {
            streamPublisher.publishEvent(null, event.getMessageAsString());
        }
        return event;
    }

    public DefaultHttpEventPublisherConfig getStreamPublisher()
    {
        return streamPublisher;
    }
    
    public void setStreamPublisher(DefaultHttpEventPublisherConfig streamPublisher)
    {
        this.streamPublisher = streamPublisher;
    }
    
    public HttpSseEventBuilder getEventBuilder()
    {
        return eventBuilder;
    }
    
    public void setEventBuilder(HttpSseEventBuilder eventBuilder)
    {
        this.eventBuilder = eventBuilder;
    }
}
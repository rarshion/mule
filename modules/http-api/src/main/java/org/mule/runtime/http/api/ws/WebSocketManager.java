/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.http.api.ws;

import org.mule.api.annotation.Experimental;

import java.util.Optional;
import java.util.function.Consumer;

@Experimental
public interface WebSocketManager {

  Optional<WebSocket> getWebSocket(String socketId);

  FragmentHandler getFragmentHandler(String socketId, Consumer<FragmentHandler> newHandlerConsumer);
}

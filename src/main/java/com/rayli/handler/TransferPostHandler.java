package com.rayli.handler;

import com.networknt.handler.LightHttpHandler;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company Prive Financial
 */
public class TransferPostHandler implements LightHttpHandler {

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
		exchange.getResponseSender().send(" {}");
	}

}

package com.rayli.handler;

import java.math.BigDecimal;

import com.networknt.handler.LightHttpHandler;
import com.networknt.service.SingletonServiceFactory;
import com.rayli.service.BalanceService;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company Prive Financial
 */
public class BalanceGetHandler implements LightHttpHandler {

	private BalanceService balanceService;

	public BalanceGetHandler() {
		this.balanceService = SingletonServiceFactory.getBean(BalanceService.class);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		BigDecimal balance = balanceService.getBalance("test");
		exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
		exchange.getResponseSender().send(String.format("{\"balance\": %f}", balance.doubleValue()));
	}

}

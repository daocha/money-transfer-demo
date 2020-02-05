package com.rayli.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.service.SingletonServiceFactory;
import com.rayli.model.Account;
import com.rayli.service.TransactionService;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public class AccountGetHandler implements LightHttpHandler {

	private TransactionService transactionService = SingletonServiceFactory.getBean(TransactionService.class);

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Account account = transactionService.getAccount(exchange.getQueryParameters().get("accountId").element());
		exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
		if (account != null) {
			String json = (new ObjectMapper()).writeValueAsString(account);
			exchange.getResponseSender().send(json);
		} else {
			exchange.setStatusCode(404);
		}

	}

}

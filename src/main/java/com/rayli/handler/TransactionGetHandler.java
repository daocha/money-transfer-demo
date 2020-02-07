package com.rayli.handler;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.service.SingletonServiceFactory;
import com.rayli.model.Transaction;
import com.rayli.service.TransactionService;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * API GET /transaction
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public class TransactionGetHandler implements LightHttpHandler {

	private TransactionService transactionService = SingletonServiceFactory.getBean(TransactionService.class);

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		List<Transaction> transactions = transactionService
				.getTransactions(exchange.getQueryParameters().get("accountId").element());

		exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");

		if (transactions != null) {
			String json = (new ObjectMapper()).writeValueAsString(transactions);
			exchange.getResponseSender().send(json);
		}
	}

}

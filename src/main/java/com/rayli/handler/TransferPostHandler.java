package com.rayli.handler;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.body.BodyHandler;
import com.networknt.handler.LightHttpHandler;
import com.networknt.service.SingletonServiceFactory;
import com.rayli.exception.ServerException;
import com.rayli.model.Response;
import com.rayli.service.TransactionService;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * API POST /transfer
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public class TransferPostHandler implements LightHttpHandler {

	private final static Logger log = Logger.getLogger(TransferPostHandler.class.getName());

	private TransactionService transactionService = SingletonServiceFactory.getBean(TransactionService.class);

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Object bodyunknown = exchange.getAttachment(BodyHandler.REQUEST_BODY);

		if (bodyunknown instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> body = (Map<String, Object>) bodyunknown;

			final String creditAccountId = (String) body.get("creditAccountId");
			final String debitAccountId = (String) body.get("debitAccountId");
			final String currency = (String) body.get("currency");
			final BigDecimal amount = new BigDecimal(body.get("amount").toString());

			Response res = new Response();
			try {
				boolean success = transactionService.transfer(creditAccountId, debitAccountId, currency, amount);
				res.setSuccess(success);
				if (!success) {
					res.setError("Failed to transfer, please retry ...");
				}
			} catch (ServerException e) {
				log.severe(e.getErrorMsg());
				res.setSuccess(false);
				res.setError(e.getErrorMsg());
			}

			exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");

			String json = (new ObjectMapper()).writeValueAsString(res);
			exchange.getResponseSender().send(json);
		} else {
			exchange.setStatusCode(400);
		}
	}

}

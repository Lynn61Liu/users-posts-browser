package com.example.userspostsbrowser.transactions;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
class TransactionController {

	private final TransactionService transactionService;

	TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@GetMapping("/config")
	DynamoDbConfigResponse getConfig() {
		return transactionService.getConfig();
	}

	@PostMapping("/demo")
	DemoTransactionResponse writeDemoTransaction() {
		return transactionService.writeDemoTransaction();
	}
}

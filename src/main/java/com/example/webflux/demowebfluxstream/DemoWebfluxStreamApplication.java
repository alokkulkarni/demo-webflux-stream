package com.example.webflux.demowebfluxstream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@SpringBootApplication
public class DemoWebfluxStreamApplication {
	

	List<Product> transactionList = new ArrayList<>();
	List<String> productNames = Arrays.asList("mango,banana,guava,apples".split(","));

	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			createRandomProduct();
			transactionList.forEach(System.out::println);
		};
	}

	@RestController
	@RequestMapping("/transaction")
	class StockTransactionController {

		@Autowired
		TransactionService transactionService;

		@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
		public Flux<Transaction> stockTransactionEvents(){
			return transactionService.getStockTransactions();
		}
	}

	@Service
	class TransactionService {
		Flux<Transaction> getStockTransactions() {
			Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
			interval.subscribe((i) -> transactionList.forEach(p -> p.setPrice(changePrice(p.getPrice()))));

			Flux<Transaction> transactionFlux = Flux.fromStream(Stream.generate(() -> new Transaction(getRandomUser(), getRandomProduct(), new Date())));
			return Flux.zip(interval, transactionFlux).map(Tuple2::getT2);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	class Product {
		String name;
		float price;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	class Transaction {
		String user;
		Product product;
		Date when;
	}

	void createRandomProduct() {
		productNames.forEach(productName -> {
			transactionList.add(new Product(productName, generateRandomStockPrice()));
		});
	}

	float generateRandomStockPrice() {
		float min = 10;
		float max = 100;
		return min + roundFloat(new Random().nextFloat() * (max - min));
	}

	float changePrice(float price) {
		return roundFloat(Math.random() >= 0.6 ? price * 1.20f : price * 0.85f);
	}

	String getRandomUser() {
		String users[] = "Bael,Josh,Michael,Steve,Simon,Anne,Sarah,Scarlet".split(",");
		return users[new Random().nextInt(users.length)];
	}

	Product getRandomProduct() {
		return transactionList.get(new Random().nextInt(transactionList.size()));
	}

	float roundFloat(float number) {
		return Math.round(number * 100.0) / 100.0f;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(DemoWebfluxStreamApplication.class, args);
	}
}

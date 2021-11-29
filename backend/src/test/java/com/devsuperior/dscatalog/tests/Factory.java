package com.devsuperior.dscatalog.tests;

import java.time.Instant;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

public class Factory {

	public static Product createProduct() {
		Product product = new Product(1L, "Phone", "Good Phone", 800.00, "https://img.com/img.png",
				Instant.parse("2021-11-23T03:00:00Z"));
		product.getCategories().add(createCategory());
		return product;
	}

	public static ProductDTO createProductDTO() {
		return new ProductDTO(createProduct());
	}

	public static Category createCategory() {
		
		return new Category(2L, "Eletrônicos");
	}
	
	
}

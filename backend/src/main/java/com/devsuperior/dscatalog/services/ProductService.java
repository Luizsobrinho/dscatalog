package com.devsuperior.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;

	@Autowired
	private CategoryRepository productyRepository;

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable) {
		Page<Product> categoriesDTO = repository.findAll(pageable);
		return categoriesDTO.map(x -> new ProductDTO(x));
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> optional = repository.findById(id);
		Product entity = optional.orElseThrow(() -> new ResourceNotFoundException("Produto não encontrada"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO productDTO) {

		Product producty = new Product();
		copyDtoToEntity(productDTO, producty);

		producty = repository.save(producty);
		return new ProductDTO(producty);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO productDTO) {
		try {
			Product producty = repository.getOne(id);
			copyDtoToEntity(productDTO, producty);
			producty = repository.save(producty);
			return new ProductDTO(producty);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found: " + id);
		}

	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found: " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}

	}

	private void copyDtoToEntity(ProductDTO productyDTO, Product product) {
		product.setName(productyDTO.getName());
		product.setDescription(productyDTO.getDescription());
		product.setDate(productyDTO.getDate());
		product.setImgUrl(productyDTO.getImgUrl());
		product.setPrice(productyDTO.getPrice());

		product.getCategories().clear();
		for (CategoryDTO catDTO : productyDTO.getCategories()) {
			Category producty = productyRepository.getOne(catDTO.getId());
			product.getCategories().add(producty);
		}
	}
}

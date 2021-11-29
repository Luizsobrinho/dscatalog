package com.devsuperior.dscatalog.resources;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
class ProductResourcesTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private long existingId;
	
	private long nonExistingId;
	
	private Long dependentID;
	private ProductDTO productDTO;
	
	private PageImpl<ProductDTO> page;
	
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentID = 3L;
		
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));
		
		when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
		when(service.findById(existingId)).thenReturn(productDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		when(service.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any())).thenReturn(productDTO);
		when(service.update(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any())).thenThrow(ResourceNotFoundException.class);
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		
		doThrow(DatabaseException.class).when(service).delete(dependentID);
		
		when(service.insert(ArgumentMatchers.any())).thenReturn(productDTO);
	}
	
	@Test
	void findAllshouldReturnPage() throws Exception {
		ResultActions resultActions = mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isOk());
	}
	
	@Test
	void findByIdShouldReturnProductWhenExistsId() throws Exception {
		ResultActions resultActions = mockMvc
				.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isOk());
		resultActions.andExpect(jsonPath("$.id").exists());
		resultActions.andExpect(jsonPath("$.name").exists());
		resultActions.andExpect(jsonPath("$.description").exists());
	}

	@Test
	void findByIdShouldReturnNotFoundProductWhenDoesNotExistsId() throws Exception {
		ResultActions resultActions = mockMvc
				.perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isNotFound());
	}

	@Test
	void updateShouldReturnProdctDTOWhenExistsId() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		ResultActions resultActions = mockMvc.perform(put("/products/{id}", existingId)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		
		resultActions.andExpect(status().isOk());
		resultActions.andExpect(jsonPath("$.id").exists());
		resultActions.andExpect(jsonPath("$.name").exists());
		resultActions.andExpect(jsonPath("$.description").exists());

	}

	@Test
	void updateShouldReturnNotFoundWhenIdDoesNotExistsId() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		ResultActions resultActions = mockMvc.perform(put("/products/{id}", nonExistingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isNotFound());

	}
	
	@Test
	void deleteShouldReturnNotFoundWhenIdDoesNotExistsId() throws Exception {

		ResultActions resultActions = mockMvc.perform(delete("/products/{id}", nonExistingId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isNotFound());

	}
	
	@Test
	void deleteShouldReturnBadRequestWhenIsDependetID() throws Exception {

		ResultActions resultActions = mockMvc.perform(delete("/products/{id}", dependentID)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isBadRequest());

	}
	
	@Test
	void deleteShouldReturnNoContentWhenIdExists() throws Exception {

		ResultActions resultActions = mockMvc.perform(delete("/products/{id}", existingId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		resultActions.andExpect(status().isNoContent());

	}
	
	@Test
	void insertShouldReturnProdctDTOAndCreated() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		ResultActions resultActions = mockMvc.perform(post("/products")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		
		resultActions.andExpect(status().isCreated());
		resultActions.andExpect(jsonPath("$.id").exists());
		resultActions.andExpect(jsonPath("$.name").exists());
		resultActions.andExpect(jsonPath("$.description").exists());

	}
}

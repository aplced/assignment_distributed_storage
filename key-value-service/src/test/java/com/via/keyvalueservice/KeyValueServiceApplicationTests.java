package com.via.keyvalueservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureDataMongo
class KeyValueServiceApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void givenSetKeyValue_whenGetKey_thenReturnValue() throws Exception {
		String key = "Ozymandias";
		String value = "Veidt";
		mockMvc.perform(get("/set?k=" + key + "&v=" + value)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/get?k=" + key)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		assertEquals(value, result.getResponse().getContentAsString());
	}

	@Test
	void givenKeyDoesNotExist_whenRmKey_thenReturnNotFound() throws Exception {
		String key = "Rorschach";
		mockMvc.perform(get("/rm?k=" + key)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	void givenKeyWithOVer64Chars_whenSetKey_thenReturn() throws Exception {
		String key = "RorschachWalterJosephKovacsis5p6tallandweighs140poundsButThisStillNeedsMore";
		String value = "Kovacs";

		//Because the KeyNotFoundAdvice is not handling the exception idk why,
		//a spring NestedServletException gets thrown here with a cause the proper
		//ConstrainValidationException - hence this test is checking that its cause is set and with proper message
		Exception constrainVioliation = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/set?k=" + key + "&v=" + value)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn();
		});

		assertNotNull(constrainVioliation.getCause());
		assertEquals("set.key: size must be between 1 and 64", constrainVioliation.getCause().getMessage());
	}
}

package com.library.config;

import com.library.customer.controller.CustomerController;
import com.library.customer.service.CustomerService;
import com.library.employee.controller.EmployeeController;
import com.library.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real filter chain + Keycloak role converter: tokens are decoded by a mocked
 * JwtDecoder into Keycloak-shaped claims (realm_access.roles).
 */
@WebMvcTest(controllers = {EmployeeController.class, CustomerController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    private static final String EMPLOYEE_JSON = """
            {"name":"Dara","identityCardNo":"EMP-001"}""";
    private static final String CUSTOMER_JSON = """
            {"name":"Sok","customerType":"STUDENT","identityCardNo":"CUS-001"}""";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private EmployeeService employeeService;
    @MockitoBean private CustomerService customerService;

    @BeforeEach
    void tokens() {
        when(jwtDecoder.decode("admin-token")).thenReturn(jwt("admin", "ADMIN", "LIBRARIAN"));
        when(jwtDecoder.decode("librarian-token")).thenReturn(jwt("librarian", "LIBRARIAN"));
        when(jwtDecoder.decode("bad-token")).thenThrow(new BadJwtException("bad"));
    }

    private static Jwt jwt(String username, String... roles) {
        return Jwt.withTokenValue(username + "-token")
                .header("alg", "RS256")
                .claim("preferred_username", username)
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    private static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder req, String token, String body) {
        return req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    @Test
    void adminCanCreateEmployee() throws Exception {
        mockMvc.perform(as(post("/api/v1/employees"), "admin-token", EMPLOYEE_JSON))
                .andExpect(status().isCreated());
        verify(employeeService).create(any());
    }

    @Test
    void adminCanCreateCustomer() throws Exception {
        mockMvc.perform(as(post("/api/v1/customers"), "admin-token", CUSTOMER_JSON))
                .andExpect(status().isCreated());
        verify(customerService).create(any());
    }

    @Test
    void librarianCannotCreateEmployee() throws Exception {
        mockMvc.perform(as(post("/api/v1/employees"), "librarian-token", EMPLOYEE_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action"));
        verify(employeeService, never()).create(any());
    }

    @Test
    void librarianCannotCreateCustomer() throws Exception {
        mockMvc.perform(as(post("/api/v1/customers"), "librarian-token", CUSTOMER_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        verify(customerService, never()).create(any());
    }

    @Test
    void librarianCanStillRead() throws Exception {
        mockMvc.perform(get("/api/v1/customers").header(HttpHeaders.AUTHORIZATION, "Bearer librarian-token"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/employees/1").header(HttpHeaders.AUTHORIZATION, "Bearer librarian-token"))
                .andExpect(status().isOk());
    }

    @Test
    void librarianCannotUpdateOrDelete() throws Exception {
        mockMvc.perform(as(put("/api/v1/employees/1"), "librarian-token", EMPLOYEE_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(put("/api/v1/customers/1"), "librarian-token", CUSTOMER_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/employees/1").header(HttpHeaders.AUTHORIZATION, "Bearer librarian-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(delete("/api/v1/customers/1").header(HttpHeaders.AUTHORIZATION, "Bearer librarian-token"))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).update(any(), any());
        verify(customerService, never()).update(any(), any());
        verify(employeeService, never()).delete(any());
        verify(customerService, never()).delete(any());
    }

    @Test
    void adminCanUpdateAndDelete() throws Exception {
        mockMvc.perform(as(put("/api/v1/employees/1"), "admin-token", EMPLOYEE_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(as(put("/api/v1/customers/1"), "admin-token", CUSTOMER_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/employees/1").header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/customers/1").header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk());
        verify(employeeService).update(any(), any());
        verify(customerService).update(any(), any());
        verify(employeeService).delete(1L);
        verify(customerService).delete(1L);
    }

    @Test
    void missingTokenIsUnauthorizedWithJsonBody() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void invalidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(as(post("/api/v1/employees"), "bad-token", EMPLOYEE_JSON))
                .andExpect(status().isUnauthorized());
    }
}

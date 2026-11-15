package com.supportdesk.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.security.JwtUtil;
import com.supportdesk.service.TicketService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTests
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp()
    {
        Employee admin = new Employee("Admin Dash", "admin.dash@test.com", "password123", "IT", Role.ADMIN, "111");
        employeeRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    @DisplayName("GET /api/dashboard/summary - Returns 200 OK with all ticket status counts")
    void testGetDashboardSummaryApi() throws Exception
    {
        Employee creator = new Employee("Dash Creator", "dash.creator@test.com", "password123", "IT", Role.EMPLOYEE, "111");
        employeeRepository.save(creator);

        ticketService.createTicket(new CreateTicketRequest("Dash Issue 1", "Desc", "SW", TicketPriority.LOW, creator.getId()));
        ticketService.createTicket(new CreateTicketRequest("Dash Issue 2", "Desc", "HW", TicketPriority.HIGH, creator.getId()));

        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.openTickets").exists())
                .andExpect(jsonPath("$.assignedTickets").exists())
                .andExpect(jsonPath("$.inProgressTickets").exists())
                .andExpect(jsonPath("$.resolvedTickets").exists())
                .andExpect(jsonPath("$.closedTickets").exists())
                .andExpect(jsonPath("$.overdueTickets").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/priority-summary - Returns 200 OK with counts per priority")
    void testGetPrioritySummaryApi() throws Exception
    {
        Employee creator = new Employee("Dash Creator 2", "dash.creator2@test.com", "password123", "IT", Role.EMPLOYEE, "222");
        employeeRepository.save(creator);

        ticketService.createTicket(new CreateTicketRequest("Critical DB", "DB down", "Software", TicketPriority.CRITICAL, creator.getId()));

        mockMvc.perform(get("/api/dashboard/priority-summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.critical", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.high").exists())
                .andExpect(jsonPath("$.medium").exists())
                .andExpect(jsonPath("$.low").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/technician-workload - Returns active technician workload metrics")
    void testGetTechnicianWorkloadApi() throws Exception
    {
        Employee tech = new Employee("Dash Tech", "dash.tech@test.com", "password123", "IT", Role.TECHNICIAN, "333");
        employeeRepository.save(tech);

        mockMvc.perform(get("/api/dashboard/technician-workload")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/reports - Returns date range report")
    void testGetDateRangeReportApi() throws Exception
    {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/dashboard/reports")
                .header("Authorization", "Bearer " + adminToken)
                .param("startDate", start.toString())
                .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate", is(start.toString())))
                .andExpect(jsonPath("$.endDate", is(end.toString())))
                .andExpect(jsonPath("$.totalCreated").exists())
                .andExpect(jsonPath("$.totalResolved").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/reports - Returns 400 Bad Request when startDate is after endDate")
    void testGetDateRangeReportInvalidDates() throws Exception
    {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now();

        mockMvc.perform(get("/api/dashboard/reports")
                .header("Authorization", "Bearer " + adminToken)
                .param("startDate", start.toString())
                .param("endDate", end.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("cannot be after endDate")));
    }
}

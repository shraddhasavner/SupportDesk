package com.supportdesk.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.ActivityType;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketActivity;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketActivityRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.service.EmployeeService;
import com.supportdesk.service.TicketService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class HighPriorityFixesIntegrationTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketActivityRepository activityRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Employee admin1;
    private Employee admin2;
    private Employee tech1;
    private Employee staff1;

    private String admin1Token;
    private String admin2Token;
    private String tech1Token;

    @BeforeEach
    void setUp()
    {
        admin1 = new Employee("Admin One", "admin.one@audit-fix.com", passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "111");
        admin1 = employeeRepository.save(admin1);
        admin1Token = jwtUtil.generateToken(admin1);

        admin2 = new Employee("Admin Two", "admin.two@audit-fix.com", passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "222");
        admin2 = employeeRepository.save(admin2);
        admin2Token = jwtUtil.generateToken(admin2);

        tech1 = new Employee("Tech Worker", "tech.worker@audit-fix.com", passwordEncoder.encode("Pass123"), "Support", Role.TECHNICIAN, "333");
        tech1 = employeeRepository.save(tech1);
        tech1Token = jwtUtil.generateToken(tech1);

        staff1 = new Employee("Staff Reporter", "staff.reporter@audit-fix.com", passwordEncoder.encode("Pass123"), "Ops", Role.EMPLOYEE, "444");
        staff1 = employeeRepository.save(staff1);
    }

    @Test
    @DisplayName("Fix 1 - JwtUtil rejects short secret keys under 32 characters")
    void testJwtSecretValidation()
    {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("short", 3600000));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil(null, 3600000));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("        ", 3600000));
    }

    @Test
    @DisplayName("Fix 1 - JwtUtil accepts valid 256-bit secret key")
    void testJwtSecretValidLength()
    {
        JwtUtil customJwt = new JwtUtil("ThisIsAValid32CharKeyForTesting2026!", 3600000);
        String token = customJwt.generateToken(admin1);
        assertNotNull(token);
        assertEquals("admin.one@audit-fix.com", customJwt.getEmailFromToken(token));
    }

    @Test
    @DisplayName("Fix 2 - Status change via Admin attributes activity to Admin, not technician")
    void testStatusChangeAttributedToAuthenticatedAdmin() throws Exception
    {
        Ticket ticket = new Ticket("TCK-AUDIT-001", "Server Issue", "Email server unresponsive", "Hardware", TicketPriority.HIGH, staff1);
        ticket.setAssignedTechnician(tech1);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket = ticketRepository.save(ticket);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status")
                        .param("status", "RESOLVED")
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));

        List<TicketActivity> activities = activityRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        TicketActivity statusActivity = activities.stream()
                .filter(a -> a.getActivityType() == ActivityType.STATUS_CHANGED)
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(statusActivity, "Status changed activity must be recorded");
        assertNotNull(statusActivity.getPerformedBy(), "Activity must record an actor");
        assertEquals(admin1.getId(), statusActivity.getPerformedBy().getId(),
                "Activity must be attributed to the authenticated Admin who updated it, NOT the technician");
        assertEquals("admin.one@audit-fix.com", statusActivity.getPerformedBy().getEmail());
    }

    @Test
    @DisplayName("Fix 2 - Resolution notes via Admin attributes activity to Admin, not technician")
    void testResolutionNotesAttributedToAuthenticatedAdmin() throws Exception
    {
        Ticket ticket = new Ticket("TCK-AUDIT-002", "Network Glitch", "Switch restart required", "Network", TicketPriority.MEDIUM, staff1);
        ticket.setAssignedTechnician(tech1);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket = ticketRepository.save(ticket);

        String resolvePayload = """
                {
                    "resolutionNotes": "Restarted core switch at rack 4",
                    "markResolved": true
                }
                """;

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + admin1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolvePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolutionNotes", is("Restarted core switch at rack 4")));

        List<TicketActivity> activities = activityRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        TicketActivity resolveActivity = activities.stream()
                .filter(a -> a.getActivityType() == ActivityType.TICKET_RESOLVED)
                .findFirst()
                .orElse(null);

        assertNotNull(resolveActivity, "Resolution activity must be recorded");
        assertEquals(admin1.getId(), resolveActivity.getPerformedBy().getId(),
                "Activity must be attributed to Admin who resolved it, NOT the assigned technician");
    }

    @Test
    @DisplayName("Fix 2 - Technician status update attributes activity to Technician")
    void testStatusChangeAttributedToTechnician() throws Exception
    {
        Ticket ticket = new Ticket("TCK-AUDIT-003", "Mouse Issue", "Mouse replaced", "Hardware", TicketPriority.LOW, staff1);
        ticket.setAssignedTechnician(tech1);
        ticket.setStatus(TicketStatus.OPEN);
        ticket = ticketRepository.save(ticket);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status")
                        .param("status", "IN_PROGRESS")
                        .header("Authorization", "Bearer " + tech1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        List<TicketActivity> activities = activityRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        TicketActivity statusActivity = activities.stream()
                .filter(a -> a.getActivityType() == ActivityType.STATUS_CHANGED)
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(statusActivity);
        assertEquals(tech1.getId(), statusActivity.getPerformedBy().getId(),
                "Activity must be attributed to technician who updated the status");
    }

    @Test
    @DisplayName("Fix 3 - Admin cannot deactivate their own account via PATCH /api/employees/{id}/deactivate")
    void testAdminCannotDeactivateOwnAccount() throws Exception
    {
        mockMvc.perform(patch("/api/employees/" + admin1.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Administrators cannot deactivate their own account.")));

        Employee reloaded = employeeRepository.findById(admin1.getId()).orElseThrow();
        assertTrue(reloaded.isActive(), "Admin account must remain active");
    }

    @Test
    @DisplayName("Fix 3 - Admin cannot soft-delete their own account via DELETE /api/employees/{id}")
    void testAdminCannotDeleteOwnAccount() throws Exception
    {
        mockMvc.perform(delete("/api/employees/" + admin1.getId())
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Administrators cannot deactivate their own account.")));

        Employee reloaded = employeeRepository.findById(admin1.getId()).orElseThrow();
        assertTrue(reloaded.isActive(), "Admin account must remain active");
    }

    @Test
    @DisplayName("Fix 3 - Admin cannot demote their own account from ADMIN role via PUT")
    void testAdminCannotDemoteOwnAccount() throws Exception
    {
        String demotePayload = """
                {
                    "name": "Admin One",
                    "email": "admin.one@audit-fix.com",
                    "password": "Pass123",
                    "department": "Support",
                    "role": "EMPLOYEE",
                    "phone": "111"
                }
                """;

        mockMvc.perform(put("/api/employees/" + admin1.getId())
                        .header("Authorization", "Bearer " + admin1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(demotePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Administrators cannot demote their own account from the ADMIN role.")));

        Employee reloaded = employeeRepository.findById(admin1.getId()).orElseThrow();
        assertEquals(Role.ADMIN, reloaded.getRole(), "Role must remain ADMIN");
    }

    @Test
    @DisplayName("Fix 3 - Cannot deactivate the last active administrator account")
    void testCannotDeactivateLastActiveAdmin() throws Exception
    {
        mockMvc.perform(patch("/api/employees/" + admin2.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        List<Employee> allAdmins = employeeRepository.findByRole(Role.ADMIN);
        for (Employee a : allAdmins)
        {
            if (!a.getId().equals(admin1.getId()) && a.isActive())
            {
                a.setActive(false);
                employeeRepository.save(a);
            }
        }

        List<Employee> activeAdmins = employeeRepository.findActiveEmployeesByRoleForUpdate(Role.ADMIN);
        assertEquals(1, activeAdmins.size(), "There should be exactly one active admin");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
        {
            employeeService.deactivateEmployee(admin1.getId());
        });

        assertTrue(ex.getMessage().contains("Cannot deactivate the last active administrator account."));

        Employee stillActive = employeeRepository.findById(admin1.getId()).orElseThrow();
        assertTrue(stillActive.isActive());
    }

    @Test
    @DisplayName("Fix 3 - Admin can deactivate another admin when multiple active admins exist")
    void testAdminCanDeactivateOtherAdminWhenMultipleExist() throws Exception
    {
        mockMvc.perform(patch("/api/employees/" + admin2.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        Employee deactivated = employeeRepository.findById(admin2.getId()).orElseThrow();
        assertFalse(deactivated.isActive(), "Admin2 must now be deactivated");
    }
}
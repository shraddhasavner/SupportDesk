package com.supportdesk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketRepositoryTests
{
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Should save ticket and find by ticket number")
    void testSaveAndFindByTicketNumber()
    {
        Employee creator = new Employee("Anil Kumar", "anil@supportdesk.com", "password123", "IT", Role.EMPLOYEE, "12345");
        employeeRepository.save(creator);

        Ticket ticket = new Ticket("TCK-20261014-TEST01", "VPN not connecting", "Unable to connect from home", "Network", TicketPriority.HIGH, creator);
        ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findByTicketNumber("TCK-20261014-TEST01");
        assertTrue(found.isPresent());
        assertEquals("VPN not connecting", found.get().getTitle());
        assertEquals(TicketStatus.OPEN, found.get().getStatus());
        assertEquals(TicketPriority.HIGH, found.get().getPriority());
        assertEquals("Anil Kumar", found.get().getEmployee().getName());
    }

    @Test
    @DisplayName("Should filter tickets by status and priority")
    void testFilterByStatusAndPriority()
    {
        Employee emp = new Employee("Sunita Roy", "sunita@supportdesk.com", "password123", "HR", Role.EMPLOYEE, "54321");
        employeeRepository.save(emp);

        Ticket t1 = new Ticket("TCK-20261014-FILT01", "Laptop blue screen", "BSOD crash", "Hardware", TicketPriority.CRITICAL, emp);
        Ticket t2 = new Ticket("TCK-20261014-FILT02", "Mouse cursor lag", "Laggy USB mouse", "Hardware", TicketPriority.LOW, emp);

        ticketRepository.save(t1);
        ticketRepository.save(t2);

        List<Ticket> criticalTickets = ticketRepository.findByPriority(TicketPriority.CRITICAL);
        assertTrue(criticalTickets.stream().anyMatch(t -> t.getTicketNumber().equals("TCK-20261014-FILT01")));

        List<Ticket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
        assertTrue(openTickets.size() >= 2);
    }
}
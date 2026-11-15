/**
 * SupportDesk - Main Application Controller
 * Phase 7.4: Technician Assignment & Ticket Status Updates UI
 * 
 * Manages:
 * 1. Same-page view switching (Login View vs. Dashboard View with zero localStorage/sessionStorage).
 * 2. Real JWT authentication on POST /api/auth/login.
 * 3. Dynamic Dashboard metrics (GET /api/dashboard/summary).
 * 4. Dynamic Tickets table with status/priority filters (GET /api/tickets).
 * 5. Ticket Creation Modal (POST /api/tickets).
 * 6. Ticket Details View (GET /api/tickets/{id}).
 * 7. Phase 7.4: Technician Assignment (PATCH /api/tickets/{id}/assign/{technicianId} for ADMIN).
 * 8. Phase 7.4: Status Transitions (PATCH /api/tickets/{id}/status for ADMIN & TECHNICIAN).
 * 9. Phase 7.4: Resolution Notes (PATCH /api/tickets/{id}/resolve for ADMIN & TECHNICIAN).
 * 10. Ticket Comments Listing & Posting (GET & POST /api/tickets/{id}/comments).
 * 11. Ticket Activity Audit Timeline (GET /api/tickets/{id}/activities).
 */

const App = (function() {
    let currentDetailTicketId = null;
    let cachedTechnicians = null;
    let cachedEmployees = [];
    let notifications = [];
    let notificationPollingInterval = null;
    let allLoadedTickets = [];

    // ==================== View State Management ====================

    function showLoginView(alertMessage) {
        const loginView = document.getElementById('loginView');
        const dashboardView = document.getElementById('dashboardView');
        const loginAlert = document.getElementById('loginAlertBox');
        const loginAlertMsg = document.getElementById('loginAlertMessage');
        const loginForm = document.getElementById('loginForm');
        const emailInput = document.getElementById('emailInput');
        const passwordInput = document.getElementById('passwordInput');

        if (dashboardView) dashboardView.style.display = 'none';
        if (loginView) loginView.style.display = 'flex';

        if (loginForm) loginForm.reset();
        if (emailInput) emailInput.value = '';
        if (passwordInput) passwordInput.value = '';

        if (loginAlert && alertMessage) {
            loginAlert.style.display = 'flex';
            if (loginAlertMsg) loginAlertMsg.textContent = alertMessage;
        } else if (loginAlert) {
            loginAlert.style.display = 'none';
        }

        closeAllModals();
        cachedTechnicians = null;
        cachedEmployees = [];
        notifications = [];

        if (notificationPollingInterval) {
            clearInterval(notificationPollingInterval);
            notificationPollingInterval = null;
        }
        toggleNotificationDropdown(false);
    }

    function showDashboardView() {
        const loginView = document.getElementById('loginView');
        const dashboardView = document.getElementById('dashboardView');

        if (loginView) loginView.style.display = 'none';
        if (dashboardView) dashboardView.style.display = 'flex';

        initUserDisplay();
        showDashboardSection();

        // Phase 7.6: Load notifications count & start light background polling (30s)
        loadUnreadCount();
        if (!notificationPollingInterval) {
            notificationPollingInterval = setInterval(loadUnreadCount, 30000);
        }
    }

    function showDashboardSection() {
        const dashboardSection = document.getElementById('dashboardSectionView');
        const ticketsSection = document.getElementById('ticketsSectionView');
        const employeesSection = document.getElementById('employeesSectionView');
        const breadcrumb = document.getElementById('breadcrumbCurrentPage');
        const navDashboard = document.getElementById('navDashboard');
        const navTickets = document.getElementById('navTickets');
        const sidebarEmployeesBtn = document.getElementById('sidebarEmployeesBtn');

        if (dashboardSection) dashboardSection.style.display = 'block';
        if (ticketsSection) ticketsSection.style.display = 'none';
        if (employeesSection) employeesSection.style.display = 'none';
        if (breadcrumb) breadcrumb.textContent = 'IT Help Desk Dashboard';

        if (navDashboard) navDashboard.classList.add('active');
        if (navTickets) navTickets.classList.remove('active');
        if (sidebarEmployeesBtn) sidebarEmployeesBtn.classList.remove('active');

        loadDashboardSummary();
        loadTickets();
    }

    function showTicketsSection() {
        const dashboardSection = document.getElementById('dashboardSectionView');
        const ticketsSection = document.getElementById('ticketsSectionView');
        const employeesSection = document.getElementById('employeesSectionView');
        const breadcrumb = document.getElementById('breadcrumbCurrentPage');
        const navDashboard = document.getElementById('navDashboard');
        const navTickets = document.getElementById('navTickets');
        const sidebarEmployeesBtn = document.getElementById('sidebarEmployeesBtn');

        if (dashboardSection) dashboardSection.style.display = 'none';
        if (ticketsSection) ticketsSection.style.display = 'block';
        if (employeesSection) employeesSection.style.display = 'none';
        if (breadcrumb) breadcrumb.textContent = 'All Support Tickets';

        if (navTickets) navTickets.classList.add('active');
        if (navDashboard) navDashboard.classList.remove('active');
        if (sidebarEmployeesBtn) sidebarEmployeesBtn.classList.remove('active');

        loadDashboardSummary();
        loadTickets();
    }

    function showEmployeesSection() {
        const role = AuthService.getRole();
        if (role !== 'ADMIN') {
            showSystemToast('Access Denied: Only administrators can access the employee directory.', true);
            return;
        }

        const dashboardSection = document.getElementById('dashboardSectionView');
        const ticketsSection = document.getElementById('ticketsSectionView');
        const employeesSection = document.getElementById('employeesSectionView');
        const breadcrumb = document.getElementById('breadcrumbCurrentPage');
        const navDashboard = document.getElementById('navDashboard');
        const navTickets = document.getElementById('navTickets');
        const sidebarEmployeesBtn = document.getElementById('sidebarEmployeesBtn');

        if (dashboardSection) dashboardSection.style.display = 'none';
        if (ticketsSection) ticketsSection.style.display = 'none';
        if (employeesSection) employeesSection.style.display = 'block';
        if (breadcrumb) breadcrumb.textContent = 'Employee Directory';

        if (sidebarEmployeesBtn) sidebarEmployeesBtn.classList.add('active');
        if (navDashboard) navDashboard.classList.remove('active');
        if (navTickets) navTickets.classList.remove('active');

        loadEmployees();
    }

    function showSystemToast(message, isError = false) {
        const toast = document.getElementById('systemToastAlert');
        if (!toast) return;
        toast.textContent = message;
        toast.className = 'alert ' + (isError ? 'alert-danger' : 'alert-success');
        toast.style.display = 'flex';
        setTimeout(() => {
            toast.style.display = 'none';
        }, 5000);
    }

    // ==================== User & Header Display ====================

    function initUserDisplay() {
        const user = AuthService.getUser();
        if (!user) return;

        const userNameEls = document.querySelectorAll('.display-user-name');
        const userRoleEls = document.querySelectorAll('.display-user-role');
        const userAvatarEls = document.querySelectorAll('.display-user-avatar');

        userNameEls.forEach(el => el.textContent = user.name || user.email || 'User');
        userRoleEls.forEach(el => {
            el.textContent = user.role || 'EMPLOYEE';
            el.className = 'badge display-user-role ' + getRoleBadgeClass(user.role);
        });

        const initials = getInitials(user.name || user.email || 'U');
        userAvatarEls.forEach(el => el.textContent = initials);

        // Role-based visibility for Admin sidebar menu
        const role = user.role || 'EMPLOYEE';
        const adminSection = document.getElementById('adminNavSectionTitle');
        const adminBtn = document.getElementById('sidebarEmployeesBtn');
        if (adminSection) adminSection.style.display = role === 'ADMIN' ? 'block' : 'none';
        if (adminBtn) adminBtn.style.display = role === 'ADMIN' ? 'flex' : 'none';
    }

    // ==================== Dashboard Statistics ====================

    function updateMetricsDisplay(total, openAssigned, inProgress, resolved, overdue, openCount, assignedCount, resCount, closedCount) {
        // 1. Dashboard 4 cards
        setStatValue('statTotalTickets', total);
        setStatValue('statInProgressTickets', inProgress);
        setStatValue('statOverdueTickets', overdue);
        setStatValue('statResolvedTickets', resolved);

        const inProgSub = document.getElementById('statInProgressSub');
        if (inProgSub) inProgSub.textContent = `${openCount} open, ${assignedCount} assigned`;
        const resSub = document.getElementById('statResolvedSub');
        if (resSub) resSub.textContent = `${resCount} resolved, ${closedCount} closed`;

        // 2. All Tickets 5 cards
        setStatValue('ticketsStatTotal', total);
        setStatValue('ticketsStatOpen', openAssigned);
        setStatValue('ticketsStatInProgress', inProgress);
        setStatValue('ticketsStatResolved', resolved);
        setStatValue('ticketsStatOverdue', overdue);

        const tOpenSub = document.getElementById('ticketsStatOpenSub');
        if (tOpenSub) tOpenSub.textContent = `${openCount} open, ${assignedCount} assigned`;
        const tInProgSub = document.getElementById('ticketsStatInProgressSub');
        if (tInProgSub) tInProgSub.textContent = 'Active resolution work';
        const tResSub = document.getElementById('ticketsStatResolvedSub');
        if (tResSub) tResSub.textContent = `${resCount} resolved, ${closedCount} closed`;

        // 3. Header badge
        const totalHdr = document.getElementById('totalTicketsHeaderBadge');
        if (totalHdr) totalHdr.textContent = `${total} total`;
    }

    function resetMetricsDisplay(val = '...') {
        setStatValue('statTotalTickets', val);
        setStatValue('statInProgressTickets', val);
        setStatValue('statOverdueTickets', val);
        setStatValue('statResolvedTickets', val);

        setStatValue('ticketsStatTotal', val);
        setStatValue('ticketsStatOpen', val);
        setStatValue('ticketsStatInProgress', val);
        setStatValue('ticketsStatResolved', val);
        setStatValue('ticketsStatOverdue', val);
    }

    async function loadDashboardSummary() {
        const role = AuthService.getRole();
        const statsNotice = document.getElementById('statsNoticeAlert');

        if (role === 'EMPLOYEE') {
            if (statsNotice) {
                statsNotice.style.display = 'flex';
                statsNotice.className = 'alert alert-info';
                statsNotice.innerHTML = '<strong>Personal Overview:</strong> Displaying real-time metrics for your submitted support tickets. Aggregate organization metrics are accessible to Technicians and Administrators.';
            }
            if (allLoadedTickets && allLoadedTickets.length > 0) {
                calculateAndSetEmployeeMetrics(allLoadedTickets);
            } else {
                updateMetricsDisplay(0, 0, 0, 0, 0, 0, 0, 0, 0);
            }
            return;
        }

        resetMetricsDisplay('...');

        try {
            const summary = await ApiClient.get(AppConfig.ENDPOINTS.DASHBOARD_SUMMARY);
            if (statsNotice) statsNotice.style.display = 'none';

            const total = summary.totalTickets ?? 0;
            const openAssigned = (summary.openTickets ?? 0) + (summary.assignedTickets ?? 0);
            const inProgress = summary.inProgressTickets ?? 0;
            const resolved = (summary.resolvedTickets ?? 0) + (summary.closedTickets ?? 0);
            const overdue = summary.overdueTickets ?? 0;
            const openCount = summary.openTickets ?? 0;
            const assignedCount = summary.assignedTickets ?? 0;
            const resCount = summary.resolvedTickets ?? 0;
            const closedCount = summary.closedTickets ?? 0;

            updateMetricsDisplay(total, openAssigned, inProgress, resolved, overdue, openCount, assignedCount, resCount, closedCount);

        } catch (err) {
            console.error('Failed to load dashboard summary:', err);
            resetMetricsDisplay('—');

            if (statsNotice) {
                statsNotice.style.display = 'flex';
                statsNotice.className = 'alert alert-warning';
                statsNotice.innerHTML = `<strong>Could not load summary:</strong> ${escapeHtml(err.message)}`;
            }
        }
    }

    function calculateAndSetEmployeeMetrics(tickets) {
        const total = tickets.length;
        const openCount = tickets.filter(t => t.status === 'OPEN').length;
        const assignedCount = tickets.filter(t => t.status === 'ASSIGNED').length;
        const inProgress = tickets.filter(t => t.status === 'IN_PROGRESS').length;
        const resCount = tickets.filter(t => t.status === 'RESOLVED').length;
        const closedCount = tickets.filter(t => t.status === 'CLOSED').length;
        const resolved = resCount + closedCount;

        const overdue = tickets.filter(t => {
            if (t.overdue) return true;
            if (t.slaDeadline && (t.status !== 'RESOLVED' && t.status !== 'CLOSED')) {
                return new Date(t.slaDeadline) < new Date();
            }
            return false;
        }).length;

        updateMetricsDisplay(total, openCount + assignedCount, inProgress, resolved, overdue, openCount, assignedCount, resCount, closedCount);
    }

    // ==================== Tickets Listing & Multi-Facet Filtering ====================

    async function loadTickets() {
        const tableBody = document.getElementById('ticketsTableBody');
        const dashTableBody = document.getElementById('dashboardTicketsTableBody');
        const badge = document.getElementById('ticketCountBadge');
        const dashBadge = document.getElementById('dashboardTicketCountBadge');

        if (tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align: center; padding: 48px 20px;">
                        <div class="loading-state-container">
                            <div class="spinner-enterprise" style="margin-bottom: 12px;"></div>
                            <div style="font-weight: 600; color: var(--text-primary); font-size: 13.5px;">Loading tickets from server...</div>
                            <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Fetching real-time ticket records</div>
                        </div>
                    </td>
                </tr>
            `;
        }

        if (dashTableBody) {
            dashTableBody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 36px 20px; color: var(--text-muted);">
                        Loading recent tickets from server...
                    </td>
                </tr>
            `;
        }

        try {
            const tickets = await ApiClient.get(AppConfig.ENDPOINTS.TICKETS);
            allLoadedTickets = Array.isArray(tickets) ? tickets : [];

            // If user is Employee, recalculate Employee summary metrics from their real loaded tickets
            const role = AuthService.getRole();
            if (role === 'EMPLOYEE') {
                calculateAndSetEmployeeMetrics(allLoadedTickets);
            } else {
                const totalHdr = document.getElementById('totalTicketsHeaderBadge');
                if (totalHdr && (!totalHdr.textContent || totalHdr.textContent === '...')) {
                    totalHdr.textContent = `${allLoadedTickets.length} total`;
                }
            }

            // Populate Dashboard recent tickets summary table
            renderDashboardTickets(allLoadedTickets);

            // Populate Category filter options dynamically from data
            populateCategoryFilter(allLoadedTickets);

            // Apply active filters and search
            applyTicketFilters();

        } catch (err) {
            console.error('Failed to load tickets:', err);
            if (badge) badge.textContent = 'Error';
            if (dashBadge) dashBadge.textContent = 'Error';

            if (dashTableBody) {
                dashTableBody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align: center; padding: 28px 20px; color: var(--priority-critical);">
                            <div style="font-weight: 600; font-size: 14px;">Error Loading Tickets</div>
                            <div style="font-size: 12.5px; margin-top: 4px;">${escapeHtml(err.message)}</div>
                        </td>
                    </tr>
                `;
            }

            if (tableBody) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="10" style="text-align: center; padding: 40px 20px;">
                            <div class="error-state-box">
                                <div class="error-state-icon" style="margin-bottom: 12px;">
                                    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                                </div>
                                <div style="font-weight: 700; font-size: 14px; color: var(--priority-critical);">Unable to Load Tickets</div>
                                <div style="font-size: 12.5px; color: var(--text-muted); margin-top: 4px; max-width: 360px;">${escapeHtml(err.message || 'An error occurred while communicating with the server.')}</div>
                                <button type="button" class="btn btn-secondary btn-sm" style="margin-top: 14px;" id="retryTicketsBtn">
                                    Retry Connection
                                </button>
                            </div>
                        </td>
                    </tr>
                `;

                const retryBtn = document.getElementById('retryTicketsBtn');
                if (retryBtn) retryBtn.addEventListener('click', () => loadTickets());
            }
        }
    }

    function renderDashboardTickets(tickets) {
        const tableBody = document.getElementById('dashboardTicketsTableBody');
        const badge = document.getElementById('dashboardTicketCountBadge');
        if (!tableBody) return;

        if (badge) {
            badge.textContent = `${tickets.length} total`;
        }

        if (!tickets || tickets.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 36px 20px; color: var(--text-muted);">
                        <div style="font-size: 14px; font-weight: 600; color: var(--text-primary);">No tickets recorded</div>
                        <div style="font-size: 12px; margin-top: 4px;">There are no support tickets in the system yet.</div>
                    </td>
                </tr>
            `;
            return;
        }

        const recentTickets = tickets.slice(0, 6);
        tableBody.innerHTML = recentTickets.map(ticket => renderDashboardTicketRow(ticket)).join('');

        tableBody.querySelectorAll('.btn-view-ticket').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const ticketId = btn.getAttribute('data-id');
                openTicketDetails(ticketId);
            });
        });

        tableBody.querySelectorAll('.clickable-ticket-row').forEach(row => {
            row.addEventListener('click', () => {
                const ticketId = row.getAttribute('data-id');
                openTicketDetails(ticketId);
            });
        });
    }

    function renderDashboardTicketRow(ticket) {
        const id = ticket.id;
        const num = escapeHtml(ticket.ticketNumber || `#${id}`);
        const title = escapeHtml(ticket.title || 'Untitled');
        const desc = escapeHtml(ticket.description || '').substring(0, 70);
        const category = escapeHtml(ticket.category || 'General');
        const priority = ticket.priority || 'MEDIUM';
        const status = ticket.status || 'OPEN';

        let slaHtml = '—';
        if (ticket.slaDeadline) {
            const formatted = formatDateTime(ticket.slaDeadline);
            const isResolvedOrClosed = status === 'RESOLVED' || status === 'CLOSED';
            const isOverdue = ticket.overdue || (!isResolvedOrClosed && new Date(ticket.slaDeadline) < new Date());

            if (isOverdue) {
                slaHtml = `<span style="color: var(--priority-critical); font-weight: 600;">${formatted}</span> <span class="badge badge-critical" style="font-size: 10px;">OVERDUE</span>`;
            } else {
                slaHtml = formatted;
            }
        }

        const requester = ticket.employee ? escapeHtml(ticket.employee.name) : 'Unknown';

        return `
            <tr class="clickable-row clickable-ticket-row" data-id="${id}">
                <td><span class="ticket-code">${num}</span></td>
                <td>
                    <div class="ticket-title-cell" title="${title}">
                        <div class="ticket-title-text">${title}</div>
                        <div class="ticket-desc-text">${desc}</div>
                    </div>
                </td>
                <td><span class="badge-category">${category}</span></td>
                <td><span class="badge ${getPriorityBadgeClass(priority)}">${priority}</span></td>
                <td><span class="badge ${getStatusBadgeClass(status)}">${formatStatus(status)}</span></td>
                <td>${slaHtml}</td>
                <td><div style="font-size: 12.5px; font-weight: 500;">${requester}</div></td>
                <td style="text-align: right;">
                    <button type="button" class="btn btn-outline btn-sm btn-view-ticket" data-id="${id}">
                        View Details &rarr;
                    </button>
                </td>
            </tr>
        `;
    }

    function populateCategoryFilter(tickets) {
        const categoryFilter = document.getElementById('categoryFilter');
        if (!categoryFilter) return;

        const currentVal = categoryFilter.value || 'ALL';
        const distinctCategories = Array.from(new Set(
            tickets.map(t => (t.category || '').trim()).filter(Boolean)
        )).sort();

        let optionsHtml = '<option value="ALL">All Categories</option>';
        distinctCategories.forEach(cat => {
            optionsHtml += `<option value="${escapeHtml(cat)}">${escapeHtml(cat)}</option>`;
        });

        categoryFilter.innerHTML = optionsHtml;
        if (distinctCategories.includes(currentVal)) {
            categoryFilter.value = currentVal;
        } else {
            categoryFilter.value = 'ALL';
        }
    }

    function applyTicketFilters() {
        const tableBody = document.getElementById('ticketsTableBody');
        const badge = document.getElementById('ticketCountBadge');
        const indicator = document.getElementById('filterStatusIndicator');
        if (!tableBody) return;

        const searchInput = document.getElementById('ticketSearchInput');
        const statusFilter = document.getElementById('statusFilter');
        const priorityFilter = document.getElementById('priorityFilter');
        const categoryFilter = document.getElementById('categoryFilter');

        const query = searchInput ? (searchInput.value || '').trim().toLowerCase() : '';
        const statusVal = statusFilter ? statusFilter.value : 'ALL';
        const priorityVal = priorityFilter ? priorityFilter.value : 'ALL';
        const categoryVal = categoryFilter ? categoryFilter.value : 'ALL';

        const isFiltered = query.length > 0 || statusVal !== 'ALL' || priorityVal !== 'ALL' || categoryVal !== 'ALL';

        let filtered = allLoadedTickets.filter(ticket => {
            // Status filter
            if (statusVal !== 'ALL' && ticket.status !== statusVal) {
                return false;
            }

            // Priority filter
            if (priorityVal !== 'ALL' && ticket.priority !== priorityVal) {
                return false;
            }

            // Category filter
            if (categoryVal !== 'ALL' && (ticket.category || '') !== categoryVal) {
                return false;
            }

            // Search query
            if (query.length > 0) {
                const num = (ticket.ticketNumber || `#${ticket.id}`).toLowerCase();
                const title = (ticket.title || '').toLowerCase();
                const desc = (ticket.description || '').toLowerCase();
                const cat = (ticket.category || '').toLowerCase();
                const requester = (ticket.employee && ticket.employee.name ? ticket.employee.name : '').toLowerCase();
                const tech = (ticket.assignedTechnician && ticket.assignedTechnician.name ? ticket.assignedTechnician.name : '').toLowerCase();

                const match = num.includes(query) ||
                    title.includes(query) ||
                    desc.includes(query) ||
                    cat.includes(query) ||
                    requester.includes(query) ||
                    tech.includes(query);

                if (!match) return false;
            }

            return true;
        });

        if (badge) {
            badge.textContent = `${filtered.length} ${filtered.length === 1 ? 'ticket' : 'tickets'}`;
        }

        if (indicator) {
            if (isFiltered) {
                indicator.style.display = 'block';
                indicator.textContent = `Showing ${filtered.length} of ${allLoadedTickets.length} tickets`;
            } else {
                indicator.style.display = 'none';
            }
        }

        if (allLoadedTickets.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align: center; padding: 48px 20px;">
                        <div class="empty-state-box">
                            <div class="empty-state-icon" style="margin-bottom: 12px;">
                                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
                            </div>
                            <div style="font-weight: 700; font-size: 14.5px; color: var(--text-primary);">No Support Tickets Found</div>
                            <div style="font-size: 12.5px; color: var(--text-muted); margin-top: 4px; max-width: 320px;">
                                There are no tickets recorded in the system yet. Click "Create Ticket" to get started.
                            </div>
                            <button type="button" class="btn btn-primary btn-sm" style="margin-top: 14px;" id="emptyCreateTicketBtn">
                                Create Ticket
                            </button>
                        </div>
                    </td>
                </tr>
            `;
            const emptyCreateBtn = document.getElementById('emptyCreateTicketBtn');
            if (emptyCreateBtn) emptyCreateBtn.addEventListener('click', openCreateTicketModal);
            return;
        }

        if (filtered.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align: center; padding: 48px 20px;">
                        <div class="empty-state-box">
                            <div class="empty-state-icon filter-empty" style="margin-bottom: 12px;">
                                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                            </div>
                            <div style="font-weight: 700; font-size: 14.5px; color: var(--text-primary);">No Matching Tickets Found</div>
                            <div style="font-size: 12.5px; color: var(--text-muted); margin-top: 4px; max-width: 340px;">
                                No tickets matched your current search and filter criteria. Try adjusting or resetting your filters.
                            </div>
                            <button type="button" class="btn btn-secondary btn-sm" style="margin-top: 14px;" id="emptyClearFiltersBtn">
                                Reset Filters
                            </button>
                        </div>
                    </td>
                </tr>
            `;
            const emptyClearBtn = document.getElementById('emptyClearFiltersBtn');
            if (emptyClearBtn) {
                emptyClearBtn.addEventListener('click', resetTicketFilters);
            }
            return;
        }

        tableBody.innerHTML = filtered.map(ticket => renderTicketRow(ticket)).join('');

        tableBody.querySelectorAll('.btn-view-ticket').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const ticketId = btn.getAttribute('data-id');
                openTicketDetails(ticketId);
            });
        });

        tableBody.querySelectorAll('.clickable-ticket-row').forEach(row => {
            row.addEventListener('click', () => {
                const ticketId = row.getAttribute('data-id');
                openTicketDetails(ticketId);
            });
        });
    }

    function resetTicketFilters() {
        const searchInput = document.getElementById('ticketSearchInput');
        const statusFilter = document.getElementById('statusFilter');
        const priorityFilter = document.getElementById('priorityFilter');
        const categoryFilter = document.getElementById('categoryFilter');

        if (searchInput) searchInput.value = '';
        if (statusFilter) statusFilter.value = 'ALL';
        if (priorityFilter) priorityFilter.value = 'ALL';
        if (categoryFilter) categoryFilter.value = 'ALL';

        applyTicketFilters();
    }

    function formatTimeRemaining(iso) {
        if (!iso) return '—';
        try {
            const deadline = new Date(iso);
            if (isNaN(deadline.getTime())) return '—';
            const now = new Date();
            const diffMs = deadline - now;
            if (diffMs <= 0) return 'Overdue';
            const diffSecs = Math.floor(diffMs / 1000);
            const diffMins = Math.floor(diffSecs / 60);
            const diffHours = Math.floor(diffMins / 60);
            const diffDays = Math.floor(diffHours / 24);

            if (diffDays > 0) {
                const remHours = diffHours % 24;
                return remHours > 0 ? `${diffDays}d ${remHours}h left` : `${diffDays}d left`;
            }
            if (diffHours > 0) {
                const remMins = diffMins % 60;
                return remMins > 0 ? `${diffHours}h ${remMins}m left` : `${diffHours}h left`;
            }
            return `${Math.max(1, diffMins)}m left`;
        } catch (_) {
            return '—';
        }
    }

    function formatStatus(s) {
        if (!s) return 'Open';
        switch (s) {
            case 'IN_PROGRESS': return 'In Progress';
            case 'OPEN': return 'Open';
            case 'ASSIGNED': return 'Assigned';
            case 'RESOLVED': return 'Resolved';
            case 'CLOSED': return 'Closed';
            default: return s;
        }
    }

    function renderTicketRow(ticket) {
        const id = ticket.id;
        const num = escapeHtml(ticket.ticketNumber || `#${id}`);
        const title = escapeHtml(ticket.title || 'Untitled');
        const desc = escapeHtml(ticket.description || '').substring(0, 90);
        const category = escapeHtml(ticket.category || 'General');
        const priority = ticket.priority || 'MEDIUM';
        const status = ticket.status || 'OPEN';

        // Requester snippet
        const requesterName = ticket.employee ? ticket.employee.name : 'Unknown';
        const requesterInitials = getInitials(requesterName);

        // Technician snippet
        let techHtml = '<span style="color: var(--text-muted); font-size: 12px; font-style: italic;">Unassigned</span>';
        if (ticket.assignedTechnician) {
            const techName = ticket.assignedTechnician.name;
            const techInitials = getInitials(techName);
            techHtml = `
                <div class="user-inline-snippet" title="${escapeHtml(techName)}">
                    <div class="user-avatar-xs tech">${techInitials}</div>
                    <div style="font-weight: 500; font-size: 12.5px; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 110px;">${escapeHtml(techName)}</div>
                </div>
            `;
        }

        // SLA Indicator
        let slaHtml = '<span style="color: var(--text-muted); font-size: 12px;">—</span>';
        if (ticket.slaDeadline) {
            const formatted = formatDateTime(ticket.slaDeadline);
            const isResolvedOrClosed = status === 'RESOLVED' || status === 'CLOSED';
            const isOverdue = ticket.overdue || (!isResolvedOrClosed && new Date(ticket.slaDeadline) < new Date());

            if (isOverdue) {
                slaHtml = `
                    <div class="sla-overdue-box">
                        <div class="sla-countdown" style="color: var(--priority-critical); display: flex; align-items: center; gap: 4px;">
                            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                            <span>Overdue</span>
                        </div>
                        <div class="sla-date-text" style="color: var(--priority-critical);">${formatted}</div>
                    </div>
                `;
            } else {
                const countdownStr = isResolvedOrClosed ? 'Met Target' : formatTimeRemaining(ticket.slaDeadline);
                slaHtml = `
                    <div class="sla-normal-box">
                        <div class="sla-countdown">${countdownStr}</div>
                        <div class="sla-date-text" style="color: var(--text-muted);">${formatted}</div>
                    </div>
                `;
            }
        }

        // Created Date
        const createdDate = formatDateTime(ticket.createdAt);

        return `
            <tr class="clickable-row clickable-ticket-row" data-id="${id}">
                <td><span class="ticket-code">${num}</span></td>
                <td>
                    <div class="ticket-title-cell" title="${title}">
                        <div class="ticket-title-text">${title}</div>
                        <div class="ticket-desc-text">${desc}</div>
                    </div>
                </td>
                <td><span class="badge-category">${category}</span></td>
                <td><span class="badge ${getPriorityBadgeClass(priority)}">${priority}</span></td>
                <td><span class="badge ${getStatusBadgeClass(status)}">${formatStatus(status)}</span></td>
                <td>
                    <div class="user-inline-snippet" title="${escapeHtml(requesterName)}">
                        <div class="user-avatar-xs">${requesterInitials}</div>
                        <div style="font-weight: 500; font-size: 12.5px; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 110px;">${escapeHtml(requesterName)}</div>
                    </div>
                </td>
                <td>${techHtml}</td>
                <td>${slaHtml}</td>
                <td><div style="font-size: 12px; color: var(--text-secondary);">${createdDate}</div></td>
                <td style="text-align: right;">
                    <button type="button" class="btn btn-outline btn-sm btn-view-ticket" data-id="${id}">
                        View Details &rarr;
                    </button>
                </td>
            </tr>
        `;
    }

    // ==================== Feature 1: Ticket Creation ====================

    function openCreateTicketModal() {
        const modal = document.getElementById('createTicketModal');
        const alertBox = document.getElementById('createTicketAlert');
        const form = document.getElementById('createTicketForm');

        if (alertBox) alertBox.style.display = 'none';
        if (form) form.reset();
        if (modal) modal.style.display = 'flex';
    }

    function closeCreateTicketModal() {
        const modal = document.getElementById('createTicketModal');
        if (modal) modal.style.display = 'none';
    }

    async function handleCreateTicketSubmit(e) {
        e.preventDefault();
        const alertBox = document.getElementById('createTicketAlert');
        const alertMsg = document.getElementById('createTicketAlertMsg');
        const submitBtn = document.getElementById('submitCreateTicketBtn');
        const btnText = document.getElementById('submitCreateBtnText');

        function showError(msg) {
            alertMsg.textContent = msg;
            alertBox.style.display = 'flex';
        }

        const title = document.getElementById('ticketTitleInput').value.trim();
        const category = document.getElementById('ticketCategorySelect').value;
        const priority = document.getElementById('ticketPrioritySelect').value;
        const description = document.getElementById('ticketDescriptionInput').value.trim();

        if (!title) {
            showError('Please enter a ticket title.');
            return;
        }
        if (title.length > 150) {
            showError('Ticket title cannot exceed 150 characters.');
            return;
        }
        if (!category) {
            showError('Please select a category.');
            return;
        }
        if (!priority) {
            showError('Please select a priority.');
            return;
        }
        if (!description) {
            showError('Please enter a detailed description of the issue.');
            return;
        }

        const currentUser = AuthService.getUser();
        if (!currentUser || !currentUser.id) {
            showError('Authentication session not found. Please log in again.');
            return;
        }

        const payload = {
            title: title,
            description: description,
            category: category,
            priority: priority,
            employeeId: currentUser.id
        };

        submitBtn.disabled = true;
        btnText.textContent = 'Submitting...';

        try {
            const created = await ApiClient.post(AppConfig.ENDPOINTS.TICKETS, payload);
            closeCreateTicketModal();
            showSystemToast(`Ticket ${created.ticketNumber || '#' + created.id} created successfully!`);
            loadDashboardSummary();
            loadTickets();
        } catch (err) {
            showError(err.message || 'Failed to create ticket. Please check input.');
        } finally {
            submitBtn.disabled = false;
            btnText.textContent = 'Submit Ticket';
        }
    }

    // ==================== Feature 2: Ticket Details & Phase 7.4 Staff Actions ====================

    async function loadTechniciansList(selectedTechnicianId = null) {
        const selectEl = document.getElementById('assignTechnicianSelect');
        if (!selectEl) return;

        selectEl.innerHTML = '<option value="">Loading technicians...</option>';

        try {
            if (!cachedTechnicians) {
                cachedTechnicians = await ApiClient.get(AppConfig.ENDPOINTS.TECHNICIANS);
            }

            selectEl.innerHTML = '<option value="">Select Technician...</option>';
            cachedTechnicians.forEach(tech => {
                const isSelected = selectedTechnicianId && String(tech.id) === String(selectedTechnicianId);
                const opt = document.createElement('option');
                opt.value = tech.id;
                opt.textContent = `${tech.name} (${tech.email}) - ${tech.department || 'IT'}`;
                if (isSelected) opt.selected = true;
                selectEl.appendChild(opt);
            });
        } catch (err) {
            console.error('Failed to load technicians list:', err);
            selectEl.innerHTML = '<option value="">Failed to load technicians</option>';
        }
    }

    async function openTicketDetails(ticketId) {
        currentDetailTicketId = ticketId;
        const modal = document.getElementById('ticketDetailModal');
        const spinner = document.getElementById('detailLoadingSpinner');
        const content = document.getElementById('detailMainContent');
        const errorAlert = document.getElementById('detailErrorAlert');
        const actionAlert = document.getElementById('actionFeedbackAlert');

        if (modal) modal.style.display = 'flex';
        if (errorAlert) errorAlert.style.display = 'none';
        if (actionAlert) actionAlert.style.display = 'none';
        if (spinner) spinner.style.display = 'block';
        if (content) content.style.display = 'none';

        switchDetailTab('comments');

        try {
            const ticket = await ApiClient.get(`${AppConfig.ENDPOINTS.TICKETS}/${ticketId}`);

            document.getElementById('detailTicketNumber').textContent = ticket.ticketNumber || `#${ticket.id}`;
            document.getElementById('detailTicketTitle').textContent = ticket.title || 'Untitled Ticket';
            document.getElementById('detailCategory').textContent = ticket.category || 'General';

            const statusEl = document.getElementById('detailStatus');
            statusEl.innerHTML = `<span class="badge ${getStatusBadgeClass(ticket.status)}">${ticket.status}</span>`;

            const priorityEl = document.getElementById('detailPriority');
            priorityEl.innerHTML = `<span class="badge ${getPriorityBadgeClass(ticket.priority)}">${ticket.priority}</span>`;

            const slaEl = document.getElementById('detailSlaDeadline');
            if (ticket.slaDeadline) {
                const formattedSla = formatDateTime(ticket.slaDeadline);
                if (ticket.overdue) {
                    slaEl.innerHTML = `<span style="color: var(--priority-critical); font-weight: 700;">${formattedSla}</span> <span class="badge badge-critical">OVERDUE</span>`;
                } else {
                    slaEl.textContent = formattedSla;
                }
            } else {
                slaEl.textContent = '—';
            }

            document.getElementById('detailRequester').textContent = ticket.employee ? `${ticket.employee.name} (${ticket.employee.department || 'Staff'})` : 'Unknown';
            document.getElementById('detailTechnician').textContent = ticket.assignedTechnician ? `${ticket.assignedTechnician.name} (${ticket.assignedTechnician.email})` : 'Unassigned';
            document.getElementById('detailCreatedAt').textContent = formatDateTime(ticket.createdAt);
            document.getElementById('detailUpdatedAt').textContent = formatDateTime(ticket.updatedAt);
            document.getElementById('detailDescription').textContent = ticket.description || 'No description provided.';

            // Resolution Notes Display
            const resSection = document.getElementById('detailResolutionSection');
            const resNotesEl = document.getElementById('detailResolutionNotes');
            if (ticket.resolutionNotes && ticket.resolutionNotes.trim() !== '') {
                resNotesEl.textContent = ticket.resolutionNotes;
                resSection.style.display = 'block';
            } else {
                resSection.style.display = 'none';
            }

            // Phase 7.4: Staff Actions Panel Visibility & Control Population
            const role = AuthService.getRole();
            const staffPanel = document.getElementById('detailStaffActionsPanel');
            const assignSection = document.getElementById('assignTechnicianSection');
            const statusSection = document.getElementById('updateStatusSection');
            const resolveSection = document.getElementById('resolveTicketSection');

            if (role === 'ADMIN') {
                if (staffPanel) staffPanel.style.display = 'block';
                if (assignSection) assignSection.style.display = 'block';
                if (statusSection) statusSection.style.display = 'block';
                if (resolveSection) resolveSection.style.display = 'block';

                const statusSelect = document.getElementById('updateStatusSelect');
                if (statusSelect) statusSelect.value = ticket.status || 'OPEN';

                // Load technicians for assignment
                loadTechniciansList(ticket.assignedTechnician ? ticket.assignedTechnician.id : null);

            } else if (role === 'TECHNICIAN') {
                if (staffPanel) staffPanel.style.display = 'block';
                if (assignSection) assignSection.style.display = 'none'; // Only ADMIN can assign technicians
                if (statusSection) statusSection.style.display = 'block';
                if (resolveSection) resolveSection.style.display = 'block';

                const statusSelect = document.getElementById('updateStatusSelect');
                if (statusSelect) statusSelect.value = ticket.status || 'OPEN';

            } else {
                // EMPLOYEE role: No staff management actions allowed
                if (staffPanel) staffPanel.style.display = 'none';
            }

            if (spinner) spinner.style.display = 'none';
            if (content) content.style.display = 'block';

            loadTicketComments(ticketId);
            loadTicketActivities(ticketId);

        } catch (err) {
            if (spinner) spinner.style.display = 'none';
            if (errorAlert) {
                errorAlert.textContent = err.message || 'Failed to load ticket details.';
                errorAlert.style.display = 'flex';
            }
        }
    }

    function closeTicketDetailModal() {
        const modal = document.getElementById('ticketDetailModal');
        if (modal) modal.style.display = 'none';
        currentDetailTicketId = null;
    }

    function switchDetailTab(tabName) {
        const commentsBtn = document.getElementById('tabCommentsBtn');
        const activitiesBtn = document.getElementById('tabActivitiesBtn');
        const commentsContent = document.getElementById('tabCommentsContent');
        const activitiesContent = document.getElementById('tabActivitiesContent');

        if (tabName === 'comments') {
            commentsBtn.classList.add('active');
            activitiesBtn.classList.remove('active');
            commentsContent.style.display = 'block';
            activitiesContent.style.display = 'none';
        } else {
            activitiesBtn.classList.add('active');
            commentsBtn.classList.remove('active');
            activitiesContent.style.display = 'block';
            commentsContent.style.display = 'none';
        }
    }

    // ==================== Phase 7.4 Action Handlers ====================

    // 1. Assign Technician (PATCH /api/tickets/{id}/assign/{technicianId})
    async function handleAssignTechnician() {
        if (!currentDetailTicketId) return;

        const select = document.getElementById('assignTechnicianSelect');
        const btn = document.getElementById('submitAssignBtn');
        const alertBox = document.getElementById('actionFeedbackAlert');

        const techId = select.value;
        if (!techId) {
            if (alertBox) {
                alertBox.textContent = 'Please select a technician to assign.';
                alertBox.style.display = 'flex';
            }
            return;
        }

        if (alertBox) alertBox.style.display = 'none';
        btn.disabled = true;
        btn.textContent = 'Assigning...';

        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.TICKETS}/${currentDetailTicketId}/assign/${techId}`);
            showSystemToast('Technician assigned successfully.');
            // Refresh details, activities, tickets list, and dashboard stats
            openTicketDetails(currentDetailTicketId);
            loadTickets();
            loadDashboardSummary();
        } catch (err) {
            if (alertBox) {
                alertBox.textContent = err.message || 'Failed to assign technician.';
                alertBox.style.display = 'flex';
            }
        } finally {
            btn.disabled = false;
            btn.textContent = 'Assign';
        }
    }

    // 2. Update Status (PATCH /api/tickets/{id}/status?status={newStatus})
    async function handleUpdateStatus() {
        if (!currentDetailTicketId) return;

        const select = document.getElementById('updateStatusSelect');
        const btn = document.getElementById('submitStatusBtn');
        const alertBox = document.getElementById('actionFeedbackAlert');

        const newStatus = select.value;
        if (!newStatus) return;

        if (alertBox) alertBox.style.display = 'none';
        btn.disabled = true;
        btn.textContent = 'Updating...';

        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.TICKETS}/${currentDetailTicketId}/status?status=${newStatus}`);
            showSystemToast(`Ticket status changed to ${newStatus}.`);
            openTicketDetails(currentDetailTicketId);
            loadTickets();
            loadDashboardSummary();
        } catch (err) {
            if (alertBox) {
                alertBox.textContent = err.message || 'Failed to update ticket status.';
                alertBox.style.display = 'flex';
            }
        } finally {
            btn.disabled = false;
            btn.textContent = 'Update';
        }
    }

    // 3. Resolve Ticket with Notes (PATCH /api/tickets/{id}/resolve)
    async function handleResolveTicket() {
        if (!currentDetailTicketId) return;

        const notesInput = document.getElementById('resolveNotesInput');
        const markCheck = document.getElementById('markResolvedCheckbox');
        const btn = document.getElementById('submitResolveBtn');
        const alertBox = document.getElementById('actionFeedbackAlert');

        const notes = notesInput.value.trim();
        if (!notes) {
            if (alertBox) {
                alertBox.textContent = 'Resolution notes cannot be blank.';
                alertBox.style.display = 'flex';
                notesInput.focus();
            }
            return;
        }

        if (alertBox) alertBox.style.display = 'none';
        btn.disabled = true;
        btn.textContent = 'Saving...';

        // Matches ResolutionRequest DTO
        const payload = {
            resolutionNotes: notes,
            markResolved: markCheck ? markCheck.checked : true
        };

        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.TICKETS}/${currentDetailTicketId}/resolve`, payload);
            showSystemToast('Resolution notes recorded successfully.');
            notesInput.value = '';
            openTicketDetails(currentDetailTicketId);
            loadTickets();
            loadDashboardSummary();
        } catch (err) {
            if (alertBox) {
                alertBox.textContent = err.message || 'Failed to record resolution.';
                alertBox.style.display = 'flex';
            }
        } finally {
            btn.disabled = false;
            btn.textContent = 'Save Resolution';
        }
    }

    // ==================== Feature 3: Comments ====================

    async function loadTicketComments(ticketId) {
        const container = document.getElementById('commentsContainer');
        const countBadge = document.getElementById('commentCount');
        if (!container) return;

        container.innerHTML = '<div style="color: var(--text-muted); font-size: 12.5px;">Loading comments...</div>';

        try {
            const comments = await ApiClient.get(`${AppConfig.ENDPOINTS.TICKETS}/${ticketId}/comments`);
            if (countBadge) countBadge.textContent = comments.length;

            if (!comments || comments.length === 0) {
                container.innerHTML = `
                    <div style="padding: 16px; background-color: #f8fafc; border: 1px dashed var(--border-color); border-radius: var(--radius-md); text-align: center; color: var(--text-muted); font-size: 13px;">
                        No comments yet. Leave a note or update below.
                    </div>
                `;
                return;
            }

            container.innerHTML = comments.map(c => {
                const authorName = c.author ? escapeHtml(c.author.name) : 'Author';
                const authorRole = c.author ? c.author.role : 'EMPLOYEE';
                const date = formatDateTime(c.createdAt);
                const text = escapeHtml(c.commentText || '');

                return `
                    <div class="comment-card">
                        <div class="comment-header">
                            <div class="comment-author-info">
                                <span class="comment-author-name">${authorName}</span>
                                <span class="badge ${getRoleBadgeClass(authorRole)}" style="font-size: 10px;">${authorRole}</span>
                            </div>
                            <span class="comment-date">${date}</span>
                        </div>
                        <div class="comment-text">${text}</div>
                    </div>
                `;
            }).join('');

        } catch (err) {
            container.innerHTML = `<div style="color: var(--priority-critical); font-size: 12.5px;">Failed to load comments: ${escapeHtml(err.message)}</div>`;
        }
    }

    async function handleAddCommentSubmit(e) {
        e.preventDefault();
        if (!currentDetailTicketId) return;

        const input = document.getElementById('commentTextInput');
        const alertBox = document.getElementById('addCommentAlert');
        const submitBtn = document.getElementById('submitCommentBtn');
        const btnText = document.getElementById('submitCommentBtnText');

        const text = input.value.trim();
        if (!text) {
            alertBox.textContent = 'Comment text cannot be empty.';
            alertBox.style.display = 'flex';
            input.focus();
            return;
        }

        alertBox.style.display = 'none';
        submitBtn.disabled = true;
        btnText.textContent = 'Posting...';

        const currentUser = AuthService.getUser();
        const payload = {
            employeeId: currentUser.id,
            commentText: text
        };

        try {
            await ApiClient.post(`${AppConfig.ENDPOINTS.TICKETS}/${currentDetailTicketId}/comments`, payload);
            input.value = '';
            loadTicketComments(currentDetailTicketId);
            loadTicketActivities(currentDetailTicketId);
        } catch (err) {
            alertBox.textContent = err.message || 'Failed to post comment.';
            alertBox.style.display = 'flex';
        } finally {
            submitBtn.disabled = false;
            btnText.textContent = 'Post Comment';
        }
    }

    // ==================== Feature 4: Activity History ====================

    async function loadTicketActivities(ticketId) {
        const container = document.getElementById('activitiesContainer');
        const countBadge = document.getElementById('activityCount');
        if (!container) return;

        container.innerHTML = '<div style="color: var(--text-muted); font-size: 12.5px;">Loading audit timeline...</div>';

        try {
            const activities = await ApiClient.get(`${AppConfig.ENDPOINTS.TICKETS}/${ticketId}/activities`);
            if (countBadge) countBadge.textContent = activities.length;

            if (!activities || activities.length === 0) {
                container.innerHTML = `
                    <div style="padding: 16px; background-color: #f8fafc; border: 1px dashed var(--border-color); border-radius: var(--radius-md); text-align: center; color: var(--text-muted); font-size: 13px;">
                        No activity history recorded.
                    </div>
                `;
                return;
            }

            container.innerHTML = activities.map(a => {
                const type = a.activityType || 'EVENT';
                const desc = escapeHtml(a.description || '');
                const performer = a.performedBy ? escapeHtml(a.performedBy.name) : 'System';
                const date = formatDateTime(a.createdAt);

                return `
                    <div class="timeline-item">
                        <div class="timeline-marker"></div>
                        <div class="timeline-header">
                            <span class="timeline-type">${formatActivityType(type)} &bull; ${performer}</span>
                            <span class="timeline-date">${date}</span>
                        </div>
                        <div class="timeline-desc">${desc}</div>
                    </div>
                `;
            }).join('');

        } catch (err) {
            container.innerHTML = `<div style="color: var(--priority-critical); font-size: 12.5px;">Failed to load activity history: ${escapeHtml(err.message)}</div>`;
        }
    }

    // ==================== Phase 7.5: Employee Administration ====================

    async function loadEmployees() {
        const tableBody = document.getElementById('employeesTableBody');
        const badge = document.getElementById('employeeCountBadge');
        if (!tableBody) return;

        tableBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 28px; color: var(--text-muted);">
                    Loading employees from server...
                </td>
            </tr>
        `;

        let url = AppConfig.ENDPOINTS.EMPLOYEES;
        const params = new URLSearchParams();

        const roleFilter = document.getElementById('employeeRoleFilter');
        const deptFilter = document.getElementById('employeeDeptFilter');

        if (roleFilter && roleFilter.value && roleFilter.value !== 'ALL') {
            params.append('role', roleFilter.value);
        }
        if (deptFilter && deptFilter.value && deptFilter.value !== 'ALL') {
            params.append('department', deptFilter.value);
        }

        const q = params.toString();
        if (q) url += '?' + q;

        try {
            const employees = await ApiClient.get(url);
            cachedEmployees = Array.isArray(employees) ? employees : [];
            renderEmployeesTable();
        } catch (err) {
            console.error('Failed to load employees:', err);
            if (badge) badge.textContent = 'Error';
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 28px 20px; color: var(--priority-critical);">
                        <div style="font-weight: 600; font-size: 14px;">Error Loading Employees</div>
                        <div style="font-size: 12.5px; margin-top: 4px;">${escapeHtml(err.message)}</div>
                    </td>
                </tr>
            `;
        }
    }

    function renderEmployeesTable() {
        const tableBody = document.getElementById('employeesTableBody');
        const badge = document.getElementById('employeeCountBadge');
        if (!tableBody) return;

        const searchInput = document.getElementById('employeeSearchInput');
        const query = searchInput ? searchInput.value.trim().toLowerCase() : '';

        let list = cachedEmployees;
        if (query) {
            list = list.filter(emp => {
                const name = (emp.name || '').toLowerCase();
                const email = (emp.email || '').toLowerCase();
                const dept = (emp.department || '').toLowerCase();
                const phone = (emp.phone || '').toLowerCase();
                const role = (emp.role || '').toLowerCase();
                return name.includes(query) || email.includes(query) || dept.includes(query) || phone.includes(query) || role.includes(query);
            });
        }

        if (badge) badge.textContent = `${list.length} employee${list.length === 1 ? '' : 's'}`;

        if (!list || list.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 36px 20px; color: var(--text-muted);">
                        <div style="font-size: 20px; margin-bottom: 8px;">👥</div>
                        <div style="font-size: 14px; font-weight: 600; color: var(--text-primary);">No employees found</div>
                        <div style="font-size: 12.5px; margin-top: 4px;">There are no employee records matching your search or filters.</div>
                    </td>
                </tr>
            `;
            return;
        }

        tableBody.innerHTML = list.map(emp => renderEmployeeRow(emp)).join('');

        tableBody.querySelectorAll('.btn-edit-employee').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const empId = btn.getAttribute('data-id');
                openEditEmployeeModal(empId);
            });
        });

        tableBody.querySelectorAll('.btn-deactivate-employee').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const empId = btn.getAttribute('data-id');
                handleDeactivateEmployee(empId);
            });
        });

        tableBody.querySelectorAll('.btn-activate-employee').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const empId = btn.getAttribute('data-id');
                handleActivateEmployee(empId);
            });
        });
    }

    function renderEmployeeRow(emp) {
        const id = emp.id;
        const name = escapeHtml(emp.name || 'Unnamed');
        const email = escapeHtml(emp.email || '—');
        const dept = escapeHtml(emp.department || '—');
        const role = emp.role || 'EMPLOYEE';
        const phone = escapeHtml(emp.phone || '—');
        const active = emp.active !== false;
        const joinedDate = formatDateTime(emp.createdAt);
        const initials = getInitials(emp.name || emp.email || 'U');

        const statusBadge = active
            ? '<span class="badge badge-active">Active</span>'
            : '<span class="badge badge-inactive">Inactive</span>';

        const toggleBtn = active
            ? `<button type="button" class="btn btn-outline btn-sm btn-deactivate-employee" data-id="${id}" style="color: var(--priority-critical); border-color: #fca5a5; padding: 4px 10px; font-size: 12px;" title="Deactivate employee account">Deactivate</button>`
            : `<button type="button" class="btn btn-outline btn-sm btn-activate-employee" data-id="${id}" style="color: var(--status-resolved); border-color: #86efac; padding: 4px 10px; font-size: 12px;" title="Reactivate employee account">Activate</button>`;

        return `
            <tr data-id="${id}">
                <td>
                    <div style="display: flex; align-items: center;">
                        <div class="user-table-avatar">${initials}</div>
                        <div>
                            <div style="font-weight: 600; color: var(--text-primary); font-size: 13.5px;">${name}</div>
                            <div style="font-size: 11.5px; color: var(--text-muted);">${email}</div>
                        </div>
                    </div>
                </td>
                <td><span class="badge" style="background-color: var(--border-light); color: var(--text-secondary);">${dept}</span></td>
                <td><span class="badge ${getRoleBadgeClass(role)}">${role}</span></td>
                <td><div style="font-size: 12.5px; color: var(--text-secondary);">${phone}</div></td>
                <td>${statusBadge}</td>
                <td><div style="font-size: 12px; color: var(--text-muted);">${joinedDate}</div></td>
                <td style="text-align: right; white-space: nowrap;">
                    <div style="display: inline-flex; gap: 6px;">
                        <button type="button" class="btn btn-outline btn-sm btn-edit-employee" data-id="${id}" style="padding: 4px 10px; font-size: 12px;">Edit</button>
                        ${toggleBtn}
                    </div>
                </td>
            </tr>
        `;
    }

    function openAddEmployeeModal() {
        const modal = document.getElementById('employeeModal');
        const alertBox = document.getElementById('employeeModalAlert');
        const form = document.getElementById('employeeForm');

        if (alertBox) alertBox.style.display = 'none';
        if (form) form.reset();

        document.getElementById('employeeIdInput').value = '';
        document.getElementById('employeeModalTitle').textContent = 'Add New Employee';
        document.getElementById('employeePasswordLabel').textContent = 'Password';
        const reqMarker = document.getElementById('employeePasswordRequiredMarker');
        if (reqMarker) reqMarker.style.display = 'inline';
        const pwdInput = document.getElementById('employeePasswordInput');
        pwdInput.value = '';
        pwdInput.placeholder = 'Minimum 6 characters';
        pwdInput.required = true;
        document.getElementById('employeePasswordHint').textContent = 'Must be at least 6 characters. Passwords are encrypted with BCrypt and never stored in plain-text.';
        document.getElementById('submitEmployeeBtnText').textContent = 'Create Employee';

        if (modal) modal.style.display = 'flex';
    }

    function openEditEmployeeModal(empId) {
        const modal = document.getElementById('employeeModal');
        const alertBox = document.getElementById('employeeModalAlert');
        const form = document.getElementById('employeeForm');

        if (alertBox) alertBox.style.display = 'none';
        if (form) form.reset();

        const emp = cachedEmployees.find(e => String(e.id) === String(empId));
        if (!emp) {
            showSystemToast('Could not find employee details.', true);
            return;
        }

        document.getElementById('employeeIdInput').value = emp.id;
        document.getElementById('employeeModalTitle').textContent = `Edit Employee: ${emp.name}`;
        document.getElementById('employeeNameInput').value = emp.name || '';
        document.getElementById('employeeEmailInput').value = emp.email || '';
        document.getElementById('employeeDeptInput').value = emp.department || '';
        document.getElementById('employeeRoleSelect').value = emp.role || 'EMPLOYEE';
        document.getElementById('employeePhoneInput').value = emp.phone || '';

        // Password is optional on update: blank leaves existing password unchanged
        document.getElementById('employeePasswordLabel').textContent = 'New Password (Optional)';
        const reqMarker = document.getElementById('employeePasswordRequiredMarker');
        if (reqMarker) reqMarker.style.display = 'none';
        const pwdInput = document.getElementById('employeePasswordInput');
        pwdInput.value = '';
        pwdInput.placeholder = 'Leave blank to keep existing password';
        pwdInput.required = false;
        document.getElementById('employeePasswordHint').textContent = 'Leave blank to preserve current password. Enter 6+ characters only if resetting password.';
        document.getElementById('submitEmployeeBtnText').textContent = 'Save Changes';

        if (modal) modal.style.display = 'flex';
    }

    function closeEmployeeModal() {
        const modal = document.getElementById('employeeModal');
        if (modal) modal.style.display = 'none';
    }

    async function handleEmployeeFormSubmit(e) {
        e.preventDefault();
        const alertBox = document.getElementById('employeeModalAlert');
        const alertMsg = document.getElementById('employeeModalAlertMsg');
        const submitBtn = document.getElementById('submitEmployeeBtn');
        const btnText = document.getElementById('submitEmployeeBtnText');

        function showError(msg) {
            alertMsg.textContent = msg;
            alertBox.style.display = 'flex';
        }

        const empId = document.getElementById('employeeIdInput').value;
        const name = document.getElementById('employeeNameInput').value.trim();
        const email = document.getElementById('employeeEmailInput').value.trim();
        const department = document.getElementById('employeeDeptInput').value.trim();
        const role = document.getElementById('employeeRoleSelect').value;
        const phone = document.getElementById('employeePhoneInput').value.trim();
        const password = document.getElementById('employeePasswordInput').value;

        if (!name) {
            showError('Please enter the employee full name.');
            return;
        }
        if (name.length > 100) {
            showError('Name cannot exceed 100 characters.');
            return;
        }
        if (!email) {
            showError('Please enter a work email address.');
            return;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showError('Please enter a valid email address.');
            return;
        }
        if (!department) {
            showError('Please specify the department.');
            return;
        }
        if (!role) {
            showError('Please select a role.');
            return;
        }

        const isEdit = Boolean(empId);

        if (!isEdit) {
            if (!password || password.length < 6) {
                showError('Password is required and must be at least 6 characters long.');
                return;
            }
        } else {
            // When editing, password is optional, but if entered it must be at least 6 chars
            if (password && password.trim().length > 0 && password.trim().length < 6) {
                showError('New password must be at least 6 characters long (or leave blank to keep current password).');
                return;
            }
        }

        const payload = {
            name,
            email,
            department,
            role,
            phone: phone || null
        };

        if (!isEdit) {
            payload.password = password.trim();
        } else if (password && password.trim().length > 0) {
            payload.password = password.trim();
        }

        submitBtn.disabled = true;
        btnText.textContent = isEdit ? 'Saving...' : 'Creating...';

        try {
            if (isEdit) {
                const updated = await ApiClient.put(`${AppConfig.ENDPOINTS.EMPLOYEES}/${empId}`, payload);
                closeEmployeeModal();
                showSystemToast(`Employee "${updated.name}" updated successfully.`);
            } else {
                const created = await ApiClient.post(AppConfig.ENDPOINTS.EMPLOYEES, payload);
                closeEmployeeModal();
                showSystemToast(`Employee "${created.name}" created successfully.`);
            }
            cachedTechnicians = null; // Invalidate technician dropdown cache
            loadEmployees();
        } catch (err) {
            showError(err.message || 'Operation failed. Please check inputs.');
        } finally {
            submitBtn.disabled = false;
            btnText.textContent = isEdit ? 'Save Changes' : 'Create Employee';
        }
    }

    async function handleDeactivateEmployee(empId) {
        const emp = cachedEmployees.find(e => String(e.id) === String(empId));
        const currentUser = AuthService.getUser();
        if (currentUser && String(currentUser.id) === String(empId)) {
            alert('You cannot deactivate your own administrative account.');
            return;
        }

        const empName = emp ? emp.name : 'this employee';
        if (!confirm(`Are you sure you want to deactivate ${empName}?\nThey will no longer be able to log in to SupportDesk.`)) {
            return;
        }

        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.EMPLOYEES}/${empId}/deactivate`);
            showSystemToast(`Employee "${empName}" has been deactivated.`);
            cachedTechnicians = null;
            loadEmployees();
        } catch (err) {
            showSystemToast(err.message || 'Failed to deactivate employee.', true);
        }
    }

    async function handleActivateEmployee(empId) {
        const emp = cachedEmployees.find(e => String(e.id) === String(empId));
        const empName = emp ? emp.name : 'this employee';
        if (!confirm(`Are you sure you want to reactivate ${empName}?`)) {
            return;
        }

        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.EMPLOYEES}/${empId}/activate`);
            showSystemToast(`Employee "${empName}" has been reactivated.`);
            cachedTechnicians = null;
            loadEmployees();
        } catch (err) {
            showSystemToast(err.message || 'Failed to reactivate employee.', true);
        }
    }

    // ==================== Utility & Event Listeners ====================

    function closeAllModals() {
        closeCreateTicketModal();
        closeTicketDetailModal();
        closeEmployeeModal();
    }

    function setStatValue(id, val) {
        const el = document.getElementById(id);
        if (el) el.textContent = val;
    }

    function getPriorityBadgeClass(p) {
        switch (p) {
            case 'CRITICAL': return 'badge-critical';
            case 'HIGH': return 'badge-high';
            case 'MEDIUM': return 'badge-medium';
            case 'LOW': return 'badge-low';
            default: return 'badge-medium';
        }
    }

    function getStatusBadgeClass(s) {
        switch (s) {
            case 'OPEN': return 'badge-open';
            case 'ASSIGNED': return 'badge-assigned';
            case 'IN_PROGRESS': return 'badge-in-progress';
            case 'RESOLVED': return 'badge-resolved';
            case 'CLOSED': return 'badge-closed';
            default: return 'badge-open';
        }
    }

    function getRoleBadgeClass(r) {
        switch (r) {
            case 'ADMIN': return 'badge-role-admin';
            case 'TECHNICIAN': return 'badge-role-technician';
            case 'EMPLOYEE': return 'badge-role-employee';
            default: return 'badge-role-employee';
        }
    }

    function formatActivityType(type) {
        switch (type) {
            case 'TICKET_CREATED': return 'Ticket Created';
            case 'STATUS_CHANGED': return 'Status Changed';
            case 'TECHNICIAN_ASSIGNED': return 'Technician Assigned';
            case 'COMMENT_ADDED': return 'Comment Added';
            case 'TICKET_RESOLVED': return 'Ticket Resolved';
            default: return type;
        }
    }

    function getInitials(name) {
        if (!name) return 'U';
        const parts = name.trim().split(/\s+/);
        if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }

    function formatDateTime(iso) {
        if (!iso) return '—';
        try {
            const d = new Date(iso);
            if (isNaN(d.getTime())) return iso;
            return d.toLocaleDateString(undefined, {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (_) {
            return iso;
        }
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // ==================== Initialization ====================

    function init() {
        // Reset any leftover or application-prefilled login values
        const loginForm = document.getElementById('loginForm');
        if (loginForm) loginForm.reset();
        const emailInput = document.getElementById('emailInput');
        const passwordInput = document.getElementById('passwordInput');
        if (emailInput) emailInput.value = '';
        if (passwordInput) passwordInput.value = '';

        if (AuthService.isAuthenticated()) {
            showDashboardView();
        } else {
            showLoginView();
        }

        // Login Form
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const emailInput = document.getElementById('emailInput');
                const passwordInput = document.getElementById('passwordInput');
                const alertBox = document.getElementById('loginAlertBox');
                const alertMsg = document.getElementById('loginAlertMessage');
                const submitBtn = document.getElementById('loginSubmitBtn');
                const btnText = document.getElementById('loginBtnText');

                alertBox.style.display = 'none';

                const email = emailInput.value.trim();
                const password = passwordInput.value;

                if (!email) {
                    alertMsg.textContent = 'Please enter your email address.';
                    alertBox.style.display = 'flex';
                    emailInput.focus();
                    return;
                }
                if (!password) {
                    alertMsg.textContent = 'Please enter your password.';
                    alertBox.style.display = 'flex';
                    passwordInput.focus();
                    return;
                }

                submitBtn.disabled = true;
                btnText.textContent = 'Authenticating...';

                try {
                    const response = await fetch(AppConfig.ENDPOINTS.LOGIN, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email, password })
                    });

                    if (response.ok) {
                        const data = await response.json();
                        AuthService.setToken(data.token);
                        AuthService.setUser({
                            id: data.id,
                            name: data.name,
                            email: data.email,
                            role: data.role
                        });
                        loginForm.reset();
                        showDashboardView();
                        return;
                    }

                    if (response.status === 401) {
                        let msg = 'Invalid email or password. Please verify your credentials.';
                        try {
                            const errData = await response.json();
                            if (errData && errData.message) {
                                if (errData.message.includes('deactivated')) {
                                    msg = 'Your account has been deactivated. Please contact an IT administrator.';
                                } else {
                                    msg = errData.message;
                                }
                            }
                        } catch (_) {}
                        alertMsg.textContent = msg;
                        alertBox.style.display = 'flex';
                        return;
                    }

                    if (response.status === 400) {
                        alertMsg.textContent = 'Please provide a valid email and password.';
                        alertBox.style.display = 'flex';
                        return;
                    }

                    alertMsg.textContent = `Server error (${response.status}). Please try again later.`;
                    alertBox.style.display = 'flex';

                } catch (netErr) {
                    alertMsg.textContent = 'Unable to connect to SupportDesk server. Check connection.';
                    alertBox.style.display = 'flex';
                } finally {
                    submitBtn.disabled = false;
                    btnText.textContent = 'Sign In to Dashboard';
                }
            });
        }

        // Show/Hide Password Toggle
        const togglePasswordBtn = document.getElementById('togglePasswordBtn');
        if (togglePasswordBtn) {
            togglePasswordBtn.addEventListener('click', () => {
                const passwordInput = document.getElementById('passwordInput');
                const eyeIcon = document.getElementById('eyeIcon');
                const eyeOffIcon = document.getElementById('eyeOffIcon');
                if (!passwordInput) return;
                const isPassword = passwordInput.type === 'password';
                passwordInput.type = isPassword ? 'text' : 'password';
                if (eyeIcon) eyeIcon.style.display = isPassword ? 'none' : 'block';
                if (eyeOffIcon) eyeOffIcon.style.display = isPassword ? 'block' : 'none';
                passwordInput.focus();
            });
        }

        // Logout Handlers
        document.querySelectorAll('.btn-logout').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                if (confirm('Are you sure you want to sign out?')) {
                    AuthService.clearAuth();
                    showLoginView('You have signed out successfully.');
                }
            });
        });

        // Filter & Search Handlers
        const statusFilter = document.getElementById('statusFilter');
        const priorityFilter = document.getElementById('priorityFilter');
        const categoryFilter = document.getElementById('categoryFilter');
        const searchInput = document.getElementById('ticketSearchInput');
        const clearFiltersBtn = document.getElementById('clearTicketFiltersBtn');
        const refreshBtn = document.getElementById('refreshDashboardBtn');
        const refreshTicketsBtn = document.getElementById('refreshTicketsBtn');

        if (statusFilter) statusFilter.addEventListener('change', () => applyTicketFilters());
        if (priorityFilter) priorityFilter.addEventListener('change', () => applyTicketFilters());
        if (categoryFilter) categoryFilter.addEventListener('change', () => applyTicketFilters());
        if (searchInput) searchInput.addEventListener('input', () => applyTicketFilters());
        if (clearFiltersBtn) clearFiltersBtn.addEventListener('click', resetTicketFilters);

        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                loadDashboardSummary();
                loadTickets();
                showSystemToast('Dashboard data refreshed.');
            });
        }
        if (refreshTicketsBtn) {
            refreshTicketsBtn.addEventListener('click', () => {
                loadDashboardSummary();
                loadTickets();
                showSystemToast('Tickets data refreshed.');
            });
        }

        // Create Ticket Modal Triggers
        const openCreateBtn = document.getElementById('openCreateTicketBtn');
        const openCreateBtnTickets = document.getElementById('openCreateTicketBtnTickets');
        const sidebarCreateBtn = document.getElementById('sidebarNewTicketBtn');
        const closeCreateBtn = document.getElementById('closeCreateModalBtn');
        const cancelCreateBtn = document.getElementById('cancelCreateModalBtn');
        const createForm = document.getElementById('createTicketForm');

        if (openCreateBtn) openCreateBtn.addEventListener('click', openCreateTicketModal);
        if (openCreateBtnTickets) openCreateBtnTickets.addEventListener('click', openCreateTicketModal);
        if (sidebarCreateBtn) sidebarCreateBtn.addEventListener('click', (e) => {
            e.preventDefault();
            openCreateTicketModal();
        });
        if (closeCreateBtn) closeCreateBtn.addEventListener('click', closeCreateTicketModal);
        if (cancelCreateBtn) cancelCreateBtn.addEventListener('click', closeCreateTicketModal);
        if (createForm) createForm.addEventListener('submit', handleCreateTicketSubmit);

        // Ticket Details Modal Triggers & Tabs
        const closeDetailBtn = document.getElementById('closeDetailModalBtn');
        const closeDetailFooterBtn = document.getElementById('closeDetailFooterBtn');
        const tabCommentsBtn = document.getElementById('tabCommentsBtn');
        const tabActivitiesBtn = document.getElementById('tabActivitiesBtn');
        const addCommentForm = document.getElementById('addCommentForm');

        if (closeDetailBtn) closeDetailBtn.addEventListener('click', closeTicketDetailModal);
        if (closeDetailFooterBtn) closeDetailFooterBtn.addEventListener('click', closeTicketDetailModal);
        if (tabCommentsBtn) tabCommentsBtn.addEventListener('click', () => switchDetailTab('comments'));
        if (tabActivitiesBtn) tabActivitiesBtn.addEventListener('click', () => switchDetailTab('activities'));
        if (addCommentForm) addCommentForm.addEventListener('submit', handleAddCommentSubmit);

        // Phase 7.4 Action Buttons
        const submitAssignBtn = document.getElementById('submitAssignBtn');
        const submitStatusBtn = document.getElementById('submitStatusBtn');
        const submitResolveBtn = document.getElementById('submitResolveBtn');

        if (submitAssignBtn) submitAssignBtn.addEventListener('click', handleAssignTechnician);
        if (submitStatusBtn) submitStatusBtn.addEventListener('click', handleUpdateStatus);
        if (submitResolveBtn) submitResolveBtn.addEventListener('click', handleResolveTicket);

        // Mobile Menu Toggle
        const menuToggleBtn = document.getElementById('menuToggleBtn');
        const sidebar = document.getElementById('appSidebar');
        if (menuToggleBtn && sidebar) {
            menuToggleBtn.addEventListener('click', () => sidebar.classList.toggle('mobile-open'));
        }

        // Sidebar Navigation
        const navDashboard = document.getElementById('navDashboard');
        const navTickets = document.getElementById('navTickets');
        const sidebarEmployeesBtn = document.getElementById('sidebarEmployeesBtn');
        const dashboardViewAllTicketsBtn = document.getElementById('dashboardViewAllTicketsBtn');

        if (navDashboard) navDashboard.addEventListener('click', (e) => {
            e.preventDefault();
            showDashboardSection();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
        if (navTickets) navTickets.addEventListener('click', (e) => {
            e.preventDefault();
            showTicketsSection();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
        if (dashboardViewAllTicketsBtn) dashboardViewAllTicketsBtn.addEventListener('click', (e) => {
            e.preventDefault();
            showTicketsSection();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
        if (sidebarEmployeesBtn) sidebarEmployeesBtn.addEventListener('click', (e) => {
            e.preventDefault();
            showEmployeesSection();
        });

        // Phase 7.5: Employee Administration Triggers
        const openCreateEmployeeBtn = document.getElementById('openCreateEmployeeBtn');
        const refreshEmployeesBtn = document.getElementById('refreshEmployeesBtn');
        const closeEmployeeModalBtn = document.getElementById('closeEmployeeModalBtn');
        const cancelEmployeeModalBtn = document.getElementById('cancelEmployeeModalBtn');
        const employeeForm = document.getElementById('employeeForm');
        const employeeRoleFilter = document.getElementById('employeeRoleFilter');
        const employeeDeptFilter = document.getElementById('employeeDeptFilter');
        const employeeSearchInput = document.getElementById('employeeSearchInput');

        if (openCreateEmployeeBtn) openCreateEmployeeBtn.addEventListener('click', openAddEmployeeModal);
        if (refreshEmployeesBtn) refreshEmployeesBtn.addEventListener('click', () => {
            loadEmployees();
            showSystemToast('Employee directory refreshed.');
        });
        if (closeEmployeeModalBtn) closeEmployeeModalBtn.addEventListener('click', closeEmployeeModal);
        if (cancelEmployeeModalBtn) cancelEmployeeModalBtn.addEventListener('click', closeEmployeeModal);
        if (employeeForm) employeeForm.addEventListener('submit', handleEmployeeFormSubmit);
        if (employeeRoleFilter) employeeRoleFilter.addEventListener('change', () => loadEmployees());
        if (employeeDeptFilter) employeeDeptFilter.addEventListener('change', () => loadEmployees());
        if (employeeSearchInput) employeeSearchInput.addEventListener('input', () => renderEmployeesTable());

        // Phase 7.6: In-App Notification Triggers
        const notificationBellBtn = document.getElementById('notificationBellBtn');
        const markAllReadBtn = document.getElementById('markAllReadBtn');

        if (notificationBellBtn) {
            notificationBellBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleNotificationDropdown();
            });
        }
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                markAllNotificationsAsRead();
            });
        }

        // Close notification dropdown when clicking outside
        document.addEventListener('click', (e) => {
            const container = document.getElementById('notificationContainer');
            const dropdown = document.getElementById('notificationDropdown');
            if (container && dropdown && dropdown.style.display !== 'none' && !container.contains(e.target)) {
                toggleNotificationDropdown(false);
            }
        });
    }

    // ==================== Phase 7.6: In-App Notifications ====================

    async function loadUnreadCount() {
        if (!AuthService.isAuthenticated()) return;
        try {
            const data = await ApiClient.get(AppConfig.ENDPOINTS.NOTIFICATIONS_UNREAD_COUNT);
            const count = (data && data.unreadCount) || 0;
            const badge = document.getElementById('notificationBadge');
            const countTag = document.getElementById('notificationCountTag');

            if (badge) {
                if (count > 0) {
                    badge.textContent = count > 99 ? '99+' : count;
                    badge.style.display = 'inline-flex';
                } else {
                    badge.style.display = 'none';
                }
            }

            if (countTag) {
                countTag.textContent = `${count} unread`;
            }
        } catch (err) {
            console.warn('Could not load unread notification count:', err);
        }
    }

    async function loadNotifications() {
        const listEl = document.getElementById('notificationList');
        if (listEl) {
            listEl.innerHTML = '<div class="notification-loading">Loading notifications...</div>';
        }

        try {
            const data = await ApiClient.get(AppConfig.ENDPOINTS.NOTIFICATIONS);
            notifications = Array.isArray(data) ? data : [];
            renderNotifications();
        } catch (err) {
            if (listEl) {
                listEl.innerHTML = `<div class="notification-empty" style="color: var(--priority-critical);">Failed to load notifications: ${escapeHtml(err.message)}</div>`;
            }
        }
    }

    function renderNotifications() {
        const listEl = document.getElementById('notificationList');
        if (!listEl) return;

        if (!notifications || notifications.length === 0) {
            listEl.innerHTML = '<div class="notification-empty">No notifications yet</div>';
            return;
        }

        listEl.innerHTML = '';
        notifications.forEach(item => {
            const itemEl = document.createElement('div');
            itemEl.className = 'notification-item' + (item.read ? ' read' : ' unread');

            let badgeClass = 'notif-type-status';
            let badgeLabel = 'Update';
            if (item.type === 'TECHNICIAN_ASSIGNED') {
                badgeClass = 'notif-type-assigned';
                badgeLabel = 'Assigned';
            } else if (item.type === 'STATUS_CHANGED') {
                badgeClass = 'notif-type-status';
                badgeLabel = 'Status';
            } else if (item.type === 'COMMENT_ADDED') {
                badgeClass = 'notif-type-comment';
                badgeLabel = 'Comment';
            } else if (item.type === 'TICKET_RESOLVED') {
                badgeClass = 'notif-type-resolved';
                badgeLabel = 'Resolved';
            }

            const timeStr = formatTimeAgo(item.createdAt);
            const ticketPill = item.ticketNumber ? `<span class="notification-ticket-pill">#${escapeHtml(item.ticketNumber)}</span>` : '';

            itemEl.innerHTML = `
                <div class="notification-item-top">
                    <span class="notification-item-title">
                        <span class="notification-type-badge ${badgeClass}">${badgeLabel}</span>
                        <span>${escapeHtml(item.title)}</span>
                    </span>
                    <span style="font-size: 11px; color: var(--text-light);">${timeStr}</span>
                </div>
                <div class="notification-item-msg">${escapeHtml(item.message)}</div>
                ${ticketPill ? `<div class="notification-item-bottom">${ticketPill} <span style="font-size: 10.5px; color: var(--primary);">View details →</span></div>` : ''}
            `;

            itemEl.addEventListener('click', async () => {
                if (!item.read) {
                    await markNotificationAsRead(item.id);
                }
                if (item.ticketId) {
                    toggleNotificationDropdown(false);
                    openTicketDetails(item.ticketId);
                }
            });

            listEl.appendChild(itemEl);
        });
    }

    async function markNotificationAsRead(id) {
        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.NOTIFICATIONS}/${id}/read`);
            const item = notifications.find(n => n.id === id);
            if (item) {
                item.read = true;
            }
            renderNotifications();
            loadUnreadCount();
        } catch (err) {
            console.error('Failed to mark notification as read:', err);
        }
    }

    async function markAllNotificationsAsRead() {
        try {
            await ApiClient.patch(`${AppConfig.ENDPOINTS.NOTIFICATIONS}/read-all`);
            notifications.forEach(n => n.read = true);
            renderNotifications();
            loadUnreadCount();
            showSystemToast('All notifications marked as read.');
        } catch (err) {
            showSystemToast(`Failed to mark all as read: ${err.message}`, true);
        }
    }

    function toggleNotificationDropdown(forceState) {
        const dropdown = document.getElementById('notificationDropdown');
        if (!dropdown) return;

        const shouldOpen = forceState !== undefined ? forceState : (dropdown.style.display === 'none' || dropdown.style.display === '');

        if (shouldOpen) {
            dropdown.style.display = 'flex';
            loadNotifications();
        } else {
            dropdown.style.display = 'none';
        }
    }

    function formatTimeAgo(isoDate) {
        if (!isoDate) return '';
        try {
            const date = new Date(isoDate);
            const now = new Date();
            const diffMs = now - date;
            const diffSecs = Math.floor(diffMs / 1000);
            const diffMins = Math.floor(diffSecs / 60);
            const diffHours = Math.floor(diffMins / 60);
            const diffDays = Math.floor(diffHours / 24);

            if (diffSecs < 60) return 'Just now';
            if (diffMins < 60) return `${diffMins}m ago`;
            if (diffHours < 24) return `${diffHours}h ago`;
            if (diffDays < 7) return `${diffDays}d ago`;
            return date.toLocaleDateString();
        } catch (_) {
            return isoDate;
        }
    }

    return {
        init,
        showLoginView,
        showDashboardView,
        showDashboardSection,
        showTicketsSection,
        showEmployeesSection,
        openTicketDetails,
        openCreateTicketModal,
        openAddEmployeeModal,
        openEditEmployeeModal,
        loadNotifications,
        loadUnreadCount,
        toggleNotificationDropdown
    };
})();

window.App = App;
document.addEventListener('DOMContentLoaded', () => App.init());

/**
 * SupportDesk - Authenticated API Client
 * Phase 7.3: Same-Page SPA Architecture
 * 
 * Central HTTP client for sending requests with in-memory JWT authorization.
 * Handles:
 * - 401 Unauthorized: Clears in-memory session and renders login view.
 * - 403 Forbidden: Throws detailed access-denied message from backend.
 * - 400 / 404 / 500: Extracts server error messages.
 * - Network errors: Clear user notification.
 */

const ApiClient = {
    /**
     * Executes a fetch request with in-memory JWT Bearer header.
     * @param {string} endpoint - API path (e.g. '/api/tickets')
     * @param {object} [options={}] - Fetch options
     * @returns {Promise<any>}
     */
    async request(endpoint, options = {}) {
        const token = AuthService.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        let response;
        try {
            response = await fetch(endpoint, {
                ...options,
                headers
            });
        } catch (netErr) {
            console.error('Network connection error:', netErr);
            throw new Error('Unable to connect to the SupportDesk server. Please check your network connection.');
        }

        // 1. HTTP 401 Unauthorized (Expired or invalid token)
        if (response.status === 401) {
            AuthService.clearAuth();
            if (window.App && typeof window.App.showLoginView === 'function') {
                window.App.showLoginView('Your session has expired. Please sign in again.');
            }
            throw new Error('401 Unauthorized: Session expired.');
        }

        // 2. HTTP 403 Forbidden (RBAC Access Denied)
        if (response.status === 403) {
            let errorMsg = 'Access Denied: You do not have permission to perform this action or view this resource.';
            try {
                const errorData = await response.json();
                if (errorData && errorData.message) {
                    errorMsg = errorData.message;
                }
            } catch (_) {}
            throw new Error(errorMsg);
        }

        // 3. HTTP 404 Not Found
        if (response.status === 404) {
            let errorMsg = 'Requested resource not found.';
            try {
                const errorData = await response.json();
                if (errorData && errorData.message) {
                    errorMsg = errorData.message;
                }
            } catch (_) {}
            throw new Error(errorMsg);
        }

        // 4. Other Non-OK Responses (400, 500, etc.)
        if (!response.ok) {
            let errorMsg = `Server error (${response.status})`;
            try {
                const errorData = await response.json();
                if (errorData && errorData.validationErrors) {
                    const firstError = Object.values(errorData.validationErrors)[0];
                    if (firstError) errorMsg = firstError;
                } else if (errorData && errorData.message) {
                    errorMsg = errorData.message;
                } else if (errorData && errorData.error) {
                    errorMsg = errorData.error;
                }
            } catch (_) {}
            throw new Error(errorMsg);
        }

        if (response.status === 204) {
            return {};
        }

        return await response.json();
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    patch(endpoint, body) {
        return this.request(endpoint, {
            method: 'PATCH',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// Expose globally
window.ApiClient = ApiClient;

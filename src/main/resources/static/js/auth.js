/**
 * SupportDesk - In-Memory Authentication Service
 * Phase 7.3 Same-Page SPA Architecture
 * 
 * Rules strictly followed:
 * - JWT token and user identity are kept ONLY in JavaScript private closure memory.
 * - Never stored in window.name, localStorage, sessionStorage, or cookies.
 * - Pure same-page authentication/dashboard flow in index.html.
 * - If page is reloaded, in-memory state is naturally reset and login screen is shown.
 * - Credentials and tokens are never logged or exposed.
 */

const AuthService = (function() {
    // Private in-memory session variables (pure closure memory, NEVER stored)
    let _token = null;
    let _user = null;

    return {
        /**
         * Get the current in-memory JWT token.
         * @returns {string|null}
         */
        getToken() {
            return _token;
        },

        /**
         * Set the in-memory JWT token.
         * @param {string|null} token
         */
        setToken(token) {
            _token = token;
        },

        /**
         * Get the current in-memory user details object { id, name, email, role }.
         * @returns {object|null}
         */
        getUser() {
            return _user;
        },

        /**
         * Set the in-memory user details object.
         * @param {object|null} user
         */
        setUser(user) {
            _user = user;
        },

        /**
         * Check if an active in-memory session exists.
         * @returns {boolean}
         */
        isAuthenticated() {
            return Boolean(_token && _user);
        },

        /**
         * Get the role string of the authenticated user.
         * @returns {string|null}
         */
        getRole() {
            return _user ? _user.role : null;
        },

        /**
         * Clear all authentication data from memory.
         */
        clearAuth() {
            _token = null;
            _user = null;
        }
    };
})();

// Expose globally
window.AuthService = AuthService;

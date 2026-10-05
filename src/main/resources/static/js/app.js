// Spring Boot Integrated Platform Frontend Controller

function getApiBase() {
    return localStorage.getItem('custom_api_base') || '/api/v1';
}

// Application State
const state = {
    auth: {
        accessToken: localStorage.getItem('access_token') || null,
        refreshToken: localStorage.getItem('refresh_token') || null,
        username: localStorage.getItem('username') || null,
        roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    },
    posts: {
        page: 0,
        size: 6,
        sortBy: 'createdAt',
        sortDir: 'desc',
        category: '',
        search: '',
        data: null
    },
    lastCorrelationId: 'NONE'
};

// Initialization on DOM load
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initSession();
    fetchTraceInfo();
    fetchPosts(0);
    fetchSchedules();
    fetchCacheStats();
});

// ================= API CLIENT WITH TRACING & CORRELATION =================

async function apiRequest(endpoint, options = {}) {
    const base = getApiBase();
    const url = endpoint.startsWith('http') ? endpoint : `${base}${endpoint}`;
    const headers = options.headers || {};

    // Attach JWT if present
    if (state.auth.accessToken && !headers['Authorization']) {
        headers['Authorization'] = `Bearer ${state.auth.accessToken}`;
    }

    if (!headers['Content-Type'] && !(options.body instanceof FormData) && options.body) {
        headers['Content-Type'] = 'application/json';
    }

    const config = {
        ...options,
        headers
    };

    const startTime = performance.now();
    try {
        const response = await fetch(url, config);
        const duration = Math.round(performance.now() - startTime);

        // Extract Correlation ID from response headers
        const cid = response.headers.get('X-Correlation-ID');
        if (cid) {
            updateCorrelationId(cid);
        }

        const data = await response.json().catch(() => null);

        // Update inspector view if open
        updateInspector(response.status, cid, data);

        return {
            ok: response.ok,
            status: response.status,
            data: data,
            duration: duration,
            cid: cid
        };
    } catch (err) {
        const duration = Math.round(performance.now() - startTime);
        showToast('Network error or server unreachable', 'error');
        updateInspector(0, state.lastCorrelationId, { error: err.message });
        return {
            ok: false,
            status: 0,
            data: { error: err.message },
            duration: duration,
            cid: state.lastCorrelationId
        };
    }
}

function updateCorrelationId(cid) {
    if (!cid) return;
    state.lastCorrelationId = cid;
    const badge = document.getElementById('header-cid');
    if (badge) {
        badge.textContent = cid.substring(0, 8) + '...';
        badge.title = cid;
    }
}

function updateInspector(status, cid, data) {
    const codeEl = document.getElementById('inspector-code');
    const cidEl = document.getElementById('inspector-cid');
    const jsonEl = document.getElementById('inspector-json');
    const statusBadge = document.getElementById('inspector-status-badge');

    if (codeEl) codeEl.textContent = status || 'ERR';
    if (cidEl) cidEl.textContent = cid || 'NONE';
    if (jsonEl) jsonEl.textContent = JSON.stringify(data, null, 2);

    if (statusBadge) {
        if (status >= 200 && status < 300) {
            statusBadge.className = 'badge badge-success';
            statusBadge.textContent = '200 OK';
        } else if (status >= 400 && status < 500) {
            statusBadge.className = 'badge badge-warn';
            statusBadge.textContent = `${status} Client Error`;
        } else {
            statusBadge.className = 'badge badge-danger';
            statusBadge.textContent = `${status} Server Error`;
        }
    }
}

// ================= NAVIGATION TABS =================

function initTabs() {
    const tabs = document.querySelectorAll('.nav-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

            tab.classList.add('active');
            const target = tab.getAttribute('data-tab');
            const pane = document.getElementById(target);
            if (pane) pane.classList.add('active');

            if (target === 'tab-schedules') {
                populatePostSelectForSchedule();
                fetchSchedules();
            } else if (target === 'tab-crypto') {
                fetchCredentials();
            } else if (target === 'tab-performance') {
                fetchCacheStats();
            }
        });
    });
}

// ================= AUTHENTICATION & JWT =================

function initSession() {
    renderUserWidget();
    renderSessionDetails();
}

function switchAuthForm(type) {
    const formLogin = document.getElementById('form-login');
    const formReg = document.getElementById('form-register');
    const btnLogin = document.getElementById('btn-show-login');
    const btnReg = document.getElementById('btn-show-register');

    if (type === 'login') {
        formLogin.classList.remove('hidden');
        formReg.classList.add('hidden');
        btnLogin.classList.add('active');
        btnReg.classList.remove('active');
    } else {
        formLogin.classList.add('hidden');
        formReg.classList.remove('hidden');
        btnLogin.classList.remove('active');
        btnReg.classList.add('active');
    }
}

function fillLoginForm(username, password) {
    document.getElementById('login-username').value = username;
    document.getElementById('login-password').value = password;
    switchAuthForm('login');
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;

    const res = await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });

    if (res.ok && res.data.success) {
        const payload = res.data.data;
        saveAuthSession(payload);
        showToast(`Signed in successfully as ${payload.username}`, 'success');
        initSession();
        fetchPosts(0);
    } else {
        showToast(res.data?.message || 'Login failed', 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;

    const res = await apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password, role })
    });

    if (res.ok && res.data.success) {
        const payload = res.data.data;
        saveAuthSession(payload);
        showToast(`Account registered for ${payload.username}`, 'success');
        initSession();
        switchAuthForm('login');
    } else {
        showToast(res.data?.message || 'Registration failed', 'error');
    }
}

function saveAuthSession(payload) {
    state.auth.accessToken = payload.accessToken;
    state.auth.refreshToken = payload.refreshToken;
    state.auth.username = payload.username;
    state.auth.roles = payload.roles || [];

    localStorage.setItem('access_token', payload.accessToken);
    localStorage.setItem('refresh_token', payload.refreshToken);
    localStorage.setItem('username', payload.username);
    localStorage.setItem('roles', JSON.stringify(payload.roles || []));
}

async function handleRefreshToken() {
    if (!state.auth.refreshToken) {
        showToast('No refresh token present in session', 'warn');
        return;
    }

    const res = await apiRequest('/auth/refresh-token', {
        method: 'POST',
        body: JSON.stringify({ refreshToken: state.auth.refreshToken })
    });

    if (res.ok && res.data.success) {
        saveAuthSession(res.data.data);
        showToast('Access token refreshed and rotated successfully', 'success');
        renderSessionDetails();
    } else {
        showToast(res.data?.message || 'Token rotation failed', 'error');
        handleLogout();
    }
}

async function handleLogout() {
    if (state.auth.accessToken) {
        await apiRequest('/auth/logout', { method: 'POST' });
    }
    state.auth.accessToken = null;
    state.auth.refreshToken = null;
    state.auth.username = null;
    state.auth.roles = [];

    localStorage.clear();
    showToast('Signed out of session', 'info');
    initSession();
}

function renderUserWidget() {
    const container = document.getElementById('user-status-widget');
    if (!container) return;

    if (state.auth.username) {
        const roleLabel = state.auth.roles.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER';
        container.innerHTML = `
            <span class="badge ${roleLabel === 'ADMIN' ? 'badge-primary' : 'badge-neutral'}">${state.auth.username} (${roleLabel})</span>
            <button class="btn btn-outline btn-xs" onclick="handleLogout()">Logout</button>
        `;
    } else {
        container.innerHTML = `
            <span class="badge badge-neutral">Not Signed In</span>
        `;
    }
}

function renderSessionDetails() {
    const activeView = document.getElementById('session-active-view');
    const emptyView = document.getElementById('session-empty-view');
    const sessionBadge = document.getElementById('session-badge');

    if (!activeView || !emptyView) return;

    if (state.auth.username) {
        activeView.classList.remove('hidden');
        emptyView.classList.add('hidden');

        document.getElementById('session-user').textContent = state.auth.username;
        sessionBadge.textContent = 'Authenticated';
        sessionBadge.className = 'badge badge-success';

        const rolesContainer = document.getElementById('session-roles');
        rolesContainer.innerHTML = state.auth.roles.map(r => `<span class="badge badge-primary">${r}</span>`).join(' ');

        document.getElementById('session-access-token').textContent = state.auth.accessToken;
        document.getElementById('session-refresh-token').textContent = state.auth.refreshToken;
    } else {
        activeView.classList.add('hidden');
        emptyView.classList.remove('hidden');
        sessionBadge.textContent = 'Guest';
        sessionBadge.className = 'badge badge-neutral';
    }
}

// ================= POSTS MANAGEMENT (CRUD + PAGINATION + SORTING) =================

let postSearchTimeout = null;
function debounceFetchPosts() {
    clearTimeout(postSearchTimeout);
    postSearchTimeout = setTimeout(() => {
        fetchPosts(0);
    }, 300);
}

function resetPostFilters() {
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-category').value = '';
    document.getElementById('filter-sort-by').value = 'createdAt';
    document.getElementById('filter-sort-dir').value = 'desc';
    document.getElementById('filter-page-size').value = '6';
    fetchPosts(0);
}

async function fetchPosts(page = 0) {
    state.posts.page = page;
    state.posts.size = parseInt(document.getElementById('filter-page-size')?.value || '6');
    state.posts.sortBy = document.getElementById('filter-sort-by')?.value || 'createdAt';
    state.posts.sortDir = document.getElementById('filter-sort-dir')?.value || 'desc';
    state.posts.category = document.getElementById('filter-category')?.value || '';
    state.posts.search = document.getElementById('filter-search')?.value.trim() || '';

    const params = new URLSearchParams({
        page: state.posts.page,
        size: state.posts.size,
        sortBy: state.posts.sortBy,
        sortDir: state.posts.sortDir
    });

    if (state.posts.category) params.append('category', state.posts.category);
    if (state.posts.search) params.append('search', state.posts.search);

    const res = await apiRequest(`/posts?${params.toString()}`);
    if (res.ok && res.data.success) {
        state.posts.data = res.data.data;
        renderPostsGrid(res.data.data);
        renderPagination(res.data.data);
    }
}

function renderPostsGrid(pagedData) {
    const container = document.getElementById('posts-container');
    if (!container) return;

    if (!pagedData.content || pagedData.content.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">📄</div>
                <h4>No Posts Found</h4>
                <p>No content matching your current filter criteria. Create a post using the top button.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = pagedData.content.map(post => {
        const tags = (post.tags || '').split(',').map(t => t.trim()).filter(Boolean);
        const canEdit = state.auth.username && (state.auth.username === post.authorUsername || state.auth.roles.includes('ROLE_ADMIN'));

        return `
            <div class="post-card" id="post-${post.id}">
                <div class="post-card-body">
                    <div class="post-card-meta">
                        <span class="post-category-tag">${escapeHtml(post.category)}</span>
                        <span class="post-views">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                            ${post.viewCount} views
                        </span>
                    </div>
                    <h3 class="post-card-title">${escapeHtml(post.title)}</h3>
                    <p class="post-card-content">${escapeHtml(post.content)}</p>
                    <div class="post-tags-list">
                        ${tags.map(t => `<span class="tag-badge">#${escapeHtml(t)}</span>`).join('')}
                    </div>
                </div>

                <!-- Comments Container -->
                <div class="post-comments-container">
                    <div class="comments-header">
                        <span>Comments (${post.commentCount})</span>
                    </div>
                    <div class="comments-list">
                        ${(post.comments || []).slice(0, 2).map(c => `
                            <div class="comment-item">
                                <span class="comment-author">${escapeHtml(c.authorName)}:</span> ${escapeHtml(c.content)}
                            </div>
                        `).join('')}
                    </div>
                    ${state.auth.accessToken ? `
                        <div class="comment-input-row">
                            <input type="text" id="comment-input-${post.id}" class="form-control form-control-sm" placeholder="Write a comment...">
                            <button class="btn btn-outline btn-xs" onclick="handleAddComment(${post.id})">Post</button>
                        </div>
                    ` : ''}
                </div>

                <div class="post-card-footer">
                    <span class="post-author">By <strong>${escapeHtml(post.authorUsername)}</strong></span>
                    <div class="post-actions">
                        ${canEdit ? `
                            <button class="btn btn-outline btn-xs" onclick="openEditPostModal(${post.id})">Edit</button>
                            <button class="btn btn-danger-outline btn-xs" onclick="handleDeletePost(${post.id})">Delete</button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderPagination(pagedData) {
    const bar = document.getElementById('pagination-bar');
    if (!bar) return;

    const totalPages = pagedData.totalPages || 1;
    const currentPage = pagedData.pageNumber || 0;
    const totalElements = pagedData.totalElements || 0;

    bar.innerHTML = `
        <div class="text-muted" style="font-size: 0.85rem;">
            Showing page <strong>${currentPage + 1}</strong> of <strong>${totalPages}</strong> (${totalElements} total posts)
        </div>
        <div class="pagination-controls">
            <button class="btn btn-outline btn-sm" ${currentPage === 0 ? 'disabled' : ''} onclick="fetchPosts(${currentPage - 1})">Previous</button>
            <button class="btn btn-outline btn-sm" ${pagedData.last ? 'disabled' : ''} onclick="fetchPosts(${currentPage + 1})">Next</button>
        </div>
    `;
}

function toggleCreatePostModal(open) {
    const modal = document.getElementById('modal-post');
    const form = document.getElementById('form-post-modal');
    const errBox = document.getElementById('modal-post-validation-error');

    if (open) {
        if (!state.auth.accessToken) {
            showToast('Please sign in first to create a post', 'warn');
            return;
        }
        form.reset();
        document.getElementById('modal-post-id').value = '';
        document.getElementById('modal-post-title').textContent = 'Create New Resource Post';
        errBox.classList.add('hidden');
        modal.classList.remove('hidden');
    } else {
        modal.classList.add('hidden');
    }
}

async function openEditPostModal(id) {
    const res = await apiRequest(`/posts/${id}`);
    if (res.ok && res.data.success) {
        const post = res.data.data;
        document.getElementById('modal-post-id').value = post.id;
        document.getElementById('post-title').value = post.title;
        document.getElementById('post-category').value = post.category;
        document.getElementById('post-tags').value = post.tags || '';
        document.getElementById('post-content').value = post.content;
        document.getElementById('modal-post-title').textContent = `Edit Post #${post.id}`;
        document.getElementById('modal-post-validation-error').classList.add('hidden');
        document.getElementById('modal-post').classList.remove('hidden');
    }
}

async function handlePostFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('modal-post-id').value;
    const title = document.getElementById('post-title').value.trim();
    const category = document.getElementById('post-category').value;
    const tags = document.getElementById('post-tags').value.trim();
    const content = document.getElementById('post-content').value.trim();
    const errBox = document.getElementById('modal-post-validation-error');

    const payload = { title, category, tags, content };
    const method = id ? 'PUT' : 'POST';
    const endpoint = id ? `/posts/${id}` : '/posts';

    const res = await apiRequest(endpoint, {
        method,
        body: JSON.stringify(payload)
    });

    if (res.ok && res.data.success) {
        showToast(res.data.message || 'Post saved successfully', 'success');
        toggleCreatePostModal(false);
        fetchPosts(state.posts.page);
    } else {
        // Display bean validation errors in modal
        errBox.classList.remove('hidden');
        if (res.data?.validationErrors) {
            errBox.innerHTML = '<strong>Validation Errors:</strong><ul style="margin-left: 1.2rem; margin-top: 0.3rem;">' +
                Object.entries(res.data.validationErrors).map(([k, v]) => `<li>${k}: ${v}</li>`).join('') +
                '</ul>';
        } else {
            errBox.textContent = res.data?.message || 'Failed to save post';
        }
    }
}

async function handleDeletePost(id) {
    if (!confirm(`Are you sure you want to delete post #${id}?`)) return;

    const res = await apiRequest(`/posts/${id}`, { method: 'DELETE' });
    if (res.ok) {
        showToast('Post deleted successfully', 'success');
        fetchPosts(state.posts.page);
    } else {
        showToast(res.data?.message || 'Delete failed', 'error');
    }
}

async function handleAddComment(postId) {
    const input = document.getElementById(`comment-input-${postId}`);
    const content = input?.value.trim();
    if (!content) return;

    const res = await apiRequest(`/posts/${postId}/comments`, {
        method: 'POST',
        body: JSON.stringify({ content })
    });

    if (res.ok) {
        input.value = '';
        showToast('Comment added', 'success');
        fetchPosts(state.posts.page);
    } else {
        showToast(res.data?.message || 'Failed to add comment', 'error');
    }
}

// ================= SOCIAL SCHEDULING =================

async function populatePostSelectForSchedule() {
    const select = document.getElementById('sched-post-id');
    if (!select) return;

    const res = await apiRequest('/posts?page=0&size=50');
    if (res.ok && res.data?.success) {
        select.innerHTML = '<option value="">Choose a post to schedule...</option>' +
            res.data.data.content.map(p => `<option value="${p.id}">#${p.id} - ${escapeHtml(p.title)}</option>`).join('');
    }
}

async function fetchSchedules() {
    const res = await apiRequest('/schedules');
    const tbody = document.getElementById('schedules-table-body');
    if (!tbody) return;

    if (res.ok && res.data.success && res.data.data.length > 0) {
        tbody.innerHTML = res.data.data.map(s => {
            const statusClass = s.status === 'PUBLISHED' ? 'badge-success' : s.status === 'FAILED' ? 'badge-danger' : 'badge-warn';
            return `
                <tr>
                    <td class="mono">#${s.id}</td>
                    <td><strong>${escapeHtml(s.postTitle)}</strong></td>
                    <td><span class="badge badge-primary">${escapeHtml(s.platform)}</span></td>
                    <td>${new Date(s.scheduledTime).toLocaleString()}</td>
                    <td><span class="badge ${statusClass}">${escapeHtml(s.status)}</span></td>
                    <td>
                        <div style="display: flex; gap: 0.3rem;">
                            <button class="btn btn-outline btn-xs" onclick="updateScheduleStatus(${s.id}, 'PUBLISHED')">Publish</button>
                            <button class="btn btn-danger-outline btn-xs" onclick="deleteSchedule(${s.id})">Delete</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    } else {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No scheduled broadcasts available.</td></tr>`;
    }
}

async function handleCreateSchedule(e) {
    e.preventDefault();
    if (!state.auth.accessToken) {
        showToast('Please sign in first to schedule posts', 'warn');
        return;
    }

    const postId = document.getElementById('sched-post-id').value;
    const platform = document.getElementById('sched-platform').value;
    const scheduledTime = document.getElementById('sched-time').value;
    const notes = document.getElementById('sched-notes').value.trim();

    const res = await apiRequest('/schedules', {
        method: 'POST',
        body: JSON.stringify({ postId, platform, scheduledTime, notes })
    });

    if (res.ok && res.data.success) {
        showToast('Broadcast scheduled successfully', 'success');
        document.getElementById('form-create-schedule').reset();
        fetchSchedules();
    } else {
        showToast(res.data?.message || 'Scheduling failed', 'error');
    }
}

async function updateScheduleStatus(id, status) {
    const res = await apiRequest(`/schedules/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status })
    });
    if (res.ok) {
        showToast(`Status updated to ${status}`, 'success');
        fetchSchedules();
    }
}

async function deleteSchedule(id) {
    const res = await apiRequest(`/schedules/${id}`, { method: 'DELETE' });
    if (res.ok) {
        showToast('Schedule removed', 'success');
        fetchSchedules();
    }
}

// ================= PERFORMANCE & CACHING LAB =================

async function runNPlusOneBenchmark() {
    const unoptTime = document.getElementById('bench-unopt-time');
    const optTime = document.getElementById('bench-opt-time');
    const banner = document.getElementById('bench-speedup');

    unoptTime.textContent = '...';
    optTime.textContent = '...';
    banner.classList.add('hidden');

    const resUnopt = await apiRequest('/system/benchmark/n-plus-one');
    const resOpt = await apiRequest('/system/benchmark/join-fetch');

    if (resUnopt.ok && resOpt.ok) {
        const u = resUnopt.data.data;
        const o = resOpt.data.data;

        unoptTime.textContent = u.executionTimeMs;
        document.getElementById('bench-unopt-queries').textContent = u.queryCountEstimated;
        document.getElementById('bench-unopt-desc').textContent = u.description;

        optTime.textContent = o.executionTimeMs;
        document.getElementById('bench-opt-queries').textContent = o.queryCountEstimated;
        document.getElementById('bench-opt-desc').textContent = o.description;

        const diff = u.executionTimeMs - o.executionTimeMs;
        banner.classList.remove('hidden');
        banner.innerHTML = `
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>
            <span>JOIN FETCH eliminated <strong>${u.queryCountEstimated - 1} database round-trips</strong>, loading all relations in a single query!</span>
        `;
    }
}

async function fetchCacheStats() {
    const res = await apiRequest('/system/cache-stats');
    if (res.ok && res.data.success) {
        const stats = res.data.data;
        document.getElementById('cache-stat-hits').textContent = stats.hitCount ?? 0;
        document.getElementById('cache-stat-misses').textContent = stats.missCount ?? 0;
        document.getElementById('cache-stat-rate').textContent = stats.hitRate ?? '0.0%';
        document.getElementById('cache-stat-size').textContent = stats.estimatedSize ?? 0;
    }
}

async function clearPlatformCache() {
    const res = await apiRequest('/system/cache-clear', { method: 'POST' });
    if (res.ok) {
        showToast('Caffeine cache cleared', 'success');
        fetchCacheStats();
    }
}

async function testCacheFetch() {
    const id = document.getElementById('cache-test-id').value;
    const resultBox = document.getElementById('cache-test-result');

    const start = performance.now();
    const res = await apiRequest(`/posts/${id}`);
    const duration = Math.round(performance.now() - start);

    resultBox.classList.remove('hidden');
    if (res.ok && res.data.success) {
        const post = res.data.data;
        resultBox.innerHTML = `
            <div><strong>Fetched Post #${post.id}:</strong> ${escapeHtml(post.title)}</div>
            <div style="margin-top: 0.4rem;">
                <span class="badge ${duration < 15 ? 'badge-success' : 'badge-warn'}">Response Time: ${duration}ms</span>
                <span class="text-muted" style="margin-left: 0.5rem;">${duration < 15 ? 'Served from in-memory Caffeine Cache' : 'Database query executed on cache miss'}</span>
            </div>
        `;
        fetchCacheStats();
    } else {
        resultBox.innerHTML = `<span class="text-warn">Post not found with ID ${id}</span>`;
    }
}

async function fetchNativeAnalytics() {
    const res = await apiRequest('/posts/analytics/native');
    const tbody = document.getElementById('native-analytics-table-body');
    if (!tbody) return;

    if (res.ok && res.data.success && res.data.data.length > 0) {
        tbody.innerHTML = res.data.data.map(row => `
            <tr>
                <td><strong>${escapeHtml(row.category)}</strong></td>
                <td><span class="badge badge-primary">${row.postCount}</span></td>
                <td>${row.totalViews}</td>
                <td>${Number(row.avgViews).toFixed(1)}</td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">No analytical data returned.</td></tr>`;
    }
}

// ================= AES ENCRYPTED CREDENTIALS VAULT =================

async function fetchCredentials() {
    const list = document.getElementById('credentials-list');
    if (!list) return;

    if (!state.auth.accessToken) {
        list.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔒</div>
                <h4>Authentication Required</h4>
                <p>Sign in to view or manage your encrypted OAuth credentials.</p>
            </div>
        `;
        return;
    }

    const res = await apiRequest('/credentials');
    if (res.ok && res.data.success && res.data.data.length > 0) {
        list.innerHTML = res.data.data.map(c => `
            <div class="credential-item">
                <div class="credential-info">
                    <span class="provider-badge">${escapeHtml(c.provider)} API</span>
                    <div>Client ID: <span class="mono">${escapeHtml(c.clientId)}</span></div>
                    <div>Secret: <span class="mono text-muted">${escapeHtml(c.maskedClientSecret)}</span></div>
                    <div>Access Token: <span class="mono text-muted">${escapeHtml(c.maskedAccessToken)}</span></div>
                </div>
                <div style="display: flex; flex-direction: column; gap: 0.35rem;">
                    <button class="btn btn-outline btn-xs" onclick="revealCredentialSecret(${c.id})">Decrypt Secret</button>
                    <button class="btn btn-danger-outline btn-xs" onclick="deleteCredential(${c.id})">Delete</button>
                </div>
            </div>
        `).join('');
    } else {
        list.innerHTML = `<div class="empty-state"><p>No credentials stored yet. Add one using the form on the right.</p></div>`;
    }
}

async function handleStoreCredential(e) {
    e.preventDefault();
    if (!state.auth.accessToken) {
        showToast('Please sign in first to store credentials', 'warn');
        return;
    }

    const provider = document.getElementById('cred-provider').value;
    const clientId = document.getElementById('cred-client-id').value.trim();
    const clientSecret = document.getElementById('cred-secret').value.trim();
    const accessToken = document.getElementById('cred-token').value.trim();
    const refreshToken = document.getElementById('cred-refresh').value.trim();

    const res = await apiRequest('/credentials', {
        method: 'POST',
        body: JSON.stringify({ provider, clientId, clientSecret, accessToken, refreshToken })
    });

    if (res.ok && res.data.success) {
        showToast('OAuth credentials encrypted with AES-256 and stored safely', 'success');
        document.getElementById('form-create-cred').reset();
        fetchCredentials();
    } else {
        showToast(res.data?.message || 'Failed to store credentials', 'error');
    }
}

async function revealCredentialSecret(id) {
    const res = await apiRequest(`/credentials/${id}/decrypt`);
    if (res.ok && res.data.success) {
        const c = res.data.data;
        document.getElementById('modal-dec-provider').value = `${c.provider} (${c.clientId})`;
        document.getElementById('modal-dec-secret').value = c.decryptedClientSecret;
        document.getElementById('modal-dec-token').value = c.decryptedAccessToken;
        toggleDecryptModal(true);
    } else {
        showToast(res.data?.message || 'Access denied or decryption failed', 'error');
    }
}

function toggleDecryptModal(open) {
    const modal = document.getElementById('modal-decrypt');
    if (open) modal.classList.remove('hidden');
    else modal.classList.add('hidden');
}

async function deleteCredential(id) {
    const res = await apiRequest(`/credentials/${id}`, { method: 'DELETE' });
    if (res.ok) {
        showToast('Credential removed from vault', 'success');
        fetchCredentials();
    }
}

// ================= ERROR TESTING & OBSERVABILITY =================

async function triggerSystemError(code) {
    const res = await apiRequest(`/system/test-${code}`);
    showToast(`Triggered ${code} response. Inspect structured JSON envelope.`, 'info');
}

async function fetchTraceInfo() {
    const res = await apiRequest('/system/trace-info');
    if (res.ok && res.data?.success) {
        updateCorrelationId(res.data.data.correlationId);
    }
}

// ================= TOAST NOTIFICATION UTILITY =================

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

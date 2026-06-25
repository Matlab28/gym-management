const API_BASE = "http://localhost:8080";

const STORAGE = {
    email: "gym.email",
    token: "gym.token",
    role: "gym.role",
    profile: "gym.profile",
    resendUntil: "gym.resendUntil"
};

const API = {
    register: `${API_BASE}/api/v1/auth/register`,
    confirm: `${API_BASE}/api/v1/auth/confirmation`,
    resend: `${API_BASE}/api/v1/auth/confirmation/resend`,
    profile: `${API_BASE}/api/v1/users/create`,
    userProfile: (id) => `${API_BASE}/api/v1/users/${id}/profile`,
    traineeTrainings: (username) => `${API_BASE}/api/v1/trainings/trainee/${encodeURIComponent(username)}`,
    trainerTrainings: (username) => `${API_BASE}/api/v1/trainings/trainer/${encodeURIComponent(username)}`
};

const ROLES = {
    trainee: "Trainee",
    trainer: "Trainer"
};

const TRAINING_TYPES = ["Fitness", "Yoga", "Zumba", "Stretching", "Resistance"];

const state = {
    email: localStorage.getItem(STORAGE.email) || "",
    token: localStorage.getItem(STORAGE.token) || "",
    role: readStoredRole(),
    profile: readStoredProfile(),
    resendUntil: Number(localStorage.getItem(STORAGE.resendUntil) || 0),
    timerId: null
};

const app = document.querySelector("#app");

window.addEventListener("hashchange", render);
document.addEventListener("DOMContentLoaded", () => {
    if (!location.hash) {
        location.hash = state.token && state.role ? dashboardPath("home") : "#/register";
    }
    render();
});

function readStoredProfile() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE.profile) || "null");
    } catch (error) {
        return null;
    }
}

function readStoredRole() {
    const role = localStorage.getItem(STORAGE.role);
    return role ? normalizeRole(role) : "";
}

function saveState() {
    setOrRemove(STORAGE.email, state.email);
    setOrRemove(STORAGE.token, state.token);
    setOrRemove(STORAGE.role, state.role);
    setOrRemove(STORAGE.profile, state.profile ? JSON.stringify(state.profile) : "");
    setOrRemove(STORAGE.resendUntil, state.resendUntil ? String(state.resendUntil) : "");
}

function setOrRemove(key, value) {
    if (value) {
        localStorage.setItem(key, value);
        return;
    }
    localStorage.removeItem(key);
}

function routeName() {
    return location.hash.replace(/^#\/?/, "") || "register";
}

function render() {
    clearTimer();
    const route = routeName();

    if (route === "register") {
        renderOnboarding("register");
        return;
    }

    if (route === "confirm") {
        renderOnboarding("confirm");
        return;
    }

    if (route === "profile") {
        if (!state.token) {
            location.hash = "#/confirm";
            return;
        }
        renderOnboarding("profile");
        return;
    }

    if (!state.token || !state.role) {
        location.hash = state.token ? "#/profile" : "#/register";
        return;
    }

    renderDashboard(route);
}

function renderOnboarding(step) {
    const stepIndex = ["register", "confirm", "profile"].indexOf(step);
    app.innerHTML = `
        <main class="onboarding-page">
            <section class="onboarding-frame" aria-label="Account onboarding">
                <aside class="intro-panel">
                    <div>
                        <div class="brand-row">
                            <div>
                                <div class="brand-mark">GM</div>
                                <p class="brand-name">Gym Management</p>
                                <p class="brand-caption">Private training workspace</p>
                            </div>
                            <span class="step-count">Step ${stepIndex + 1} of 3</span>
                        </div>
                        <div class="intro-copy">
                            <h1>${introTitle(step)}</h1>
                            <p>${introText(step)}</p>
                        </div>
                    </div>
                    ${stepList(stepIndex)}
                </aside>
                <section class="form-panel">
                    ${step === "register" ? registerTemplate() : ""}
                    ${step === "confirm" ? confirmTemplate() : ""}
                    ${step === "profile" ? profileTemplate() : ""}
                </section>
            </section>
        </main>
    `;

    if (step === "register") {
        bindRegisterForm();
    }
    if (step === "confirm") {
        bindConfirmForm();
        startResendTimer();
    }
    if (step === "profile") {
        bindProfileForm();
        updateSpecializationOptions();
    }
}

function introTitle(step) {
    const titles = {
        register: "Start with the essentials.",
        confirm: "Confirm the inbox code.",
        profile: "Shape the profile."
    };
    return titles[step];
}

function introText(step) {
    const text = {
        register: "Create the secure account first. Email verification unlocks profile creation.",
        confirm: "The confirmation code expires in five minutes. A new code can be requested every thirty seconds.",
        profile: "Choose the role and complete the details that open the correct workspace."
    };
    return text[step];
}

function stepList(activeIndex) {
    const steps = [
        ["Account", "Email and password"],
        ["Confirmation", "Email code"],
        ["Profile", "Role and details"]
    ];

    return `
        <ol class="step-list" aria-label="Onboarding progress">
            ${steps.map(([title, meta], index) => {
                const className = index === activeIndex ? "is-active" : index < activeIndex ? "is-done" : "";
                const label = index < activeIndex ? "OK" : String(index + 1);
                return `
                    <li class="step-item ${className}">
                        <span class="step-dot">${label}</span>
                        <span>
                            <p class="step-title">${title}</p>
                            <p class="step-meta">${meta}</p>
                        </span>
                    </li>
                `;
            }).join("")}
        </ol>
    `;
}

function registerTemplate() {
    return `
        <div class="form-header">
            <div>
                <p class="eyebrow">Register</p>
                <h2>Create account</h2>
                <p class="header-note">Use an email you can check right now.</p>
            </div>
            <span class="status-pill">No profile yet</span>
        </div>
        <form id="registerForm" class="form-grid" novalidate>
            <div class="field">
                <label for="registerEmail">Email</label>
                <input id="registerEmail" name="email" type="email" autocomplete="email" value="${escapeHtml(state.email)}" required>
            </div>
            <div class="field">
                <label for="registerPassword">Password</label>
                <div class="password-control">
                    <input id="registerPassword" name="password" type="password" autocomplete="new-password" required>
                    <button class="password-toggle" type="button" data-toggle-password="registerPassword" aria-label="Show password" aria-pressed="false">
                        ${eyeIcon()}
                    </button>
                </div>
                <span class="field-hint">Needs uppercase, lowercase, and a number.</span>
            </div>
            <div class="field">
                <label for="registerPassConfirm">Confirm password</label>
                <div class="password-control">
                    <input id="registerPassConfirm" name="passConfirm" type="password" autocomplete="new-password" required>
                    <button class="password-toggle" type="button" data-toggle-password="registerPassConfirm" aria-label="Show password" aria-pressed="false">
                        ${eyeIcon()}
                    </button>
                </div>
            </div>
            <div class="actions">
                <button class="button" type="submit">Send confirmation code</button>
                ${state.email ? '<button class="button ghost" type="button" data-go-confirm>Enter code</button>' : ""}
            </div>
            <p id="registerStatus" class="status-line" role="status" aria-live="polite"></p>
        </form>
    `;
}

function confirmTemplate() {
    return `
        <div class="form-header">
            <div>
                <p class="eyebrow">Email confirmation</p>
                <h2>Enter code</h2>
                <p class="header-note">We will use the token from this step to create the profile.</p>
            </div>
            <span class="status-pill">5 min expiry</span>
        </div>
        <form id="confirmForm" class="form-grid" novalidate>
            <div class="field">
                <label for="confirmEmail">Email</label>
                <input id="confirmEmail" name="email" type="email" autocomplete="email" value="${escapeHtml(state.email)}" readonly required>
            </div>
            <div class="field">
                <label for="confirmation">Confirmation code</label>
                <input id="confirmation" class="confirmation-code" name="confirmation" inputmode="numeric" pattern="[0-9]*" maxlength="6" autocomplete="one-time-code" required>
            </div>
            <div class="actions">
                <button class="button" type="submit">Confirm email</button>
                <button id="resendButton" class="button secondary" type="button">Resend email</button>
                <button class="button ghost" type="button" data-go-register>Edit account</button>
            </div>
            <p id="confirmStatus" class="status-line" role="status" aria-live="polite"></p>
        </form>
    `;
}

function profileTemplate() {
    return `
        <div class="form-header">
            <div>
                <p class="eyebrow">Profile creation</p>
                <h2>Complete profile</h2>
                <p class="header-note">Trainer profiles need a specialization. Trainees can set it later.</p>
            </div>
            <span class="status-pill">JWT ready</span>
        </div>
        <form id="profileForm" class="form-grid" novalidate>
            <div class="role-grid" role="radiogroup" aria-label="Role">
                <label class="role-option">
                    <input type="radio" name="role" value="Trainee" checked>
                    <span class="role-card">
                        <strong>Trainee</strong>
                        <span>Book, follow, and grow.</span>
                    </span>
                </label>
                <label class="role-option">
                    <input type="radio" name="role" value="Trainer">
                    <span class="role-card">
                        <strong>Trainer</strong>
                        <span>Coach, schedule, and track.</span>
                    </span>
                </label>
            </div>
            <div class="two-columns">
                <div class="field">
                    <label for="firstName">First name</label>
                    <input id="firstName" name="firstName" autocomplete="given-name" required>
                </div>
                <div class="field">
                    <label for="lastName">Last name</label>
                    <input id="lastName" name="lastName" autocomplete="family-name" required>
                </div>
            </div>
            <div class="two-columns">
                <div class="field">
                    <label for="dateOfBirth">Date of birth</label>
                    <input id="dateOfBirth" name="dateOfBirth" type="date" required>
                </div>
                <div class="field">
                    <label for="specialization">Specialization</label>
                    <select id="specialization" name="specialization" required></select>
                </div>
            </div>
            <div class="field">
                <label for="address">Address</label>
                <input id="address" name="address" autocomplete="street-address" required>
            </div>
            <div class="actions">
                <button class="button" type="submit">Open workspace</button>
                <button class="button ghost" type="button" data-go-confirm>Back to code</button>
            </div>
            <p id="profileStatus" class="status-line" role="status" aria-live="polite"></p>
        </form>
    `;
}

function bindRegisterForm() {
    const form = document.querySelector("#registerForm");
    const status = document.querySelector("#registerStatus");
    const confirmButton = document.querySelector("[data-go-confirm]");

    bindPasswordToggles(form);

    confirmButton?.addEventListener("click", () => {
        location.hash = "#/confirm";
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = formPayload(form);
        payload.email = payload.email.trim().toLowerCase();

        if (!payload.email || !payload.password || !payload.passConfirm) {
            showStatus(status, "Fill in all account fields.", "error");
            return;
        }

        if (payload.password !== payload.passConfirm) {
            showStatus(status, "Password confirmation does not match.", "error");
            return;
        }

        if (!hasPasswordShape(payload.password)) {
            showStatus(status, "Password needs uppercase, lowercase, and a number.", "error");
            return;
        }

        setBusy(form, true);
        showStatus(status, "Sending confirmation code...", "");

        try {
            const response = await request(API.register, {
                method: "POST",
                body: payload
            });

            state.email = payload.email;
            state.resendUntil = Date.now() + 30000;
            saveState();
            showStatus(status, response.message || "Confirmation code sent.", "success");
            setTimeout(() => {
                location.hash = "#/confirm";
            }, 450);
        } catch (error) {
            showStatus(status, error.message, "error");
        } finally {
            setBusy(form, false);
        }
    });
}

function bindConfirmForm() {
    const form = document.querySelector("#confirmForm");
    const status = document.querySelector("#confirmStatus");
    const resendButton = document.querySelector("#resendButton");

    document.querySelector("[data-go-register]")?.addEventListener("click", () => {
        location.hash = "#/register";
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = formPayload(form);
        payload.email = payload.email.trim().toLowerCase();
        payload.confirmation = payload.confirmation.trim();

        if (!payload.email || !payload.confirmation) {
            showStatus(status, "Enter email and confirmation code.", "error");
            return;
        }

        if (!/^\d+$/.test(payload.confirmation)) {
            showStatus(status, "Confirmation code must contain only digits.", "error");
            return;
        }

        setBusy(form, true);
        showStatus(status, "Confirming email...", "");

        try {
            const response = await request(API.confirm, {
                method: "POST",
                body: payload
            });

            state.email = payload.email;
            state.token = response.token || "";
            state.role = "";
            state.profile = null;
            saveState();
            showStatus(status, response.message || "Email confirmed.", "success");
            setTimeout(() => {
                location.hash = "#/profile";
            }, 450);
        } catch (error) {
            showStatus(status, error.message, "error");
        } finally {
            setBusy(form, false);
        }
    });

    resendButton.addEventListener("click", async () => {
        const email = document.querySelector("#confirmEmail").value.trim().toLowerCase();
        if (!email) {
            showStatus(status, "Enter the email address first.", "error");
            return;
        }

        if (Date.now() < state.resendUntil) {
            showStatus(status, `You can resend in ${secondsUntilResend()}s.`, "error");
            return;
        }

        resendButton.disabled = true;
        showStatus(status, "Sending a fresh code...", "");

        try {
            const response = await request(API.resend, {
                method: "POST",
                body: { email }
            });
            state.email = email;
            state.resendUntil = Date.now() + 30000;
            saveState();
            showStatus(status, response.message || "New confirmation code sent.", "success");
            startResendTimer();
        } catch (error) {
            showStatus(status, error.message, "error");
            startResendTimer();
        }
    });
}

function bindProfileForm() {
    const form = document.querySelector("#profileForm");
    const status = document.querySelector("#profileStatus");

    document.querySelector("[data-go-confirm]")?.addEventListener("click", () => {
        location.hash = "#/confirm";
    });

    form.querySelectorAll("input[name='role']").forEach((input) => {
        input.addEventListener("change", updateSpecializationOptions);
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = formPayload(form);
        payload.role = selectedRole();

        if (!payload.firstName || !payload.lastName || !payload.dateOfBirth || !payload.address) {
            showStatus(status, "Fill in all profile fields.", "error");
            return;
        }

        if (payload.role === ROLES.trainer && !payload.specialization) {
            showStatus(status, "Trainer specialization is required.", "error");
            return;
        }

        if (payload.role === ROLES.trainee && !payload.specialization) {
            payload.specialization = "Set Later";
        }

        setBusy(form, true);
        showStatus(status, "Creating profile...", "");

        try {
            const response = await request(API.profile, {
                method: "POST",
                token: state.token,
                body: payload
            });

            state.profile = response;
            state.role = normalizeRole(response.role || payload.role);
            saveState();
            showStatus(status, "Profile created.", "success");
            setTimeout(() => {
                location.hash = dashboardPath("home");
            }, 450);
        } catch (error) {
            showStatus(status, error.message, "error");
        } finally {
            setBusy(form, false);
        }
    });
}

function updateSpecializationOptions() {
    const select = document.querySelector("#specialization");
    if (!select) {
        return;
    }

    const role = selectedRole();
    const options = role === ROLES.trainee ? ["Set Later", ...TRAINING_TYPES] : TRAINING_TYPES;
    select.innerHTML = options
        .map((type) => `<option value="${type}">${type}</option>`)
        .join("");
    select.required = role === ROLES.trainer;
}

function bindPasswordToggles(scope) {
    scope.querySelectorAll("[data-toggle-password]").forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.querySelector(`#${button.dataset.togglePassword}`);
            if (!input) {
                return;
            }

            const willShow = input.type === "password";
            input.type = willShow ? "text" : "password";
            button.innerHTML = willShow ? eyeOffIcon() : eyeIcon();
            button.setAttribute("aria-label", willShow ? "Hide password" : "Show password");
            button.setAttribute("aria-pressed", String(willShow));
            input.focus();
        });
    });
}

function eyeIcon() {
    return `
        <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
            <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"></path>
            <circle cx="12" cy="12" r="3"></circle>
        </svg>
    `;
}

function eyeOffIcon() {
    return `
        <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
            <path d="M3 3l18 18"></path>
            <path d="M10.6 10.6A2 2 0 0 0 12 14a2 2 0 0 0 1.4-.6"></path>
            <path d="M7.4 7.4C4.3 9 2.5 12 2.5 12s3.5 6 9.5 6c1.6 0 3-.4 4.2-1"></path>
            <path d="M12 6c6 0 9.5 6 9.5 6a16 16 0 0 1-2.4 3.1"></path>
        </svg>
    `;
}

function selectedRole() {
    return document.querySelector("input[name='role']:checked")?.value || ROLES.trainee;
}

function startResendTimer() {
    const button = document.querySelector("#resendButton");
    if (!button) {
        return;
    }

    const update = () => {
        const seconds = secondsUntilResend();
        if (seconds > 0) {
            button.disabled = true;
            button.textContent = `Resend in ${seconds}s`;
            return;
        }
        button.disabled = false;
        button.textContent = "Resend email";
        clearTimer();
    };

    update();
    state.timerId = window.setInterval(update, 1000);
}

function secondsUntilResend() {
    return Math.max(0, Math.ceil((state.resendUntil - Date.now()) / 1000));
}

function clearTimer() {
    if (state.timerId) {
        window.clearInterval(state.timerId);
        state.timerId = null;
    }
}

function renderDashboard(route) {
    const [rolePath, page = "home"] = route.split("/");
    const role = normalizeRole(rolePath);

    if (role !== state.role) {
        location.hash = dashboardPath(page);
        return;
    }

    app.innerHTML = `
        <main class="dashboard-shell">
            <aside class="sidebar">
                <div>
                    <div class="brand-mark">GM</div>
                    <p class="brand-name">Gym Management</p>
                    <p class="brand-caption">${escapeHtml(state.email)}</p>
                </div>
                <nav class="sidebar-nav" aria-label="Workspace navigation">
                    ${navLink("home", "01", "Home", page)}
                    ${navLink("profile", "02", "Profile", page)}
                    ${navLink("trainings", "03", state.role === ROLES.trainer ? "Sessions" : "Trainings", page)}
                    ${navLink("settings", "04", "Settings", page)}
                </nav>
                <div class="sidebar-footer">
                    <span class="role-badge">${escapeHtml(state.role)}</span>
                    <button class="button secondary" type="button" data-sign-out>Sign out</button>
                </div>
            </aside>
            <section class="dashboard-main">
                <div class="topbar">
                    <div class="page-heading">
                        <h1>${dashboardTitle(page)}</h1>
                        <p>${dashboardText(page)}</p>
                    </div>
                    <button class="button coral" type="button" data-refresh-profile>Refresh profile</button>
                </div>
                <div id="dashboardView">${dashboardView(page)}</div>
            </section>
        </main>
    `;

    bindDashboard(page);
}

function navLink(page, symbol, label, current) {
    return `
        <a class="nav-link ${current === page ? "is-active" : ""}" href="${dashboardPath(page)}">
            <span class="nav-symbol">${symbol}</span>
            <span>${label}</span>
        </a>
    `;
}

function dashboardPath(page) {
    return `#/${rolePath(state.role)}/${page}`;
}

function rolePath(role) {
    return normalizeRole(role) === ROLES.trainer ? "trainer" : "trainee";
}

function normalizeRole(role) {
    return String(role || "").toLowerCase() === "trainer" ? ROLES.trainer : ROLES.trainee;
}

function dashboardTitle(page) {
    const titles = {
        home: state.role === ROLES.trainer ? "Trainer home" : "Trainee home",
        profile: "Profile",
        trainings: state.role === ROLES.trainer ? "Sessions" : "Trainings",
        settings: "Settings"
    };
    return titles[page] || titles.home;
}

function dashboardText(page) {
    const profile = state.profile || {};
    const name = [profile.firstName, profile.lastName].filter(Boolean).join(" ") || "there";
    const text = {
        home: `Welcome, ${escapeHtml(name)}. Your ${state.role.toLowerCase()} workspace is ready.`,
        profile: "Your account and profile details are grouped here.",
        trainings: state.role === ROLES.trainer
            ? "Review sessions connected with your trainer profile."
            : "Review trainings connected with your trainee profile.",
        settings: "Keep local session controls close and simple."
    };
    return text[page] || text.home;
}

function dashboardView(page) {
    if (page === "profile") {
        return profileView();
    }
    if (page === "trainings") {
        return trainingsView();
    }
    if (page === "settings") {
        return settingsView();
    }
    return homeView();
}

function homeView() {
    const profile = state.profile || {};
    const focus = state.role === ROLES.trainer
        ? valueOr(profile.specialization, "No specialization")
        : valueOr(profile.specialization, "Set Later");

    return `
        <div class="dashboard-grid">
            <section class="panel">
                <div class="view-title">
                    <h2>${state.role === ROLES.trainer ? "Coaching overview" : "Training overview"}</h2>
                    <p>${state.role === ROLES.trainer ? "Your trainer identity is active." : "Your trainee identity is active."}</p>
                </div>
                <div class="metric-row">
                    <div class="metric-card">
                        <strong>Active</strong>
                        <span>Profile status</span>
                    </div>
                    <div class="metric-card">
                        <strong>${escapeHtml(focus)}</strong>
                        <span>Specialization</span>
                    </div>
                    <div class="metric-card">
                        <strong>${escapeHtml(state.role)}</strong>
                        <span>Workspace</span>
                    </div>
                </div>
            </section>
            <aside class="panel">
                <div class="view-title">
                    <h2>Next</h2>
                    <p>${state.role === ROLES.trainer ? "Open sessions to load trainer trainings." : "Open trainings to load trainee trainings."}</p>
                </div>
                <div class="actions">
                    <a class="button" href="${dashboardPath("profile")}">View profile</a>
                    <a class="button secondary" href="${dashboardPath("trainings")}">View ${state.role === ROLES.trainer ? "sessions" : "trainings"}</a>
                </div>
            </aside>
        </div>
    `;
}

function profileView() {
    const profile = state.profile || {};
    return `
        <section class="panel">
            <div class="view-title">
                <h2>${escapeHtml([profile.firstName, profile.lastName].filter(Boolean).join(" ") || "Profile")}</h2>
                <p>${escapeHtml(valueOr(profile.username, state.email))}</p>
            </div>
            <dl class="detail-list">
                ${detail("Role", state.role)}
                ${detail("Email", state.email)}
                ${detail("Username", profile.username)}
                ${detail("First name", profile.firstName)}
                ${detail("Last name", profile.lastName)}
                ${detail("Date of birth", profile.dateOfBirth)}
                ${detail("Address", profile.address)}
                ${detail("Specialization", state.role === ROLES.trainee ? valueOr(profile.specialization, "Set Later") : profile.specialization)}
                ${detail("Status", valueOr(profile.profileStatus, "Active"))}
            </dl>
        </section>
    `;
}

function trainingsView() {
    const profile = state.profile || {};
    return `
        <section class="panel">
            <div class="view-title">
                <h2>${state.role === ROLES.trainer ? "Session list" : "Training list"}</h2>
                <p>Use the profile username to load saved activity from the API.</p>
            </div>
            <div class="toolbar">
                <div class="field">
                    <label for="periodFrom">From</label>
                    <input id="periodFrom" type="date">
                </div>
                <div class="field">
                    <label for="periodTo">To</label>
                    <input id="periodTo" type="date">
                </div>
                <button class="button" type="button" data-load-trainings ${profile.username ? "" : "disabled"}>Load</button>
            </div>
            <div id="trainingResult" class="empty-state">
                ${profile.username ? "No trainings loaded yet." : "Create or refresh the profile before loading trainings."}
            </div>
        </section>
    `;
}

function settingsView() {
    return `
        <section class="panel">
            <div class="view-title">
                <h2>Session controls</h2>
                <p>The browser stores only the current onboarding email, token, role, and profile snapshot.</p>
            </div>
            <div class="actions">
                <button class="button secondary" type="button" data-refresh-profile>Refresh profile</button>
                <button class="button coral" type="button" data-sign-out>Sign out</button>
                <button class="button ghost" type="button" data-reset-flow>Start again</button>
            </div>
        </section>
    `;
}

function detail(label, value) {
    return `
        <div class="detail-item">
            <dt class="detail-label">${label}</dt>
            <dd class="detail-value">${escapeHtml(valueOr(value, "Not set"))}</dd>
        </div>
    `;
}

function bindDashboard(page) {
    document.querySelectorAll("[data-sign-out]").forEach((button) => {
        button.addEventListener("click", signOut);
    });

    document.querySelectorAll("[data-reset-flow]").forEach((button) => {
        button.addEventListener("click", () => {
            signOut();
            location.hash = "#/register";
        });
    });

    document.querySelectorAll("[data-refresh-profile]").forEach((button) => {
        button.addEventListener("click", refreshProfile);
    });

    if (page === "trainings") {
        document.querySelector("[data-load-trainings]")?.addEventListener("click", loadTrainings);
    }
}

async function refreshProfile() {
    if (!state.profile?.id) {
        return;
    }

    try {
        const profile = await request(API.userProfile(state.profile.id), {
            method: "GET",
            token: state.token
        });
        state.profile = { ...state.profile, ...profile };
        saveState();
        render();
    } catch (error) {
        showInlineError(error.message);
    }
}

async function loadTrainings() {
    const profile = state.profile || {};
    const result = document.querySelector("#trainingResult");
    if (!profile.username) {
        result.textContent = "Profile username is missing.";
        return;
    }

    const params = new URLSearchParams();
    const from = document.querySelector("#periodFrom").value;
    const to = document.querySelector("#periodTo").value;
    if (from) {
        params.set("periodFrom", from);
    }
    if (to) {
        params.set("periodTo", to);
    }

    const baseUrl = state.role === ROLES.trainer
        ? API.trainerTrainings(profile.username)
        : API.traineeTrainings(profile.username);
    const url = params.toString() ? `${baseUrl}?${params}` : baseUrl;

    result.textContent = "Loading...";

    try {
        const trainings = await request(url, {
            method: "GET",
            token: state.token
        });
        result.outerHTML = renderTrainingResult(Array.isArray(trainings) ? trainings : []);
    } catch (error) {
        result.textContent = error.message;
    }
}

function renderTrainingResult(trainings) {
    if (!trainings.length) {
        return '<div id="trainingResult" class="empty-state">No trainings found for this profile.</div>';
    }

    return `
        <div id="trainingResult" class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Duration</th>
                        <th>${state.role === ROLES.trainer ? "Trainee" : "Trainer"}</th>
                    </tr>
                </thead>
                <tbody>
                    ${trainings.map((training) => `
                        <tr>
                            <td>${escapeHtml(valueOr(training.trainingName, training.name))}</td>
                            <td>${escapeHtml(valueOr(training.trainingDate, training.date))}</td>
                            <td>${escapeHtml(valueOr(training.trainingType, training.type))}</td>
                            <td>${escapeHtml(valueOr(training.duration, training.trainingDuration))}</td>
                            <td>${escapeHtml(state.role === ROLES.trainer
                                ? valueOr(training.traineeName, training.traineeUsername)
                                : valueOr(training.trainerName, training.trainerUsername))}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function signOut() {
    state.email = "";
    state.token = "";
    state.role = "";
    state.profile = null;
    state.resendUntil = 0;
    saveState();
    location.hash = "#/register";
}

function showInlineError(message) {
    const view = document.querySelector("#dashboardView");
    if (!view) {
        return;
    }
    const error = document.createElement("p");
    error.className = "status-line is-error";
    error.textContent = message;
    view.prepend(error);
}

function formPayload(form) {
    return Object.fromEntries(new FormData(form).entries());
}

async function request(url, options = {}) {
    const headers = {
        Accept: "application/json"
    };

    if (options.body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    if (options.token) {
        headers.Authorization = `Bearer ${options.token}`;
    }

    const response = await fetch(url, {
        method: options.method || "GET",
        headers,
        body: options.body !== undefined ? JSON.stringify(options.body) : undefined
    });

    const text = await response.text();
    const data = text ? safeJson(text) : null;

    if (!response.ok) {
        throw new Error(errorMessage(data, response.status));
    }

    return data || {};
}

function safeJson(text) {
    try {
        return JSON.parse(text);
    } catch (error) {
        return { message: text };
    }
}

function errorMessage(data, status) {
    if (data?.message) {
        return data.message;
    }
    if (data?.error) {
        return data.error;
    }
    return `Request failed with status ${status}.`;
}

function showStatus(element, message, type) {
    element.textContent = message;
    element.classList.toggle("is-error", type === "error");
    element.classList.toggle("is-success", type === "success");
}

function setBusy(form, busy) {
    form.querySelectorAll("button, input, select").forEach((element) => {
        element.disabled = busy;
    });
}

function hasPasswordShape(password) {
    return /[A-Z]/.test(password) && /[a-z]/.test(password) && /\d/.test(password);
}

function valueOr(value, fallback) {
    if (value === null || value === undefined || value === "") {
        return fallback;
    }
    return value;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

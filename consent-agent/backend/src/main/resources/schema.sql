-- Consent Intelligence Agent - schema
-- Assumes your existing Accounts / AccountsById / Balance tables already exist.
-- This adds Consent (if not already present) and a new consent_health_flags table
-- that the agent writes to and reasons over.

CREATE TABLE IF NOT EXISTS consent (
    consent_id       VARCHAR(64) PRIMARY KEY,
    account_id       VARCHAR(64) NOT NULL,
    tpp_name         VARCHAR(120) NOT NULL,      -- third party provider e.g. "FinTechX"
    scope            VARCHAR(200) NOT NULL,      -- e.g. "balance-read,transaction-read"
    granted_at       TIMESTAMP NOT NULL,
    expires_at       TIMESTAMP,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, REVOKED, EXPIRED
    last_used_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS consent_access_log (
    id               BIGSERIAL PRIMARY KEY,
    consent_id       VARCHAR(64) NOT NULL REFERENCES consent(consent_id),
    accessed_at      TIMESTAMP NOT NULL,
    endpoint_called  VARCHAR(120) NOT NULL       -- e.g. "GET /balance"
);

CREATE TABLE IF NOT EXISTS consent_health_flag (
    id               BIGSERIAL PRIMARY KEY,
    consent_id       VARCHAR(64) NOT NULL REFERENCES consent(consent_id),
    flag_type        VARCHAR(40) NOT NULL,       -- STALE, SCOPE_MISMATCH, EXPIRING_SOON, ACCESS_SPIKE
    severity         VARCHAR(10) NOT NULL,       -- LOW, MEDIUM, HIGH
    detail           TEXT,                       -- raw facts, before LLM explanation
    explanation      TEXT,                       -- filled in by Ollama, plain-English
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    resolved         BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS account_balance (
    account_id       VARCHAR(64) PRIMARY KEY,
    available_amount NUMERIC(14,2) NOT NULL,
    currency         VARCHAR(8) NOT NULL DEFAULT 'INR'
);

CREATE TABLE IF NOT EXISTS payment_intent (
    id               BIGSERIAL PRIMARY KEY,
    raw_command      TEXT NOT NULL,
    account_id       VARCHAR(64) NOT NULL,
    payee_name       VARCHAR(120),
    amount           NUMERIC(14,2),
    currency         VARCHAR(8) DEFAULT 'INR',
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    -- PENDING_CONFIRMATION, DECLINED, EXECUTED, FAILED_VALIDATION
    validation_note  TEXT,
    transaction_ref  VARCHAR(64),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    confirmed_at     TIMESTAMP,
    executed_at      TIMESTAMP
);

-- ===== Seed data =====

INSERT INTO consent (consent_id, account_id, tpp_name, scope, granted_at, expires_at, status, last_used_at) VALUES
('CNS-1001', 'ACC-001', 'Zenith Budgeting App', 'balance-read', now() - interval '10 days', now() + interval '80 days', 'ACTIVE', now() - interval '1 days'),
('CNS-1002', 'ACC-001', 'QuickPay Wallet', 'balance-read,transaction-read,account-read', now() - interval '150 days', now() + interval '30 days', 'ACTIVE', now() - interval '2 days'),
-- PLANTED ANOMALY: wide scope, unused for 96 days, near expiry -> should trigger multiple flags
('CNS-1003', 'ACC-002', 'FinTechX Rewards', 'balance-read,transaction-read,account-read,identity-read', now() - interval '140 days', now() + interval '10 days', 'ACTIVE', now() - interval '96 days'),
('CNS-1004', 'ACC-002', 'SmartSave Analyzer', 'balance-read', now() - interval '5 days', now() + interval '175 days', 'ACTIVE', now() - interval '1 days')
ON CONFLICT (consent_id) DO NOTHING;

INSERT INTO account_balance (account_id, available_amount, currency) VALUES
('ACC-001', 12500.00, 'INR'),
('ACC-002', 800.00, 'INR')   -- deliberately low, so an over-limit payment demo also works
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO consent_access_log (consent_id, accessed_at, endpoint_called) VALUES
('CNS-1001', now() - interval '1 days', 'GET /balance'),
('CNS-1002', now() - interval '2 days', 'GET /balance'),
('CNS-1002', now() - interval '9 days', 'GET /transactions'),
('CNS-1003', now() - interval '96 days', 'GET /balance'),  -- last known access, matches the anomaly
('CNS-1004', now() - interval '1 days', 'GET /balance');

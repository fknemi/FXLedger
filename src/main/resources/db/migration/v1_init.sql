CREATE TABLE accounts (
    id         UUID        PRIMARY KEY,
    name       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE transactions (
    id                          UUID        PRIMARY KEY,
    type                        TEXT        NOT NULL,
    status                      TEXT        NOT NULL,
    reversal_of_transaction_id  UUID,
    created_at                  TIMESTAMPTZ NOT NULL
);

CREATE TABLE ledger_entries (
    id             UUID           PRIMARY KEY,
    account_id     UUID           NOT NULL REFERENCES accounts(id),
    transaction_id UUID           NOT NULL REFERENCES transactions(id),
    currency       CHAR(3)        NOT NULL,
    amount         NUMERIC(38,10) NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_ledger_account_currency ON ledger_entries(account_id, currency);
CREATE INDEX idx_ledger_transaction       ON ledger_entries(transaction_id);

CREATE TABLE exchange_rates (
    id            UUID           PRIMARY KEY,
    from_currency CHAR(3)        NOT NULL,
    to_currency   CHAR(3)        NOT NULL,
    rate          NUMERIC(38,10) NOT NULL,
    effective_at  TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_exchange_rates_lookup ON exchange_rates(from_currency, to_currency, effective_at DESC);

CREATE TABLE idempotency_records (
    idempotency_key TEXT        PRIMARY KEY,
    response_body   TEXT        NOT NULL,
    http_status     INT         NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

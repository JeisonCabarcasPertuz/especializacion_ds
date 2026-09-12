CREATE TABLE customers (
    id              UUID PRIMARY KEY,
    document_type   VARCHAR(20) NOT NULL,
    document_number VARCHAR(50) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    CONSTRAINT uk_customers_document_number UNIQUE (document_number)
);

CREATE TABLE bank_accounts (
    id               UUID PRIMARY KEY,
    account_number   VARCHAR(50) NOT NULL,
    customer_id      UUID NOT NULL,
    account_type     VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    balance          NUMERIC(19,2) NOT NULL,
    currency         VARCHAR(3) NOT NULL,
    version          BIGINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    CONSTRAINT uk_bank_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_bank_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT ck_bank_accounts_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE bank_transactions (
    id                     UUID PRIMARY KEY,
    type                   VARCHAR(20) NOT NULL,
    amount                 NUMERIC(19,2) NOT NULL,
    fee                    NUMERIC(19,2) NOT NULL,
    status                 VARCHAR(20) NOT NULL,
    source_account_id      UUID,
    destination_account_id UUID,
    created_at             TIMESTAMP NOT NULL,
    CONSTRAINT fk_bank_transactions_source FOREIGN KEY (source_account_id) REFERENCES bank_accounts (id),
    CONSTRAINT fk_bank_transactions_destination FOREIGN KEY (destination_account_id) REFERENCES bank_accounts (id),
    CONSTRAINT ck_bank_transactions_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_bank_transactions_fee_non_negative CHECK (fee >= 0)
);

CREATE TABLE audit_records (
    id             UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    description    VARCHAR(500) NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_audit_records_transaction FOREIGN KEY (transaction_id) REFERENCES bank_transactions (id)
);

CREATE INDEX idx_bank_accounts_customer_id ON bank_accounts (customer_id);
CREATE INDEX idx_bank_transactions_source_account_id ON bank_transactions (source_account_id);
CREATE INDEX idx_bank_transactions_destination_account_id ON bank_transactions (destination_account_id);
CREATE INDEX idx_bank_transactions_created_at ON bank_transactions (created_at);

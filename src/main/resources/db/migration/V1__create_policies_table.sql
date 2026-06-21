CREATE TABLE policies (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    device_brand VARCHAR NOT NULL,
    device_model VARCHAR NOT NULL,
    device_imei VARCHAR(15) NOT NULL,
    device_invoice_value NUMERIC(10, 2) NOT NULL,
    monthly_premium NUMERIC(10, 2) NOT NULL,
    due_day INTEGER NOT NULL,
    status VARCHAR NOT NULL,
    coverage VARCHAR NOT NULL,
    CONSTRAINT chk_policies_due_day CHECK (due_day >= 1 AND due_day <= 28)
);

CREATE TABLE transactions
(
    id       IDENTITY,
    type     VARCHAR(255),
    amount   DOUBLE,
    category VARCHAR(255),
    date     TIMESTAMP
);
    -- ===================== EMPLOYEE =====================
CREATE TABLE employee (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    position          VARCHAR(50),
    gender            VARCHAR(10),
    date_of_birth     DATE,
    place_of_birth    VARCHAR(255),
    address           VARCHAR(255),
    identity_card_no  VARCHAR(30)  NOT NULL UNIQUE,
    phone_number      VARCHAR(20),
    salary            NUMERIC(12, 2),
    rec_status        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    create_date       TIMESTAMP,
    update_date       TIMESTAMP
);

-- ===================== CUSTOMER =====================
CREATE TABLE customer (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    gender            VARCHAR(10),
    date_of_birth     DATE,
    address           VARCHAR(255),
    customer_type     VARCHAR(10)  NOT NULL,      -- STUDENT | TEACHER
    identity_card_no  VARCHAR(30)  NOT NULL UNIQUE,
    phone_number      VARCHAR(20),
    rec_status        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    create_date       TIMESTAMP,
    update_date       TIMESTAMP
);

-- ===================== BOOK =====================
CREATE TABLE book (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(255) NOT NULL,
    publish_year  INTEGER,
    amount        INTEGER      NOT NULL CHECK (amount >= 0 AND amount <= 20),
    unit_price    NUMERIC(10, 2),
    book_status   VARCHAR(15)  NOT NULL DEFAULT 'AVAILABLE',  -- AVAILABLE | NOT_AVAILABLE
    rec_status    VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    create_date   TIMESTAMP,
    update_date   TIMESTAMP,
    CONSTRAINT uk_book_title_author_year UNIQUE (title, author, publish_year)
);

-- ===================== BORROW =====================
CREATE TABLE borrow (
    id             BIGSERIAL PRIMARY KEY,
    book_id        BIGINT      NOT NULL REFERENCES book (id),
    employee_id    BIGINT      NOT NULL REFERENCES employee (id),
    customer_id    BIGINT      NOT NULL REFERENCES customer (id),
    borrow_date    DATE        NOT NULL,
    return_date    DATE        NOT NULL,
    amount         INTEGER     NOT NULL,
    owe            INTEGER     NOT NULL,
    borrow_status  VARCHAR(10) NOT NULL DEFAULT 'BORROW',  -- BORROW | OWE | RETURN | OVER
    rec_status     VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    create_date    TIMESTAMP,
    update_date    TIMESTAMP
);
CREATE INDEX idx_borrow_customer ON borrow (customer_id);
CREATE INDEX idx_borrow_status_return_date ON borrow (borrow_status, return_date);

-- ===================== BOOK RETURN =====================
CREATE TABLE book_return (
    id              BIGSERIAL PRIMARY KEY,
    borrow_id       BIGINT      NOT NULL REFERENCES borrow (id),
    employee_id     BIGINT      NOT NULL REFERENCES employee (id),
    return_date     DATE        NOT NULL,
    amount          INTEGER     NOT NULL,
    penalty_amount  NUMERIC(10, 2) DEFAULT 0,
    rec_status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    create_date     TIMESTAMP,
    update_date     TIMESTAMP
);
CREATE INDEX idx_return_borrow ON book_return (borrow_id);

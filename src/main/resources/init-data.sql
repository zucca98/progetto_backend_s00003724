-- Script di inizializzazione database cooperativa_immobiliare
-- PostgreSQL 14+

-- Crea database (esegui separatamente se necessario)
-- CREATE DATABASE cooperativa_immobiliare;

-- Tabella roles
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Inserisci i 3 ruoli richiesti
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_MANAGER'), ('ROLE_LOCATARIO')
ON CONFLICT (name) DO NOTHING;

-- Tabella users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabella user_roles (join many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Tabella locatario
CREATE TABLE IF NOT EXISTS locatario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    cf VARCHAR(16) NOT NULL UNIQUE,
    indirizzo VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

-- Tabella immobile (super-classe)
CREATE TABLE IF NOT EXISTS immobile (
    id SERIAL PRIMARY KEY,
    indirizzo VARCHAR(255) NOT NULL,
    citta VARCHAR(100) NOT NULL,
    superficie DECIMAL(10,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('APPARTAMENTO', 'NEGOZIO', 'UFFICIO'))
);

-- Tabella appartamento (sottoclasse)
CREATE TABLE IF NOT EXISTS appartamento (
    immobile_id BIGINT PRIMARY KEY REFERENCES immobile(id) ON DELETE CASCADE,
    piano INTEGER NOT NULL,
    num_camere INTEGER NOT NULL
);

-- Tabella negozio (sottoclasse)
CREATE TABLE IF NOT EXISTS negozio (
    immobile_id BIGINT PRIMARY KEY REFERENCES immobile(id) ON DELETE CASCADE,
    vetrine INTEGER NOT NULL,
    magazzino_mq DECIMAL(10,2) NOT NULL
);

-- Tabella ufficio (sottoclasse)
CREATE TABLE IF NOT EXISTS ufficio (
    immobile_id BIGINT PRIMARY KEY REFERENCES immobile(id) ON DELETE CASCADE,
    posti_lavoro INTEGER NOT NULL,
    sale_riunioni INTEGER NOT NULL
);

-- Tabella contratto
CREATE TABLE IF NOT EXISTS contratto (
    id SERIAL PRIMARY KEY,
    locatario_id BIGINT NOT NULL REFERENCES locatario(id) ON DELETE CASCADE,
    immobile_id BIGINT NOT NULL REFERENCES immobile(id) ON DELETE CASCADE,
    data_inizio DATE NOT NULL,
    durata_anni INTEGER NOT NULL,
    canone_annuo DECIMAL(10,2) NOT NULL,
    frequenza_rata VARCHAR(20) NOT NULL DEFAULT 'TRIMESTRALE' 
        CHECK (frequenza_rata IN ('MENSILE', 'BIMESTRALE', 'TRIMESTRALE', 'SEMESTRALE', 'ANNUALE'))
);

-- Tabella rata
CREATE TABLE IF NOT EXISTS rata (
    id SERIAL PRIMARY KEY,
    contratto_id BIGINT NOT NULL REFERENCES contratto(id) ON DELETE CASCADE,
    numero_rata INTEGER NOT NULL,
    data_scadenza DATE NOT NULL,
    importo DECIMAL(10,2) NOT NULL,
    pagata CHAR(1) NOT NULL DEFAULT 'N' CHECK (pagata IN ('S', 'N'))
);

-- Tabella manutenzione
CREATE TABLE IF NOT EXISTS manutenzione (
    id SERIAL PRIMARY KEY,
    immobile_id BIGINT NOT NULL REFERENCES immobile(id) ON DELETE CASCADE,
    locatario_id BIGINT NOT NULL REFERENCES locatario(id) ON DELETE CASCADE,
    data_man DATE NOT NULL,
    importo DECIMAL(10,2) NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'STRAORDINARIA',
    descrizione TEXT
);

-- Indici per migliorare le performance delle query
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_locatario_cf ON locatario(cf);
CREATE INDEX IF NOT EXISTS idx_locatario_user_id ON locatario(user_id);
CREATE INDEX IF NOT EXISTS idx_immobile_citta ON immobile(citta);
CREATE INDEX IF NOT EXISTS idx_immobile_tipo ON immobile(tipo);
CREATE INDEX IF NOT EXISTS idx_contratto_locatario ON contratto(locatario_id);
CREATE INDEX IF NOT EXISTS idx_contratto_immobile ON contratto(immobile_id);
CREATE INDEX IF NOT EXISTS idx_rata_contratto ON rata(contratto_id);
CREATE INDEX IF NOT EXISTS idx_rata_pagata ON rata(pagata);
CREATE INDEX IF NOT EXISTS idx_manutenzione_locatario ON manutenzione(locatario_id);
CREATE INDEX IF NOT EXISTS idx_manutenzione_immobile ON manutenzione(immobile_id);
CREATE INDEX IF NOT EXISTS idx_manutenzione_data ON manutenzione(data_man);

-- Dati di esempio per testing

-- Admin user (password: admin123)
INSERT INTO users (email, password, nome, cognome, enabled) 
VALUES ('admin@cooperativa.it', '$2a$10$N.zmdr9A/QhOXWdEKMkf9OgH4VvILqU3S4bGWXU3.1RQEKPxgTuji', 'Admin', 'Sistema', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'admin@cooperativa.it' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Manager user (password: manager123)
INSERT INTO users (email, password, nome, cognome, enabled)
VALUES ('manager@cooperativa.it', '$2a$10$N.zmdr9A/QhOXWdEKMkf9OgH4VvILqU3S4bGWXU3.1RQEKPxgTuji', 'Mario', 'Rossi', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'manager@cooperativa.it' AND r.name = 'ROLE_MANAGER'
ON CONFLICT DO NOTHING;

-- Locatario user (password: locatario123)
INSERT INTO users (email, password, nome, cognome, enabled)
VALUES ('locatario@example.it', '$2a$10$N.zmdr9A/QhOXWdEKMkf9OgH4VvILqU3S4bGWXU3.1RQEKPxgTuji', 'Giuseppe', 'Verdi', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'locatario@example.it' AND r.name = 'ROLE_LOCATARIO'
ON CONFLICT DO NOTHING;

-- Locatario associato
INSERT INTO locatario (nome, cognome, cf, indirizzo, telefono, user_id)
SELECT 'Giuseppe', 'Verdi', 'VRDGPP80A01H501Z', 'Via Roma 10, Milano', '+39 333 1234567', id
FROM users WHERE email = 'locatario@example.it'
ON CONFLICT (cf) DO NOTHING;

-- Immobili di esempio
INSERT INTO immobile (indirizzo, citta, superficie, tipo) VALUES
    ('Via Garibaldi 15', 'Milano', 85.5, 'APPARTAMENTO'),
    ('Corso Buenos Aires 200', 'Milano', 120.0, 'NEGOZIO'),
    ('Via Dante 50', 'Bergamo', 150.0, 'UFFICIO'),
    ('Piazza Duomo 1', 'Milano', 95.0, 'APPARTAMENTO')
ON CONFLICT DO NOTHING;

-- Sottoclassi immobili
INSERT INTO appartamento (immobile_id, piano, num_camere)
SELECT id, 3, 3 FROM immobile WHERE indirizzo = 'Via Garibaldi 15' AND tipo = 'APPARTAMENTO'
ON CONFLICT DO NOTHING;

INSERT INTO appartamento (immobile_id, piano, num_camere)
SELECT id, 2, 4 FROM immobile WHERE indirizzo = 'Piazza Duomo 1' AND tipo = 'APPARTAMENTO'
ON CONFLICT DO NOTHING;

INSERT INTO negozio (immobile_id, vetrine, magazzino_mq)
SELECT id, 2, 30.0 FROM immobile WHERE indirizzo = 'Corso Buenos Aires 200' AND tipo = 'NEGOZIO'
ON CONFLICT DO NOTHING;

INSERT INTO ufficio (immobile_id, posti_lavoro, sale_riunioni)
SELECT id, 10, 2 FROM immobile WHERE indirizzo = 'Via Dante 50' AND tipo = 'UFFICIO'
ON CONFLICT DO NOTHING;

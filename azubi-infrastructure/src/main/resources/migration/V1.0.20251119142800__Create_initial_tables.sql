-- V1__Initial_Schema.sql
CREATE SCHEMA IF NOT EXISTS relin_uni2;

-- Produktgruppen Tabelle
CREATE TABLE relin_uni2.ProductGroup (
    productGroupId INT PRIMARY KEY,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    productGroupName VARCHAR(50) NOT NULL
);

-- Produkte Tabelle
CREATE TABLE relin_uni2.SalesProduct (
    productId INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    productGroupId INT,
    price DECIMAL(10,2),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (productGroupId) REFERENCES relin_uni2.ProductGroup(productGroupId)
);

-- Allgemeiner Bestand (für Kompatibilität)
CREATE TABLE relin_uni2.Stock (
    productId INTEGER PRIMARY KEY,
    stock INTEGER,
    lastChange TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (productId) REFERENCES relin_uni2.SalesProduct(productId)
);

-- Märkte Tabelle
CREATE TABLE relin_uni2.Market (
    marketId INT PRIMARY KEY,
    marketName VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    postalCode VARCHAR(10) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Markt-spezifische Bestände
CREATE TABLE relin_uni2.MarketStock (
    marketStockId SERIAL PRIMARY KEY,
    marketId INT NOT NULL,
    productId INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    lastChange TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (marketId) REFERENCES relin_uni2.Market(marketId),
    FOREIGN KEY (productId) REFERENCES relin_uni2.SalesProduct(productId),
    UNIQUE(marketId, productId)
);

-- Trigger-Funktion für Timestamp-Updates
CREATE OR REPLACE FUNCTION relin_uni2.updateTimeStamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- Trigger für automatische Timestamp-Updates
CREATE TRIGGER updateProductGroupTrigger
BEFORE UPDATE ON relin_uni2.ProductGroup
FOR EACH ROW EXECUTE FUNCTION relin_uni2.updateTimeStamp();

CREATE TRIGGER updateSalesProductTrigger
BEFORE UPDATE ON relin_uni2.SalesProduct
FOR EACH ROW EXECUTE FUNCTION relin_uni2.updateTimeStamp();

CREATE TRIGGER updateMarketTrigger
BEFORE UPDATE ON relin_uni2.Market
FOR EACH ROW EXECUTE FUNCTION relin_uni2.updateTimeStamp();

CREATE TRIGGER updateMarketStockTrigger
BEFORE UPDATE ON relin_uni2.MarketStock
FOR EACH ROW EXECUTE FUNCTION relin_uni2.updateTimeStamp();

-- Grunddaten einfügen
-- Produktgruppen
INSERT INTO relin_uni2.ProductGroup (productGroupId, productGroupName) VALUES
(1, 'SMARTPHONE'),
(2, 'GAMING'),
(3, 'PCS'),
(4, 'WASHINGMACHINES');

-- Produkte
INSERT INTO relin_uni2.SalesProduct (productId, name, productGroupId, price, created, updated) VALUES
(1002, 'Samsung Galaxy Z Fold7', 1, 1799.99, NOW(), NOW()),
(1003, 'Sony WH-1000XM4', 2, 849.99, NOW(), NOW()),
(1004, 'MacBook Pro 14"', 3, 2399.99, NOW(), NOW()),
(106, 'Xiaomi Mi 13', 1, 599.99, NOW(), NOW()),
(107, 'OnePlus 11', 1, 699.99, NOW(), NOW()),
(108, 'Xbox Series X', 2, 499.99, NOW(), NOW()),
(109, 'Nintendo Switch OLED', 2, 349.99, NOW(), NOW()),
(110, 'Dell XPS 13', 3, 1299.99, NOW(), NOW()),
(111, 'HP Pavilion Desktop', 3, 799.99, NOW(), NOW()),
(112, 'Miele Washing Machine', 4, 899.99, NOW(), NOW()),
(113, 'LG Front Load Washer', 4, 749.99, NOW(), NOW()),
(114, 'Gaming Mouse Logitech', 2, 79.99, NOW(), NOW()),
(115, 'MacBook Pro M3', 3, 1999.99, NOW(), NOW()),
(848, 'Nokia 6210', 1, 50.99, NOW(), NOW()),
(849, 'Iphone 15 Pro', 1, 599.99, NOW(), NOW()),
(850, 'Playstation 3', 2, 299.99, NOW(), NOW()),
(851, 'BOSCH Waschmaschine 6789', 4, 1349.99, NOW(), NOW());

-- Allgemeiner Bestand (für Kompatibilität mit bestehendem Code)
INSERT INTO relin_uni2.Stock (productId, stock, lastChange) VALUES
(1002, 18, NOW()),
(1003, 12, NOW()),
(1004, 8, NOW()),
(106, 35, NOW()),
(107, 22, NOW()),
(108, 15, NOW()),
(109, 28, NOW()),
(110, 10, NOW()),
(111, 14, NOW()),
(112, 6, NOW()),
(113, 9, NOW()),
(114, 45, NOW()),
(115, 5, NOW()),
(848, 40, NOW()),
(849, 98, NOW()),
(850, 20, NOW()),
(851, 4, NOW());

-- Märkte
INSERT INTO relin_uni2.Market (marketId, marketName, location, postalCode) VALUES
(1, 'MediaMarkt München Zentrum', 'Marienplatz 12, München', '80331'),
(2, 'MediaMarkt Hamburg Nord', 'Mönckebergstraße 45, Hamburg', '20095'),
(3, 'MediaMarkt Berlin Alexanderplatz', 'Alexanderplatz 9, Berlin', '10178'),
(999, 'Online Shop', 'Online Lager', '00000');

-- Markt-spezifische Bestände
-- München (Markt 1) - Kleinere Bestände
INSERT INTO relin_uni2.MarketStock (marketId, productId, stock) VALUES
(1, 1002, 5), (1, 1003, 3), (1, 1004, 2), (1, 106, 8), (1, 107, 4),
(1, 108, 6), (1, 109, 10), (1, 110, 3), (1, 111, 2), (1, 112, 1),
(1, 113, 2), (1, 114, 15), (1, 115, 1), (1, 848, 12), (1, 849, 25),
(1, 850, 3), (1, 851, 1);

-- Hamburg (Markt 2) - Mittlere Bestände
INSERT INTO relin_uni2.MarketStock (marketId, productId, stock) VALUES
(2, 1002, 7), (2, 1003, 5), (2, 1004, 4), (2, 106, 12), (2, 107, 8),
(2, 108, 3), (2, 109, 15), (2, 110, 5), (2, 111, 6), (2, 112, 2),
(2, 113, 4), (2, 114, 20), (2, 115, 2), (2, 848, 18), (2, 849, 30),
(2, 850, 5), (2, 851, 2);

-- Berlin (Markt 3) - Große Auswahl
INSERT INTO relin_uni2.MarketStock (marketId, productId, stock) VALUES
(3, 1002, 10), (3, 1003, 8), (3, 1004, 6), (3, 106, 15), (3, 107, 12),
(3, 108, 8), (3, 109, 20), (3, 110, 7), (3, 111, 10), (3, 112, 3),
(3, 113, 5), (3, 114, 25), (3, 115, 4), (3, 848, 25), (3, 849, 45),
(3, 850, 8), (3, 851, 3);

-- Online Shop (Markt 999) - Größte Bestände
INSERT INTO relin_uni2.MarketStock (marketId, productId, stock) VALUES
(999, 1002, 50), (999, 1003, 40), (999, 1004, 25), (999, 106, 100),
(999, 107, 80), (999, 108, 45), (999, 109, 90), (999, 110, 35),
(999, 111, 60), (999, 112, 20), (999, 113, 25), (999, 114, 150),
(999, 115, 15), (999, 848, 200), (999, 849, 300), (999, 850, 75), (999, 851, 10);


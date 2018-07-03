CREATE KEYSPACE IF NOT EXISTS teststress
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': '3'
};

CREATE TABLE IF NOT EXISTS testTable (
    id UUID,
    name text,
    prename text,
    createdAt timestamp,
    salary decimal,
    lebenslauf blob,
    PRIMARY KEY (name,id)
);


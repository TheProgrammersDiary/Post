CREATE TABLE IF NOT EXISTS post (
    id VARCHAR(255) NOT NULL,
    version SERIAL NOT NULL,
    date_posted TIMESTAMP NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    PRIMARY KEY (id, version)
);
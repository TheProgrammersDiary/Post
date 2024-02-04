CREATE TABLE IF NOT EXISTS post (
    post_id VARCHAR(255) NOT NULL,
    version INT NOT NULL,
    date_posted TIMESTAMP NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    PRIMARY KEY (post_id, version)
);
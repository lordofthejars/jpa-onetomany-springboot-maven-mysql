
CREATE TABLE book_category ( id SERIAL PRIMARY KEY,name TEXT NOT NULL);

CREATE TABLE book (id SERIAL PRIMARY KEY,name TEXT DEFAULT NULL,book_category_id int references book_category(id));
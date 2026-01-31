ALTER TABLE movie
ADD CONSTRAINT uk_movie_title_year UNIQUE (title);

CREATE TABLE IF NOT EXISTS process (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	"name" varchar NOT NULL,
	payload TEXT NOT NULL,
	status varchar NOT NULL,
	created_at timestamp NOT NULL,
	updated_at timestamp NOT NULL
);



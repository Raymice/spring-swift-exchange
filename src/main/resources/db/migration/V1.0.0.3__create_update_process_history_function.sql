CREATE OR REPLACE FUNCTION update_process_history()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO process_history(process_id, status, created_at)
    VALUES (NEW.id, NEW.status, NOW());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
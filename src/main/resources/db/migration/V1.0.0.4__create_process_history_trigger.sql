CREATE TRIGGER process_history_trigger
AFTER INSERT OR UPDATE ON process
FOR EACH ROW
EXECUTE FUNCTION update_process_history();
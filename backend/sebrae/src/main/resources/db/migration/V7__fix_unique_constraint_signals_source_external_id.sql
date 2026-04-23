-- Remove constraint e índice antigos que cobriam apenas external_id
ALTER TABLE signals DROP CONSTRAINT IF EXISTS uk_signals_external_id;
DROP INDEX IF EXISTS idx_signals_external_id;

-- Adiciona constraint correta: unicidade composta por source + external_id
CREATE UNIQUE INDEX uq_signals_source_external_id ON signals(source, external_id) WHERE external_id IS NOT NULL;

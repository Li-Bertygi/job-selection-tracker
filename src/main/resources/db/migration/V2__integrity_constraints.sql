ALTER TABLE applications
    ADD CONSTRAINT chk_applications_priority_range
        CHECK (priority BETWEEN 0 AND 10);

ALTER TABLE stages
    ADD CONSTRAINT uk_stages_application_stage_order
        UNIQUE (application_id, stage_order);

ALTER TABLE schedules
    ADD CONSTRAINT chk_schedules_end_at_after_start_at
        CHECK (end_at IS NULL OR end_at >= start_at);

ALTER TABLE schedules
    ADD COLUMN stage_scope_id BIGINT GENERATED ALWAYS AS (COALESCE(stage_id, 0));

CREATE UNIQUE INDEX uk_schedules_application_scope_type_start_at
    ON schedules (application_id, stage_scope_id, schedule_type, start_at);

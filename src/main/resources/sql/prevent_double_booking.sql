-- ═══════════════════════════════════════════════════════════════════
-- Prevent double-booking: only one user can book a coach on a given day.
-- Run this against your GestionCoach database.
-- ═══════════════════════════════════════════════════════════════════

-- Add a UNIQUE constraint on (id_coach, date_seance) to enforce
-- at the DB level that no two bookings exist for the same coach+day.
-- The IF NOT EXISTS check prevents errors if run multiple times.

ALTER TABLE reservation
ADD UNIQUE INDEX uq_coach_date (id_coach, date_seance);

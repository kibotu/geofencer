package net.kibotu.geofencer.internal

internal object GeofenceEvaluationState {
    val evaluator = GeofenceConsistencyEvaluator()
    val dwellConfirmation = DwellConfirmation()
    val deduplicator = EventDeduplicator()
}

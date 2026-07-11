package com.example.hgh_assessment_prabesh_bhattarai.enums;

/**
 * Why a {@link DeviceAssignment} window was closed. Null while the assignment
 * is still active; set at the moment {@code assignedTo} is stamped.
 */
public enum AssignmentEndReason {

    /** Device was moved to another order before this window ended. */
    REASSIGNED,

    /** Device served the order to completion. */
    COMPLETED,

    /** Assignment was cancelled. */
    CANCELLED
}

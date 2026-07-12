package com.example.hgh_assessment_prabesh_bhattarai.dto.request;

import com.example.hgh_assessment_prabesh_bhattarai.enums.AssignmentEndReason;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CloseAssignmentRequest(

        /** Why the device is being released. REASSIGNED is not accepted here -- that is
         *  stamped automatically when a device moves straight to another order. */
        @NotNull(message = "reason is required")
        AssignmentEndReason reason,

        /** When the device came back. Optional; defaults to now. */
        Instant assignedTo
) {
}

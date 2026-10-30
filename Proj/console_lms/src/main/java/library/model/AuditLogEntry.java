package library.model;

import java.time.LocalDateTime;

/**
 * Represents an audit trail log entry for tracking critical actions in the system.
 */
public class AuditLogEntry {
    private String id;
    private LocalDateTime timestamp;
    private String action;
    private String performedBy;
    private String targetEntity;
    private String details;
    private boolean success;

    public AuditLogEntry() {
    }

    public AuditLogEntry(String id, LocalDateTime timestamp, String action, 
                         String performedBy, String targetEntity, String details, boolean success) {
        this.id = id;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.action = action;
        this.performedBy = performedBy != null ? performedBy : "SYSTEM";
        this.targetEntity = targetEntity != null ? targetEntity : "";
        this.details = details != null ? details : "";
        this.success = success;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + (success ? "SUCCESS" : "FAILURE") + " - " +
                action + " by '" + performedBy + "' on '" + targetEntity + "': " + details;
    }
}

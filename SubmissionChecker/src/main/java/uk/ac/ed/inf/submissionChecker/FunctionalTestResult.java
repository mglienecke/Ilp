package uk.ac.ed.inf.submissionChecker;

/**
 * describe the result of a functional test
 */
public class FunctionalTestResult {
    String title;
    String message;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void appendMessage(String appendExtraMessage){
        if (message == null){
            message = new String();
        }
        if (message.isEmpty() == false){
            message += "<br/>";
        }
        message += appendExtraMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    boolean success;

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    boolean warning;

    FunctionalTestResult(String title, String message, boolean success) {
        this.title = title;
        this.message = message;
        this.success = success;
    }
}

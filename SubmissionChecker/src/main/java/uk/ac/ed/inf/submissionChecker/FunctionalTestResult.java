package uk.ac.ed.inf.submissionChecker;

/**
 * describe the result of a functional test
 */
public class FunctionalTestResult {
    String title;
    String message;

    float pointsAchieved;

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
        appendMessage(message);
    }

    public void addPoints(float pointsToAdd){
        pointsAchieved += pointsToAdd;
    }

    public float getPointsAchieved(){
        return pointsAchieved;
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

    public void appendCodeBlockMessage(String codeBlock, String cssCodeBase){
        if (message == null){
            message = new String();
        }
        if (message.isEmpty() == false){
            message += "<br/>";
        }
        message += String.format("<pre><code class='%s'>%s</code></pre>", cssCodeBase, codeBlock);
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

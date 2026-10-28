package techchallenge.feedback.testsupport;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestContext implements Context {

    private final List<String> logs = new ArrayList<>();

    private final LambdaLogger logger = new LambdaLogger() {
        @Override
        public void log(String message) {
            logs.add(message);
        }

        @Override
        public void log(byte[] message) {
            logs.add(new String(message, StandardCharsets.UTF_8));
        }
    };

    public List<String> logs() {
        return logs;
    }

    @Override public String getAwsRequestId() { return "test-request"; }
    @Override public String getLogGroupName() { return "test-group"; }
    @Override public String getLogStreamName() { return "test-stream"; }
    @Override public String getFunctionName() { return "test-function"; }
    @Override public String getFunctionVersion() { return "$LATEST"; }
    @Override public String getInvokedFunctionArn() { return "arn:aws:lambda:us-east-1:000000000000:function:test"; }
    @Override public CognitoIdentity getIdentity() { return null; }
    @Override public ClientContext getClientContext() { return null; }
    @Override public int getRemainingTimeInMillis() { return 30_000; }
    @Override public int getMemoryLimitInMB() { return 512; }
    @Override public LambdaLogger getLogger() { return logger; }
}

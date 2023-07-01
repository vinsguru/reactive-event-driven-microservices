package com.vinsguru.common.orchestrator;

import com.vinsguru.common.messages.Response;

public interface WorkflowStep<T extends Response> extends
                                                        RequestSender,
                                                        RequestCompensator,
                                                        ResponseProcessor<T>,
                                                        WorkflowChain {


}

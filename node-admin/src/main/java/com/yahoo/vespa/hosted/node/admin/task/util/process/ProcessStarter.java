// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

package com.yahoo.vespa.hosted.node.admin.task.util.process;

/**
 * @author hakonhall
 */
public interface ProcessStarter {
    ProcessApi2 start(ProcessBuilder processBuilder);
}

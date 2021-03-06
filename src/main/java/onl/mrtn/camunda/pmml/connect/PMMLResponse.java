/*
 * Copyright (c) 2021. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package onl.mrtn.camunda.pmml.connect;

import org.camunda.connect.impl.AbstractConnectorResponse;

import java.util.Map;

public class PMMLResponse extends AbstractConnectorResponse  {

    public static String PARAM_NAME_OUTPUT = "output";

    private Map<String, ?> results;

    public PMMLResponse(Map<String, ?> results){
        this.results=results;
    }

    @Override
    protected void collectResponseParameters(Map<String, Object> map) {
        responseParameters.put(PARAM_NAME_OUTPUT, results);
    }
}

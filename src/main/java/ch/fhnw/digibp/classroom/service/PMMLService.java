/*
 * Copyright (c) 2021. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.classroom.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;

@Service
public class PMMLService {

    private final Logger logger = LoggerFactory.getLogger(PMMLService.class);

    @Inject
    private RepositoryService repositoryService;

    public Map<String, ?> evaluate(String fileName, String modelName, Map<String, ?> request, String tenantId, String definitionId) throws Exception {
        return evaluate(getDeploymentResource(tenantId, definitionId, fileName), modelName, request);
    }

    public Map<String, ?> evaluate(String fileName, String modelName, Map<String, ?> request, String deploymentId) throws Exception {
        return evaluate(getResourceAsStream(deploymentId, fileName), modelName, request);
    }

    public Map<String, List<?>> evaluateInfo(String fileName, String modelName, String deploymentId) throws Exception {
        InputStream pmmlFileInput = getResourceAsStream(deploymentId, fileName);

        Evaluator evaluator;
        if(modelName.isEmpty()) {
            evaluator = new LoadingModelEvaluatorBuilder().load(pmmlFileInput).build();
        } else {
            evaluator = new LoadingModelEvaluatorBuilder().load(pmmlFileInput, modelName).build();
        }

        evaluator.verify();

        Map<String, List<?>> info = new HashMap<>();

        List<? extends InputField> inputFields = evaluator.getInputFields();
        info.put("Input field(s)", inputFields);

        List<? extends TargetField> targetFields = evaluator.getTargetFields();
        info.put("Target field(s)", targetFields);

        List<? extends OutputField> outputFields = evaluator.getOutputFields();
        info.put("Output field(s)", outputFields);

        return info;
    }

    public Map<String, ?> evaluateGenerateInput(String fileName, String modelName, String deploymentId) throws Exception {
        InputStream pmmlFileInput = getResourceAsStream(deploymentId, fileName);

        Evaluator evaluator;
        if(modelName.isEmpty()) {
            evaluator = new LoadingModelEvaluatorBuilder().load(pmmlFileInput).build();
        } else {
            evaluator = new LoadingModelEvaluatorBuilder().load(pmmlFileInput, modelName).build();
        }

        evaluator.verify();

        Map<FieldName, ?> arguments = new LinkedHashMap<>();

        List<InputField> inputFields = evaluator.getInputFields();
        for (InputField inputField : inputFields) {
            arguments.put(inputField.getName(), null);
        }
        return EvaluatorUtil.decodeAll(arguments);
    }

    public Map<String, ?> evaluate(InputStream pmmlFileInput, String modelName, Map<String, ?> request) throws Exception {
        Evaluator evaluator;
        if(modelName.isEmpty()) {
            evaluator = new LoadingModelEvaluatorBuilder().load(pmmlFileInput).build();
        } else {
            evaluator = new LoadingModelEvaluatorBuilder().load(pmmlFileInput, modelName).build();
        }

        evaluator.verify();

        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

        List<InputField> inputFields = evaluator.getInputFields();
        for (InputField inputField : inputFields) {
            FieldName inputName = inputField.getName();

            String key = inputName.getValue();

            Object value = request.get(key);
            if (value == null && !request.containsKey(key)) {
                throw new Exception("Evaluation request does not specify an input field "+ key);
            }

            FieldValue inputValue = inputField.prepare(value);

            arguments.put(inputName, inputValue);
        }

        logger.debug("Evaluation request has prepared arguments: {}", arguments);

        Map<FieldName, ?> result = evaluator.evaluate(arguments);

        logger.debug("Evaluation request produced result: {}", result);

        return EvaluatorUtil.decodeAll(result);
    }

    public InputStream getDeploymentResource(String tenantId, String processDefinitionId, String resourceName) {
        String deploymentId;
        if (tenantId.isEmpty()) {
            deploymentId = repositoryService.createProcessDefinitionQuery().withoutTenantId().processDefinitionId(processDefinitionId).singleResult().getDeploymentId();
        } else {
            deploymentId = repositoryService.createProcessDefinitionQuery().tenantIdIn(tenantId).processDefinitionId(processDefinitionId).singleResult().getDeploymentId();
        }
        return getResourceAsStream(deploymentId, resourceName);
    }

    public InputStream getResourceAsStream(String deploymentId, String resourceName) {
        return repositoryService.getResourceAsStream(deploymentId, resourceName);
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> mapFromObject(Object variable, String message, String variableName)
    {
        EnsureUtil.ensureInstanceOf(message, variableName, variable, Map.class);
        return (Map<String, ?>) variable;
    }
}

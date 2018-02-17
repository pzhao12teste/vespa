// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.searchdefinition.processing;

import com.yahoo.config.application.api.DeployLogger;
import com.yahoo.search.query.profile.QueryProfileRegistry;
import com.yahoo.searchdefinition.MapTypeContext;
import com.yahoo.searchdefinition.RankProfile;
import com.yahoo.searchdefinition.RankProfileRegistry;
import com.yahoo.searchdefinition.Search;
import com.yahoo.searchlib.rankingexpression.RankingExpression;
import com.yahoo.tensor.TensorType;
import com.yahoo.tensor.evaluation.TypeContext;
import com.yahoo.vespa.model.container.search.QueryProfiles;

public class RankingExpressionTypeValidator extends Processor {

    private final QueryProfileRegistry queryProfiles;

    public RankingExpressionTypeValidator(Search search,
                                          DeployLogger deployLogger,
                                          RankProfileRegistry rankProfileRegistry,
                                          QueryProfiles queryProfiles) {
        super(search, deployLogger, rankProfileRegistry, queryProfiles);
        this.queryProfiles = queryProfiles.getRegistry();
    }

    @Override
    public void process() {
        for (RankProfile profile : rankProfileRegistry.allRankProfiles()) {
            try {
                validate(profile);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("In " + search + ", " + profile, e);
            }
        }
    }

    /** Throws an IllegalArgumentException if the given rank profile does not produce valid type */
    private void validate(RankProfile profile) {
        profile.parseExpressions();
        TypeContext context = profile.typeContext(queryProfiles);
        for (RankProfile.Macro macro : profile.getMacros().values())
            ensureValid(macro.getRankingExpression(), "macro '" + macro.getName() + "'", context);
        ensureValidDouble(profile.getFirstPhaseRanking(), "first-phase expression", context);
        ensureValidDouble(profile.getSecondPhaseRanking(), "second-phase expression", context);
    }

    private TensorType ensureValid(RankingExpression expression, String expressionDescription, TypeContext context) {
        if (expression == null) return null;

        TensorType type;
        try {
            type = expression.type(context);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The " + expressionDescription + " is invalid", e);
        }
        if (type == null) // Not expected to happen
            throw new IllegalStateException("Could not determine the type produced by " + expressionDescription);
        return type;
    }

    private void ensureValidDouble(RankingExpression expression, String expressionDescription, TypeContext context) {
        if (expression == null) return;
        TensorType type = ensureValid(expression, expressionDescription, context);
        if ( ! type.equals(TensorType.empty))
            throw new IllegalArgumentException("The " + expressionDescription + " must produce a double " +
                                               "(a tensor with no dimensions), but produces " + type);
    }

}

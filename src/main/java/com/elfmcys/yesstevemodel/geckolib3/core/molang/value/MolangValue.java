package com.elfmcys.yesstevemodel.geckolib3.core.molang.value;

import com.elfmcys.yesstevemodel.molang.parser.ast.Expression;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.github.bsideup.jabel.Desugar;

import java.util.List;

@Desugar
public record MolangValue(Expression[] expressions) implements IValue {
    public MolangValue(List<Expression> expressions) {
        this(expressions.toArray(new Expression[0]));
    }

    @Override
    public Object evalUnsafe(ExpressionEvaluator<?> evaluator) {
        Object lastResult = 0d;

        for (Expression expression : this.expressions) {
            lastResult = evaluator.eval(expression);
            Object returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        return lastResult;
    }
}

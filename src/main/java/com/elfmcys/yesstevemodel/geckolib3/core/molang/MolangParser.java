package com.elfmcys.yesstevemodel.geckolib3.core.molang;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.PrimaryBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.DoubleValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.MolangValue;
import com.elfmcys.yesstevemodel.molang.MolangEngine;
import com.elfmcys.yesstevemodel.molang.parser.ParseException;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;

import java.util.Map;

public class MolangParser {
    private final MolangEngine engine;
    private final PrimaryBinding primaryBinding;

    public MolangParser(Map<String, ObjectBinding> extraBindings) {
        this.primaryBinding = new PrimaryBinding(extraBindings);
        this.engine = MolangEngine.fromCustomBinding(this.primaryBinding);
    }

    @SuppressWarnings("unused")
    public IValue parseExpression(String molangExpression) {
        try {
            return this.parseExpressionUnsafe(molangExpression);
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to parse value \"{}\": {}", molangExpression, e.getMessage());
            return DoubleValue.ZERO;
        }
    }

    public IValue parseExpressionUnsafe(String molangExpression) throws ParseException {
        MolangValue value = new MolangValue(this.engine.parse(molangExpression));
        this.primaryBinding.popStackFrame();
        return value;
    }

    @SuppressWarnings("unused")
    public IValue getConstant(double value) {
        return new DoubleValue(value);
    }

    public void reset() {
        this.primaryBinding.reset();
    }
}

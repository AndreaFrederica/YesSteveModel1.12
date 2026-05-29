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
        return this.parseExpression(molangExpression, false);
    }

    @SuppressWarnings("unused")
    public IValue parseExpression(String molangExpression, boolean isScript) {
        try {
            return this.parseExpressionUnsafe(molangExpression, isScript);
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to parse value \"{}\": {}", molangExpression, e.getMessage());
            return DoubleValue.ZERO;
        }
    }

    public IValue parseExpressionUnsafe(String molangExpression) throws ParseException {
        return this.parseExpressionUnsafe(molangExpression, false);
    }

    public IValue parseExpressionUnsafe(String molangExpression, boolean isScript) throws ParseException {
        MolangValue value = new MolangValue(this.engine.parse(isScript ? stripComments(molangExpression) : molangExpression));
        this.primaryBinding.popStackFrame();
        return value;
    }

    private static String stripComments(String input) {
        if (input.indexOf('/') < 0) {
            return input;
        }

        int len = input.length();
        StringBuilder result = new StringBuilder(len);
        boolean inBlockComment = false;
        boolean inLineComment = false;
        boolean inStringLiteral = false;

        for (int i = 0; i < len; i++) {
            char currentChar = input.charAt(i);

            if (inStringLiteral) {
                if (currentChar == '\'') {
                    inStringLiteral = false;
                }
                result.append(currentChar);
                continue;
            }

            if (inLineComment) {
                if (currentChar == '\r' || currentChar == '\n') {
                    inLineComment = false;
                    result.append('\n');
                }
                continue;
            }

            if (inBlockComment) {
                if (currentChar == '*' && i + 1 < len && input.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (currentChar == '\'') {
                inStringLiteral = true;
                result.append(currentChar);
                continue;
            }

            if (currentChar == '/' && i + 1 < len) {
                char nextChar = input.charAt(i + 1);
                if (nextChar == '/') {
                    inLineComment = true;
                    i++;
                    continue;
                }
                if (nextChar == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
            }

            result.append(currentChar);
        }

        return result.toString();
    }

    @SuppressWarnings("unused")
    public IValue getConstant(double value) {
        return new DoubleValue(value);
    }

    public void reset() {
        this.primaryBinding.reset();
    }
}

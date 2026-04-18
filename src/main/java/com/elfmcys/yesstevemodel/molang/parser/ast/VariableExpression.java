package com.elfmcys.yesstevemodel.molang.parser.ast;

import com.elfmcys.yesstevemodel.molang.runtime.Variable;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class VariableExpression implements Expression {
    private final Variable target;

    public VariableExpression(Variable target) {
        Objects.requireNonNull(target, "target");
        this.target = target;
    }

    public Variable target() {
        return this.target;
    }

    @Override
    public <R> R visit(final @Nonnull ExpressionVisitor<R> visitor) {
        return visitor.visitVariable(this);
    }

    @Override
    public String toString() {
        return this.target.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        VariableExpression that = (VariableExpression) o;
        return this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return this.target.hashCode();
    }
}

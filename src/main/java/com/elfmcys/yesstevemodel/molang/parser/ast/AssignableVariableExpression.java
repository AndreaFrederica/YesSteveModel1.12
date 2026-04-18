package com.elfmcys.yesstevemodel.molang.parser.ast;

import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class AssignableVariableExpression implements Expression {
    private final AssignableVariable target;

    public AssignableVariableExpression(AssignableVariable target) {
        Objects.requireNonNull(target, "target");
        this.target = target;
    }

    public AssignableVariable target() {
        return this.target;
    }

    @Override
    public <R> R visit(final @Nonnull ExpressionVisitor<R> visitor) {
        return visitor.visitAssignableVariable(this);
    }

    @Override
    public String toString() {
        return this.target.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AssignableVariableExpression that = (AssignableVariableExpression) o;
        return this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return this.target.hashCode();
    }
}

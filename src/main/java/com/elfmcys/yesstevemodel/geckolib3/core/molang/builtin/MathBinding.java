package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math.*;

public class MathBinding extends ContextBinding {
    public static final MathBinding INSTANCE = new MathBinding();

    private MathBinding() {
        /* 常量 */
        this.constValue("pi", Math.PI);
        this.constValue("e", Math.E);

        /* 取整函数 */
        this.function("floor", new Floor());
        this.function("round", new Round());
        this.function("ceil", new Ceil());
        this.function("trunc", new Trunc());

        /* 比较函数 */
        this.function("clamp", new Clamp());
        this.function("max", new Max());
        this.function("min", new Min());

        /* 经典数学函数 */
        this.function("abs", new Abs());
        this.function("exp", new Exp());
        this.function("ln", new Ln());
        this.function("sqrt", new Sqrt());
        this.function("mod", new Mod());
        this.function("pow", new Pow());

        /* 三角函数 */
        this.function("sin", new Sin());     // degree
        this.function("cos", new Cos());     // degree
        this.function("acos", new ACos());
        this.function("asin", new ASin());
        this.function("atan", new Atan());
        this.function("atan2", new ATan2());

        /* 实用工具 */
        this.function("lerp", new Lerp());
        this.function("lerprotate", new LerpRotate());
        this.function("random", new Random());
        this.function("random_integer", new RandomInteger());
        this.function("die_roll", new DieRoll());
        this.function("die_roll_integer", new DieRollInteger());
        this.function("hermite_blend", new HermitBlend());

        /* 其它 */
        this.function("min_angle", new MinAngle());

        /* 非标准命名，兼容原 geckolib */
        this.function("randomi", new RandomInteger());
        this.function("roll", new DieRoll());
        this.function("rolli", new DieRollInteger());
        this.function("hermite", new HermitBlend());
    }
}
